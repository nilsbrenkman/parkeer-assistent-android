package nl.parkeerassistent.amsterdam.ui.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.data.remote.ApiException
import nl.parkeerassistent.amsterdam.util.StringProvider

/**
 * Central error mapping, the counterpart to iOS `SessionStore` acting as the `ApiClient`
 * ErrorHandler. ViewModels call [handle] from their catch blocks: server errors surface on the
 * [MessageBus], and 401/403 emit on [unauthorized] so the SessionViewModel can drop the session.
 */
class ApiErrorHandler(
    private val messageBus: MessageBus,
    private val strings: StringProvider,
) {

    private val _unauthorized = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorized: SharedFlow<Unit> = _unauthorized.asSharedFlow()

    fun handle(error: Throwable) {
        when (error) {
            is ApiException.Unauthorized -> _unauthorized.tryEmit(Unit)
            is ApiException.ServerError -> messageBus.show(error.serverMessage, MessageType.ERROR)
            else -> messageBus.show(strings.get(R.string.error_server_unknown), MessageType.ERROR)
        }
    }
}
