package nl.parkeerassistent.amsterdam.data.repository

import nl.parkeerassistent.amsterdam.data.model.LoginRequest
import nl.parkeerassistent.amsterdam.data.model.Response
import nl.parkeerassistent.amsterdam.data.remote.LoginApi

/**
 * Repositories are thin wrappers over the Retrofit services (port of the iOS `client` layer).
 * They let [nl.parkeerassistent.amsterdam.data.remote.ApiException] propagate; ViewModels catch
 * and route errors to the message bus, mirroring the iOS stores' `do/catch`.
 */
interface LoginRepository {
    suspend fun isLoggedIn(): Response
    suspend fun login(username: String, password: String): Response
    suspend fun logout(): Response
}

class LoginRepositoryImpl(private val api: LoginApi) : LoginRepository {
    override suspend fun isLoggedIn(): Response = api.isLoggedIn()
    override suspend fun login(username: String, password: String): Response =
        api.login(LoginRequest(username, password))
    override suspend fun logout(): Response = api.logout()
}
