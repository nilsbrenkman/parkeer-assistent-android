package nl.parkeerassistent.amsterdam.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.data.model.ParkingMeter
import nl.parkeerassistent.amsterdam.data.model.ParkingMeterType
import nl.parkeerassistent.amsterdam.location.LocationProvider
import nl.parkeerassistent.amsterdam.ui.components.TitleBar
import nl.parkeerassistent.amsterdam.ui.parkingmeter.ParkingMeterViewModel
import nl.parkeerassistent.amsterdam.ui.theme.AppShape
import nl.parkeerassistent.amsterdam.ui.theme.AppTheme
import nl.parkeerassistent.amsterdam.ui.theme.Dimens
import nl.parkeerassistent.amsterdam.ui.theme.ParkeerAssistentTheme
import nl.parkeerassistent.amsterdam.ui.user.UserViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * Parking-meter picker (iOS `ParkingMeterView`). A list of meters near the current location
 * (Amsterdam centre fallback), sorted by distance by the server. Selecting one sets it on the
 * shared [UserViewModel] (updating zone/regime/cost) and returns.
 *
 * NOTE: iOS shows these on a MapKit map; a Google Map here needs a Maps API key the project
 * doesn't have, so this is a list. The data/selection path is identical — swap in a map later.
 */
@Composable
fun ParkingMeterScreen(
    userVm: UserViewModel,
    onBack: () -> Unit,
    vm: ParkingMeterViewModel = koinViewModel(),
    location: LocationProvider = koinInject(),
) {
    val meters by vm.meters.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    suspend fun loadNearby() {
        val (lat, lon) = location.current()
            ?: (LocationProvider.AMSTERDAM_LAT to LocationProvider.AMSTERDAM_LON)
        vm.fetchNearby(lat, lon)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { scope.launch { loadNearby() } }

    LaunchedEffect(Unit) {
        if (location.hasPermission()) {
            loadNearby()
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            )
        }
    }

    ParkingMeterContent(
        meters = meters,
        onSelect = {
            userVm.setParkingMeter(it.id.toLong())
            onBack()
        },
    )
}

@Composable
private fun ParkingMeterContent(meters: List<ParkingMeter>, onSelect: (ParkingMeter) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TitleBar(title = stringResource(R.string.parking_sign))
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = Dimens.paddingNormal)) {
            items(meters, key = { it.id }) { meter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(meter) }
                        .padding(vertical = Dimens.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
                ) {
                    Text(
                        text = meter.id.toString(),
                        color = AppTheme.colors.onHeader,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(AppTheme.colors.header, AppShape.roundedSmall)
                            .padding(horizontal = Dimens.paddingSmall, vertical = Dimens.paddingNano),
                    )
                    Text(meter.name, Modifier.weight(1f))
                    meter.distance?.let { Text("${"%.0f".format(it)} m") }
                }
                HorizontalDivider()
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun ParkingMeterPreview() = ParkeerAssistentTheme {
    ParkingMeterContent(
        meters = listOf(
            ParkingMeter(55105, 1, "Nieuwmarkt", ParkingMeterType.METER, 52.3725, 4.9005, 80.0),
            ParkingMeter(55106, 1, "Waterlooplein", ParkingMeterType.SIGN, 52.3680, 4.9020, 240.0),
        ),
        onSelect = {},
    )
}
