package nl.parkeerassistent.amsterdam.ui.account

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import nl.parkeerassistent.amsterdam.data.local.CredentialStore
import nl.parkeerassistent.amsterdam.data.model.Credentials

/**
 * Port of iOS `AccountStore`. The biometric prompt itself lives in the screen layer (it needs the
 * Activity); this VM tracks whether auth is still valid (5-minute window) and exposes the list +
 * CRUD over [CredentialStore].
 */
class AccountViewModel(private val store: CredentialStore) : ViewModel() {

    private val _accounts = MutableStateFlow<List<Credentials>>(emptyList())
    val accounts: StateFlow<List<Credentials>> = _accounts.asStateFlow()

    private var authenticatedAt: Long? = null

    fun isAuthenticated(): Boolean {
        val at = authenticatedAt ?: return false
        return System.currentTimeMillis() - at < AUTH_VALID_MS
    }

    /** Called after a successful biometric prompt. */
    fun onAuthenticated() {
        authenticatedAt = System.currentTimeMillis()
        reload()
    }

    private fun reload() {
        _accounts.value = store.retrieve()
    }

    fun account(username: String): Credentials? = _accounts.value.firstOrNull { it.username == username }

    fun addAccount(username: String, password: String, alias: String?) {
        store.store(username, password, alias?.ifBlank { null })
        reload()
    }

    fun updateAccount(account: Credentials, username: String, password: String, alias: String?) {
        store.update(account, username, password, alias?.ifBlank { null })
        if (store.recent == account.username) store.recent = username
        reload()
    }

    fun deleteAccount(account: Credentials) {
        store.delete(account)
        if (store.recent == account.username) store.recent = _accounts.value.firstOrNull()?.username
        reload()
    }

    var autoLogin: Boolean
        get() = store.autoLoginEnabled
        set(value) { store.autoLoginEnabled = value }

    private companion object {
        const val AUTH_VALID_MS = 5 * 60 * 1000L
    }
}
