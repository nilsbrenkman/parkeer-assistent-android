package nl.parkeerassistent.amsterdam.data.remote

import nl.parkeerassistent.amsterdam.data.model.ParkingMeter
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoApi {

    @GET("geo/parking-meters/nearby")
    suspend fun nearby(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("n") n: Int = 25,
    ): List<ParkingMeter>

    @GET("geo/parking-meters/in-region")
    suspend fun inRegion(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): List<ParkingMeter>
}
