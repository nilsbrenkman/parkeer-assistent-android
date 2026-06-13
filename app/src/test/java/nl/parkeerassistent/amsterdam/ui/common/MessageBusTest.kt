package nl.parkeerassistent.amsterdam.ui.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessageBusTest {

    @Test fun `show emits a message with its type`() = runTest {
        val bus = MessageBus()
        val received = mutableListOf<Message>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { bus.messages.toList(received) }

        bus.show("saved", MessageType.SUCCESS)

        assertEquals(listOf(Message("saved", MessageType.SUCCESS)), received)
        job.cancel()
    }

    @Test fun `show ignores null and blank text`() = runTest {
        val bus = MessageBus()
        val received = mutableListOf<Message>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { bus.messages.toList(received) }

        bus.show(null, MessageType.ERROR)
        bus.show("   ", MessageType.ERROR)
        bus.show("real", MessageType.INFO)

        assertEquals(listOf(Message("real", MessageType.INFO)), received)
        job.cancel()
    }
}
