package nl.parkeerassistent.amsterdam.data.remote.cookie

import android.content.Context
import androidx.core.content.edit

/**
 * Synchronous persistence for the two session cookies (`token`, `product_id`).
 * Mirrors the iOS `ApiClient` cookie persistence (UserDefaults key `Cookies`).
 *
 * Extracted as an interface so [SessionCookieJar]'s whitelist/expiry logic is fakeable on plain
 * JVM (the same testability pattern as `StringProvider`/`CredentialStore`/`StatsStore`).
 */
interface SessionCookieStore {
    fun get(name: String): String?
    fun put(name: String, value: String)
    fun remove(name: String)
    fun clear()
}

/**
 * The production [SessionCookieStore], backed by [android.content.SharedPreferences] because
 * [okhttp3.CookieJar] is synchronous.
 */
class PrefsSessionCookieStore(context: Context) : SessionCookieStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun get(name: String): String? = prefs.getString(name, null)

    override fun put(name: String, value: String) {
        prefs.edit { putString(name, value) }
    }

    override fun remove(name: String) {
        prefs.edit { remove(name) }
    }

    override fun clear() {
        prefs.edit { clear() }
    }

    private companion object {
        const val PREFS_NAME = "session_cookies"
    }
}
