package nl.parkeerassistent.amsterdam.ui.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.parkeerassistent.amsterdam.FakeStringProvider
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.data.remote.ApiException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ApiErrorHandlerTest {

    @Test fun `unauthorized emits on the unauthorized flow and not on the message bus`() = runTest {
        val bus = MessageBus()
        val handler = ApiErrorHandler(bus, FakeStringProvider())
        val messages = mutableListOf<Message>()
        val unauthorized = mutableListOf<Unit>()
        val m = launch(UnconfinedTestDispatcher(testScheduler)) { bus.messages.toList(messages) }
        val u = launch(UnconfinedTestDispatcher(testScheduler)) { handler.unauthorized.toList(unauthorized) }

        handler.handle(ApiException.Unauthorized())

        assertEquals(1, unauthorized.size)
        assertTrue(messages.isEmpty())
        m.cancel(); u.cancel()
    }

    @Test fun `server error surfaces its message on the bus`() = runTest {
        val bus = MessageBus()
        val handler = ApiErrorHandler(bus, FakeStringProvider())
        val messages = mutableListOf<Message>()
        val m = launch(UnconfinedTestDispatcher(testScheduler)) { bus.messages.toList(messages) }

        handler.handle(ApiException.ServerError("upstream down"))

        assertEquals(listOf(Message("upstream down", MessageType.ERROR)), messages)
        m.cancel()
    }

    @Test fun `unknown error falls back to the generic string resource`() = runTest {
        val bus = MessageBus()
        val handler = ApiErrorHandler(bus, FakeStringProvider())
        val messages = mutableListOf<Message>()
        val m = launch(UnconfinedTestDispatcher(testScheduler)) { bus.messages.toList(messages) }

        handler.handle(RuntimeException("anything"))

        // FakeStringProvider renders a resource id as "res:<id>".
        assertEquals(listOf(Message("res:${R.string.error_server_unknown}", MessageType.ERROR)), messages)
        m.cancel()
    }
}
