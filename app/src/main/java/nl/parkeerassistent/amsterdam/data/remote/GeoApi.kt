package nl.parkeerassistent.amsterdam.data.remote

import nl.parkeerassistent.amsterdam.data.model.ParkingMeter
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GeoApi {

    @GET("geo/parking-meters/nearby")
    suspend fun nearby(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("n") n: Int = 25,
    ): List<ParkingMeter>

    /** Details for a single meter; the server returns 404 when [id] is unknown. */
    @GET("geo/parking-meters/{id}")
    suspend fun details(@Path("id") id: Long): ParkingMeter

    @GET("geo/parking-meters/in-region")
    suspend fun inRegion(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): List<ParkingMeter>
}
