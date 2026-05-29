package nl.parkeerassistent.amsterdam.data.remote

import nl.parkeerassistent.amsterdam.data.model.AddVisitorRequest
import nl.parkeerassistent.amsterdam.data.model.Response
import nl.parkeerassistent.amsterdam.data.model.VisitorResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface VisitorApi {

    @GET("visitor")
    suspend fun getVisitors(): VisitorResponse

    @POST("visitor")
    suspend fun addVisitor(@Body body: AddVisitorRequest): Response

    @DELETE("visitor/{id}")
    suspend fun deleteVisitor(@Path("id") id: Long): Response
}
