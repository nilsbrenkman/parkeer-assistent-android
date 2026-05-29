package nl.parkeerassistent.amsterdam.data.remote

import nl.parkeerassistent.amsterdam.data.model.LoginRequest
import nl.parkeerassistent.amsterdam.data.model.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LoginApi {

    @GET("login")
    suspend fun isLoggedIn(): Response

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response

    @GET("logout")
    suspend fun logout(): Response
}
