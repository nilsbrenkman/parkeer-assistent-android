package nl.parkeerassistent.amsterdam.data.remote

import nl.parkeerassistent.amsterdam.data.model.AddParkingRequest
import nl.parkeerassistent.amsterdam.data.model.HistoryResponse
import nl.parkeerassistent.amsterdam.data.model.ParkingResponse
import nl.parkeerassistent.amsterdam.data.model.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ParkingApi {

    @GET("parking")
    suspend fun getParking(): ParkingResponse

    @POST("parking")
    suspend fun addParking(@Body body: AddParkingRequest): Response

    @DELETE("parking/{id}")
    suspend fun stopParking(@Path("id") id: Long): Response

    @GET("parking/history")
    suspend fun getHistory(): HistoryResponse
}
