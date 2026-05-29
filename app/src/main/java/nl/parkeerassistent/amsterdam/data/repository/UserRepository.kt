package nl.parkeerassistent.amsterdam.data.repository

import nl.parkeerassistent.amsterdam.data.model.BalanceResponse
import nl.parkeerassistent.amsterdam.data.model.RegimeResponse
import nl.parkeerassistent.amsterdam.data.model.UserResponse
import nl.parkeerassistent.amsterdam.data.remote.UserApi

interface UserRepository {
    suspend fun getUser(): UserResponse
    suspend fun getBalance(): BalanceResponse
    suspend fun getRegime(parkingMeterId: Long): RegimeResponse
}

class UserRepositoryImpl(private val api: UserApi) : UserRepository {
    override suspend fun getUser(): UserResponse = api.getUser()
    override suspend fun getBalance(): BalanceResponse = api.getBalance()
    override suspend fun getRegime(parkingMeterId: Long): RegimeResponse = api.getRegime(parkingMeterId)
}
