package nl.parkeerassistent.amsterdam.data.model

import kotlinx.serialization.Serializable

/** Generic success/message envelope returned by most write endpoints. */
@Serializable
data class Response(
    val success: Boolean,
    val message: String? = null,
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

// Note: `POST login` returns a [Response] to the app; the bearer token arrives as the `token`
// cookie (handled by SessionCookieJar). The server's internal LoginResponse is not app-facing.
