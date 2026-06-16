package nl.parkeerassistent.amsterdam.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.location.Location
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import nl.parkeerassistent.amsterdam.BuildConfig
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.data.model.ParkingMeter
import nl.parkeerassistent.amsterdam.data.model.ParkingMeterType
import nl.parkeerassistent.amsterdam.location.LocationProvider
import nl.parkeerassistent.amsterdam.ui.components.TitleBar
import nl.parkeerassistent.amsterdam.ui.parkingmeter.ParkingMeterViewModel
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.Dimens
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import nl.parkeerassistent.amsterdam.ui.user.UserViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.style.layers.Property
import android.graphics.Color as AndroidColor

/**
 * Parking-meter picker (iOS `ParkingMeterView`). A MapLibre map of the meters near the centre:
 * each meter is a badge marker showing its id; tapping one sets it on the shared [UserViewModel]
 * (updating zone/regime/cost) and returns. Panning the map re-fetches the meters around the new
 * centre (debounced by a 50 m guard, like iOS).
 *
 * On open the map centres on the **active** parking meter (fetched via `GET geo/parking-meters/{id}`)
 * when the user has one; otherwise it falls back to the user's location (Amsterdam centre if no
 * location) and follows the GPS dot.
 *
 * Tiles are self-hosted Protomaps vector tiles via [BuildConfig.MAP_STYLE_URL] (no API key); the
 * server/k8s hosting of that style is tracked separately in docs/TO_DO.md.
 */
@Composable
fun ParkingMeterScreen(
    userVm: UserViewModel,
    onBack: () -> Unit,
    vm: ParkingMeterViewModel = koinViewModel(),
    location: LocationProvider = koinInject(),
) {
    val meters by vm.meters.collectAsStateWithLifecycle()
    val userState by userVm.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Where to centre the map once it's ready: the active meter if there is one, otherwise the
    // user (or Amsterdam). Resolved asynchronously in loadInitial.
    var initialCenter by remember { mutableStateOf<LatLng?>(null) }
    // Whether the camera should follow the GPS dot. Only when centring on the user — when an active
    // meter sets the centre we keep the camera there instead of snapping to the user's location.
    var followUser by remember { mutableStateOf(false) }
    // Last location we fetched meters for — used to skip near-duplicate fetches while panning.
    var lastFetch by remember { mutableStateOf<LatLng?>(null) }

    fun fetchAround(lat: Double, lon: Double) {
        lastFetch = LatLng(lat, lon)
        vm.fetchNearby(lat, lon)
    }

    suspend fun loadInitial() {
        // Prefer the active parking meter: fetch its details and centre there.
        val activeMeter = userState.parkingMeterId?.let { vm.details(it) }
        if (activeMeter != null) {
            initialCenter = LatLng(activeMeter.latitude, activeMeter.longitude)
            followUser = false
            fetchAround(activeMeter.latitude, activeMeter.longitude)
            return
        }
        // Otherwise fall back to the user's location (or Amsterdam) and follow the GPS dot.
        val (lat, lon) = location.current()
            ?: (LocationProvider.AMSTERDAM_LAT to LocationProvider.AMSTERDAM_LON)
        initialCenter = LatLng(lat, lon)
        followUser = location.hasPermission()
        fetchAround(lat, lon)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { scope.launch { loadInitial() } }

    LaunchedEffect(Unit) {
        if (location.hasPermission()) {
            loadInitial()
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            )
        }
    }

    ParkingMeterContent(
        meters = meters,
        initialCenter = initialCenter,
        hasLocationPermission = location.hasPermission(),
        followUser = followUser,
        onCameraMoved = { lat, lon ->
            val last = lastFetch
            val moved = last == null || distanceMeters(last.latitude, last.longitude, lat, lon) >= 50.0
            if (moved) fetchAround(lat, lon)
        },
        onSelect = {
            userVm.setParkingMeter(it.id.toLong())
            onBack()
        },
    )
}

@Composable
internal fun ParkingMeterContent(
    meters: List<ParkingMeter>,
    initialCenter: LatLng? = null,
    hasLocationPermission: Boolean = false,
    followUser: Boolean = false,
    onCameraMoved: (lat: Double, lon: Double) -> Unit = { _, _ -> },
    onSelect: (ParkingMeter) -> Unit = {},
) {
    Column(Modifier.fillMaxSize()) {
        TitleBar(title = stringResource(R.string.parking_sign))
        // @Preview / tests can't host a GL surface — skip the live map there.
        if (LocalInspectionMode.current) {
            Box(Modifier.fillMaxSize())
        } else {
            MeterMap(
                meters = meters,
                initialCenter = initialCenter,
                hasLocationPermission = hasLocationPermission,
                followUser = followUser,
                onCameraMoved = onCameraMoved,
                onSelect = onSelect,
            )
        }
    }
}

private const val DEFAULT_ZOOM_LEVEL = 16.0
private const val MIN_ZOOM_LEVEL = 11.0
private const val MAX_ZOOM_LEVEL = 18.0

@Composable
private fun MeterMap(
    meters: List<ParkingMeter>,
    initialCenter: LatLng?,
    hasLocationPermission: Boolean,
    followUser: Boolean,
    onCameraMoved: (lat: Double, lon: Double) -> Unit,
    onSelect: (ParkingMeter) -> Unit,
) {
    val badgeBg = AppTheme.colors.header.toArgb()
    val badgeFg = AppTheme.colors.onHeader.toArgb()
    val density = LocalResources.current.displayMetrics.density

    val mapView = rememberMapViewWithLifecycle()

    // Map/style/symbol handles, populated once (the map and then its style finish loading).
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var style by remember { mutableStateOf<Style?>(null) }
    var symbolManager by remember { mutableStateOf<SymbolManager?>(null) }
    // SymbolManager click gives us a Symbol id → look up the meter it represents.
    val symbolToMeter = remember { mutableMapOf<Long, ParkingMeter>() }

    // Keep listeners pointing at the latest lambdas without re-creating the map.
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnCameraMoved by rememberUpdatedState(onCameraMoved)

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        // iOS shows a MapUserLocationButton when authorized — recentre on the GPS dot on tap.
        if (hasLocationPermission) {
            FloatingActionButton(
                onClick = { map?.let(::recenterOnUser) },
                containerColor = AppTheme.colors.header,
                contentColor = AppTheme.colors.onHeader,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Dimens.paddingNormal),
            ) {
                Icon(
                    Icons.Filled.MyLocation,
                    contentDescription = stringResource(R.string.meter_my_location),
                )
            }
        }
    }

    // One-time map + style setup (keyed on the stable mapView, so it runs once — not per recompose,
    // which would stack camera listeners / re-create the SymbolManager).
    LaunchedEffect(mapView) {
        mapView.getMapAsync { loadedMap ->
            loadedMap.setMinZoomPreference(MIN_ZOOM_LEVEL)
            loadedMap.setMaxZoomPreference(MAX_ZOOM_LEVEL)
            loadedMap.setLatLngBoundsForCameraTarget(AMSTERDAM_BOUNDS)
            loadedMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialCenter ?: AMSTERDAM, DEFAULT_ZOOM_LEVEL))
            loadedMap.addOnCameraIdleListener {
                val target = loadedMap.cameraPosition.target ?: return@addOnCameraIdleListener
                currentOnCameraMoved(target.latitude, target.longitude)
            }
            loadedMap.setStyle(Style.Builder().fromUri(BuildConfig.MAP_STYLE_URL)) { loadedStyle ->
                symbolManager = SymbolManager(mapView, loadedMap, loadedStyle).apply {
                    iconAllowOverlap = true
                    textAllowOverlap = true
                    addClickListener { symbol ->
                        symbolToMeter[symbol.id]?.let { currentOnSelect(it) }
                        true
                    }
                }
                style = loadedStyle
                map = loadedMap
            }
        }
    }

    // Activate the user-location dot once the style is ready and permission is held (re-runs if the
    // permission is granted while the screen is open). Only track the GPS dot when [followUser] —
    // when an active meter sets the centre we show the dot but leave the camera on the meter.
    LaunchedEffect(map, style, hasLocationPermission, followUser) {
        val readyMap = map ?: return@LaunchedEffect
        val loadedStyle = style ?: return@LaunchedEffect
        enableUserLocation(readyMap, loadedStyle, mapView.context, hasLocationPermission, followUser)
    }

    // Re-draw the meter badges whenever the list changes (and once the style is ready).
    LaunchedEffect(meters, symbolManager, style) {
        val manager = symbolManager ?: return@LaunchedEffect
        val loadedStyle = style ?: return@LaunchedEffect
        manager.deleteAll()
        symbolToMeter.clear()
        meters.forEach { meter ->
            val image = "meter-${meter.id}"
            loadedStyle.addImage(image, meterBadge(meter.id.toString(), badgeBg, badgeFg, density))
            val symbol = manager.create(
                SymbolOptions()
                    .withLatLng(LatLng(meter.latitude, meter.longitude))
                    .withIconImage(image)
                    .withIconAnchor(Property.ICON_ANCHOR_CENTER),
            )
            symbolToMeter[symbol.id] = meter
        }
    }

    // Centre on the resolved target (active meter, or the user/Amsterdam fallback) once it's known.
    // Skipped while [followUser]: there the GPS dot is followed (CameraMode.TRACKING in
    // enableUserLocation), and an external animateCamera would cancel that tracking — leaving the
    // map stuck on the fallback. With an active meter (followUser == false) we always centre here.
    LaunchedEffect(map, initialCenter, followUser) {
        if (followUser) return@LaunchedEffect
        val readyMap = map ?: return@LaunchedEffect
        val center = initialCenter ?: return@LaunchedEffect
        readyMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, DEFAULT_ZOOM_LEVEL))
    }
}

@SuppressLint("MissingPermission")
private fun enableUserLocation(
    map: MapLibreMap,
    style: Style,
    context: android.content.Context,
    hasPermission: Boolean,
    followUser: Boolean,
) {
    if (!hasPermission) return
    map.locationComponent.apply {
        activateLocationComponent(
            LocationComponentActivationOptions.builder(context, style).build(),
        )
        isLocationComponentEnabled = true
        // Follow the GPS dot so the map centres on the user once the first fix arrives (relying on
        // `getCurrentLocation` for the initial centre is unreliable right after a permission grant).
        // MapLibre drops back to NONE automatically as soon as the user pans the map. When centring
        // on an active meter we still show the dot but leave the camera on the meter (NONE).
        cameraMode = if (followUser) CameraMode.TRACKING else CameraMode.NONE
        renderMode = RenderMode.COMPASS
    }
}

/** Re-centre on the user's GPS dot (the recentre button / iOS `MapUserLocationButton`). */
private fun recenterOnUser(map: MapLibreMap) {
    val locationComponent = map.locationComponent
    if (!locationComponent.isLocationComponentActivated) return
    locationComponent.cameraMode = CameraMode.TRACKING
    locationComponent.lastKnownLocation?.let {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude),
            DEFAULT_ZOOM_LEVEL
        ))
    }
}

/** Creates and lifecycle-binds a [MapView], destroying it when the composable leaves. */
@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context).apply {
            id = View.generateViewId()
            onCreate(null)
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onStop()
            mapView.onDestroy()
        }
    }
    return mapView
}

/** Draws an iOS-style id badge: white bold text on a header-coloured rounded rect. */
private fun meterBadge(text: String, bgColor: Int, fgColor: Int, density: Float): Bitmap {
    val pad = 20f * density
    val radius = 6f * density
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fgColor
        textSize = 24f * density
        typeface = Typeface.DEFAULT_BOLD
    }
    val bounds = Rect()
    textPaint.getTextBounds(text, 0, text.length, bounds)
    val width = (bounds.width() + pad * 2).toInt().coerceAtLeast(1)
    val height = (bounds.height() + pad * 2).toInt().coerceAtLeast(1)
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor }
    canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), radius, radius, bgPaint)
    val strokeWidth = 2f * density
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidth
    }
    val inset = strokeWidth / 2f
    canvas.drawRoundRect(
        RectF(inset, inset, width - inset, height - inset),
        radius,
        radius,
        borderPaint,
    )
    val x = pad - bounds.left
    val y = height / 2f - (bounds.top + bounds.bottom) / 2f
    canvas.drawText(text, x, y, textPaint)
    return bitmap
}

private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val result = FloatArray(1)
    Location.distanceBetween(lat1, lon1, lat2, lon2, result)
    return result[0].toDouble()
}

private val AMSTERDAM = LatLng(LocationProvider.AMSTERDAM_LAT, LocationProvider.AMSTERDAM_LON)

// Restrict panning to roughly Amsterdam (mirrors iOS `MapCameraBounds.amsterdam`).
private val AMSTERDAM_BOUNDS: LatLngBounds = LatLngBounds.Builder()
    .include(LatLng(LocationProvider.AMSTERDAM_LAT - 0.125, LocationProvider.AMSTERDAM_LON - 0.125))
    .include(LatLng(LocationProvider.AMSTERDAM_LAT + 0.125, LocationProvider.AMSTERDAM_LON + 0.125))
    .build()

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun ParkingMeterPreview() = ParkeerAssistentTheme {
    ParkingMeterContent(
        meters = listOf(
            ParkingMeter(55105, 1, "Nieuwmarkt", ParkingMeterType.METER, 52.3725, 4.9005, 80.0),
            ParkingMeter(55106, 1, "Waterlooplein", ParkingMeterType.SIGN, 52.3680, 4.9020, 240.0),
        ),
    )
}
