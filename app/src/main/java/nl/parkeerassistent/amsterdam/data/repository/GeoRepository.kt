package nl.parkeerassistent.amsterdam.data.repository

import nl.parkeerassistent.amsterdam.data.model.ParkingMeter
import nl.parkeerassistent.amsterdam.data.remote.GeoApi

interface GeoRepository {
    suspend fun parkingMetersNearby(lat: Double, lon: Double, n: Int = 25): List<ParkingMeter>
    suspend fun parkingMetersInRegion(lat: Double, lon: Double): List<ParkingMeter>
}

class GeoRepositoryImpl(private val api: GeoApi) : GeoRepository {
    override suspend fun parkingMetersNearby(lat: Double, lon: Double, n: Int): List<ParkingMeter> =
        api.nearby(lat, lon, n)
    override suspend fun parkingMetersInRegion(lat: Double, lon: Double): List<ParkingMeter> =
        api.inRegion(lat, lon)
}
