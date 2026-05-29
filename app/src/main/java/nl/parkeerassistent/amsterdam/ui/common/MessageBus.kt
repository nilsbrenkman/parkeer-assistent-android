package nl.parkeerassistent.amsterdam.ui.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class MessageType { SUCCESS, INFO, WARN, ERROR }

data class Message(val text: String, val type: MessageType)

/**
 * App-wide message channel (port of iOS `MessageStore`). A single instance is shared via Koin;
 * the root composable collects [messages] and shows a Snackbar (Phase 6). Emitting events rather
 * than holding one current message is the idiomatic Compose equivalent.
 *
 * Note: iOS messages could carry an `ok` confirmation callback; confirmation dialogs are handled
 * separately in the UI layer (Phase 6) instead.
 */
class MessageBus {

    private val _messages = MutableSharedFlow<Message>(extraBufferCapacity = 8)
    val messages: SharedFlow<Message> = _messages.asSharedFlow()

    fun show(text: String?, type: MessageType) {
        if (text.isNullOrBlank()) return
        _messages.tryEmit(Message(text, type))
    }
}
