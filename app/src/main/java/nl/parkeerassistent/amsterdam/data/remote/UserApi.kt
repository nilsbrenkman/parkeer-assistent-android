package nl.parkeerassistent.amsterdam.data.remote

import nl.parkeerassistent.amsterdam.data.model.BalanceResponse
import nl.parkeerassistent.amsterdam.data.model.RegimeResponse
import nl.parkeerassistent.amsterdam.data.model.UserResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {

    @GET("user")
    suspend fun getUser(): UserResponse

    @GET("user/balance")
    suspend fun getBalance(): BalanceResponse

    @GET("user/regime/{parkingMeterId}")
    suspend fun getRegime(@Path("parkingMeterId") parkingMeterId: Long): RegimeResponse
}
