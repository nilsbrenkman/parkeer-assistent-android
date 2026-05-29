package nl.parkeerassistent.amsterdam.data.remote.cookie

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * Persists and replays only the `token` and `product_id` cookies, ignoring everything else —
 * the same whitelist the iOS client keeps. `token` is set on login; `product_id` is set by
 * `GET user`. Both must be sent on authenticated requests.
 */
class SessionCookieJar(private val store: SessionCookieStore) : CookieJar {

    override fun loadForRequest(url: HttpUrl): List<Cookie> =
        NAMES.mapNotNull { name ->
            store.get(name)?.let { value ->
                Cookie.Builder()
                    .name(name)
                    .value(value)
                    .domain(url.host)
                    .path("/")
                    .build()
            }
        }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        for (cookie in cookies) {
            if (cookie.name !in NAMES) continue
            val expired = cookie.expiresAt < System.currentTimeMillis()
            if (cookie.value.isEmpty() || expired) {
                store.remove(cookie.name)
            } else {
                store.put(cookie.name, cookie.value)
            }
        }
    }

    /** Drops the session entirely (called on 401/403). */
    fun clear() = store.clear()

    private companion object {
        val NAMES = setOf("token", "product_id")
    }
}
