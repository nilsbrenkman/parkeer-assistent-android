package nl.parkeerassistent.amsterdam.data.remote

import java.io.IOException

/**
 * Typed client errors, the Android counterpart of the iOS `ClientError`. Extends [IOException]
 * so it propagates cleanly out of the OkHttp interceptor chain and Retrofit suspend calls.
 */
sealed class ApiException(message: String?) : IOException(message) {

    /** 401/403 — the session is no longer valid; cookies are cleared. */
    class Unauthorized : ApiException("Unauthorized")

    /** 404 — the requested resource does not exist. */
    class NotFound : ApiException("Not Found")

    /** Any other non-2xx response; [serverMessage] is the raw response body. */
    class ServerError(val serverMessage: String) : ApiException(serverMessage)
}
