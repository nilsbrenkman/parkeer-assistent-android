package nl.parkeerassistent.amsterdam.data.remote.cookie

import android.content.Context

/**
 * Synchronous persistence for the two session cookies (`token`, `product_id`).
 * Mirrors the iOS `ApiClient` cookie persistence (UserDefaults key `Cookies`).
 * Backed by [android.content.SharedPreferences] because [okhttp3.CookieJar] is synchronous.
 */
class SessionCookieStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(name: String): String? = prefs.getString(name, null)

    fun put(name: String, value: String) {
        prefs.edit().putString(name, value).apply()
    }

    fun remove(name: String) {
        prefs.edit().remove(name).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "session_cookies"
    }
}
