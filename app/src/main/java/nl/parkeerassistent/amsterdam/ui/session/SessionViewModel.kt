package nl.parkeerassistent.amsterdam.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.parkeerassistent.amsterdam.data.local.CredentialStore
import nl.parkeerassistent.amsterdam.data.remote.ApiException
import nl.parkeerassistent.amsterdam.stats.StatsStore
import nl.parkeerassistent.amsterdam.data.repository.LoginRepository
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.ui.common.ApiErrorHandler
import nl.parkeerassistent.amsterdam.ui.common.MessageBus
import nl.parkeerassistent.amsterdam.ui.common.MessageType
import nl.parkeerassistent.amsterdam.util.Log
import nl.parkeerassistent.amsterdam.util.StringProvider

data class SessionUiState(
    val isLoading: Boolean = true,
    val isBackground: Boolean = false,
    val isLoggedIn: Boolean = false,
)

/**
 * Port of iOS `SessionStore`. Owns the root auth state and reacts to 401/403 from any call
 * (via [ApiErrorHandler.unauthorized]) by dropping the session.
 */
class SessionViewModel(
    private val loginRepository: LoginRepository,
    private val errorHandler: ApiErrorHandler,
    private val messageBus: MessageBus,
    private val credentialStore: CredentialStore,
    private val strings: StringProvider,
    private val stats: StatsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(SessionUiState())
    val state: StateFlow<SessionUiState> = _state.asStateFlow()

    /** Mirrors iOS `autoLogin`; the login screen consumes it once to auto-login on launch. */
    private var autoLogin: Boolean = true

    /** Returns whether an auto-login should be attempted, then disarms it (one-shot). */
    fun consumeAutoLogin(): Boolean {
        val value = autoLogin
        autoLogin = false
        return value
    }

    init {
        viewModelScope.launch {
            errorHandler.unauthorized.collect { onUnauthorized() }
        }
    }

    /** Initial / return-from-background check against `GET login`. */
    fun checkLoggedIn() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val loggedIn = try {
                loginRepository.isLoggedIn().success
            } catch (e: Exception) {
                Log.error("loggedIn check failed", e)
                false
            }
            _state.update { it.copy(isLoggedIn = loggedIn, isLoading = false) }
        }
    }

    fun login(username: String, password: String, storeCredentials: Boolean) {
        viewModelScope.launch {
            val response = try {
                loginRepository.login(username, password)
            } catch (e: ApiException.Unauthorized) {
                messageBus.show(strings.get(R.string.login_failed), MessageType.WARN)
                return@launch
            } catch (e: ApiException.ServerError) {
                messageBus.show(e.serverMessage, MessageType.ERROR)
                return@launch
            } catch (e: Exception) {
                Log.error("login failed", e)
                messageBus.show(strings.get(R.string.login_error), MessageType.ERROR)
                return@launch
            }

            if (response.success) {
                if (storeCredentials) {
                    credentialStore.store(username, password, alias = null)
                    credentialStore.recent = username
                }
                stats.incrementLogin()
                _state.update { it.copy(isLoggedIn = true) }
            } else {
                messageBus.show(response.message, MessageType.ERROR)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val response = loginRepository.logout()
                if (!response.success) messageBus.show(response.message, MessageType.ERROR)
            } catch (e: Exception) {
                Log.error("logout failed", e)
            }
            clearUser()
        }
    }

    fun onEnterBackground() {
        _state.update { it.copy(isBackground = true) }
    }

    fun onReturnFromBackground() {
        if (_state.value.isBackground) {
            _state.update { it.copy(isBackground = false) }
            checkLoggedIn()
        }
    }

    private fun onUnauthorized() {
        if (_state.value.isLoggedIn) {
            messageBus.show(strings.get(R.string.error_unauthorized), MessageType.WARN)
        }
        clearUser()
    }

    private fun clearUser() {
        autoLogin = false
        _state.update { it.copy(isLoggedIn = false, isLoading = false, isBackground = false) }
    }
}
