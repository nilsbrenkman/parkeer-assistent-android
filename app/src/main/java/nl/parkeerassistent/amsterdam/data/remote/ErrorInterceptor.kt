package nl.parkeerassistent.amsterdam.data.remote

import nl.parkeerassistent.amsterdam.data.remote.cookie.SessionCookieJar
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Maps non-2xx responses to [ApiException], mirroring the iOS `ApiClient` error handling:
 * 401/403 clear the session and surface as [ApiException.Unauthorized]; everything else becomes
 * [ApiException.ServerError] carrying the response body. The server's own `Set-Cookie` clearing
 * of `token` on 401 is already applied by the [SessionCookieJar]; we also clear explicitly so
 * `product_id` goes too.
 */
class ErrorInterceptor(private val cookieJar: SessionCookieJar) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.isSuccessful) return response

        when (response.code) {
            401, 403 -> {
                response.close()
                cookieJar.clear()
                throw ApiException.Unauthorized()
            }
            404 -> {
                response.close()
                throw ApiException.NotFound()
            }
            else -> {
                val body = response.body.string()
                response.close()
                throw ApiException.ServerError(body)
            }
        }
    }
}
