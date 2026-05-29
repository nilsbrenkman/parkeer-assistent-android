package nl.parkeerassistent.amsterdam.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import nl.parkeerassistent.amsterdam.data.model.Credentials
import nl.parkeerassistent.amsterdam.util.Log

/**
 * Android counterpart of the iOS `Keychain`: stores the saved-credentials list in
 * EncryptedSharedPreferences (AES via the Android Keystore), plus the "recent" username and the
 * auto-login flag. Upsert/update/delete semantics mirror the iOS implementation (keyed by username).
 */
class CredentialStore(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(Credentials.serializer())

    fun retrieve(): List<Credentials> {
        val raw = prefs.getString(KEY_LIST, null) ?: return emptyList()
        return runCatching { json.decodeFromString(serializer, raw) }
            .onFailure { Log.warning("CredentialStore decode failed: ${it.message}") }
            .getOrDefault(emptyList())
    }

    private fun persist(list: List<Credentials>) {
        prefs.edit().putString(KEY_LIST, json.encodeToString(serializer, list)).apply()
    }

    fun store(username: String, password: String, alias: String?) {
        val list = retrieve().toMutableList()
        val i = list.indexOfFirst { it.username == username }
        if (i >= 0) {
            if (list[i].password != password) list[i] = Credentials(alias, username, password)
        } else {
            list.add(Credentials(alias, username, password))
        }
        persist(list)
    }

    fun update(account: Credentials, username: String, password: String, alias: String?) {
        val list = retrieve().toMutableList()
        val i = list.indexOfFirst { it.username == account.username }
        if (i >= 0) {
            list[i] = Credentials(alias, username, password)
            persist(list)
        }
    }

    fun delete(account: Credentials) {
        val list = retrieve().toMutableList()
        if (list.removeAll { it.username == account.username }) persist(list)
    }

    var recent: String?
        get() = prefs.getString(KEY_RECENT, null)
        set(value) = prefs.edit().putString(KEY_RECENT, value).apply()

    /** Mirrors iOS `Keychain.autoLogin()` (stored as a "disabled" flag, defaulting to enabled). */
    var autoLoginEnabled: Boolean
        get() = !prefs.getBoolean(KEY_AUTO_LOGIN_DISABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_LOGIN_DISABLED, !value).apply()

    private companion object {
        const val FILE_NAME = "credentials"
        const val KEY_LIST = "list"
        const val KEY_RECENT = "recent"
        const val KEY_AUTO_LOGIN_DISABLED = "autoLoginDisabled"
    }
}
