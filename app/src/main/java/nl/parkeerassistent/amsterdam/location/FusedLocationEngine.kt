package nl.parkeerassistent.amsterdam.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.maplibre.android.location.engine.LocationEngine
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.engine.LocationEngineResult

/**
 * MapLibre [LocationEngine] backed by Google's FusedLocationProviderClient.
 *
 * MapLibre's built-in engine (MapLibreFusedLocationEngineImpl) routes through the platform
 * LocationManager and falls back to the "passive" provider whenever getBestProvider returns null
 * (common on the emulator / when only coarse is granted). The passive provider requires
 * ACCESS_FINE_LOCATION and throws a SecurityException when the app only holds ACCESS_COARSE_LOCATION.
 * The fused client honours coarse permission (degrading accuracy automatically) and never touches
 * the passive provider, so the location component works without requesting fine location.
 */
class FusedLocationEngine(context: Context) : LocationEngine {

    private val client: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Each MapLibre callback maps to one Google callback so removeLocationUpdates can detach it.
    private val callbacks =
        mutableMapOf<LocationEngineCallback<LocationEngineResult>, LocationCallback>()

    @SuppressLint("MissingPermission")
    override fun getLastLocation(callback: LocationEngineCallback<LocationEngineResult>) {
        client.lastLocation
            .addOnSuccessListener { location: Location? ->
                callback.onSuccess(LocationEngineResult.create(location))
            }
            .addOnFailureListener { e -> callback.onFailure(e) }
    }

    @SuppressLint("MissingPermission")
    override fun requestLocationUpdates(
        request: LocationEngineRequest,
        callback: LocationEngineCallback<LocationEngineResult>,
        looper: Looper?,
    ) {
        val googleCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                callback.onSuccess(LocationEngineResult.create(result.locations))
            }
        }
        callbacks[callback] = googleCallback
        client.requestLocationUpdates(
            request.toGoogleRequest(),
            googleCallback,
            looper ?: Looper.getMainLooper(),
        )
    }

    @SuppressLint("MissingPermission")
    override fun requestLocationUpdates(request: LocationEngineRequest, pendingIntent: PendingIntent?) {
        pendingIntent ?: return
        client.requestLocationUpdates(request.toGoogleRequest(), pendingIntent)
    }

    override fun removeLocationUpdates(callback: LocationEngineCallback<LocationEngineResult>) {
        callbacks.remove(callback)?.let { client.removeLocationUpdates(it) }
    }

    override fun removeLocationUpdates(pendingIntent: PendingIntent?) {
        pendingIntent ?: return
        client.removeLocationUpdates(pendingIntent)
    }

    private fun LocationEngineRequest.toGoogleRequest(): LocationRequest {
        val googlePriority = when (priority) {
            LocationEngineRequest.PRIORITY_HIGH_ACCURACY -> Priority.PRIORITY_HIGH_ACCURACY
            LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
            LocationEngineRequest.PRIORITY_LOW_POWER -> Priority.PRIORITY_LOW_POWER
            else -> Priority.PRIORITY_PASSIVE
        }
        return LocationRequest.Builder(googlePriority, interval)
            .setMinUpdateIntervalMillis(fastestInterval)
            .setMinUpdateDistanceMeters(displacement)
            .setMaxUpdateDelayMillis(maxWaitTime)
            .build()
    }
}
