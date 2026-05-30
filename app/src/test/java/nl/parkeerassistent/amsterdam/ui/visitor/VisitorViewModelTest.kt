package nl.parkeerassistent.amsterdam.ui.visitor

import nl.parkeerassistent.amsterdam.FakeParkingNotifications
import nl.parkeerassistent.amsterdam.FakeStatsStore
import nl.parkeerassistent.amsterdam.FakeStringProvider
import nl.parkeerassistent.amsterdam.FakeVisitorRepository
import nl.parkeerassistent.amsterdam.MainDispatcherRule
import nl.parkeerassistent.amsterdam.data.model.Response
import nl.parkeerassistent.amsterdam.data.model.Visitor
import nl.parkeerassistent.amsterdam.ui.common.ApiErrorHandler
import nl.parkeerassistent.amsterdam.ui.common.MessageBus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class VisitorViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val repo = FakeVisitorRepository()
    private val notifications = FakeParkingNotifications()
    private val stats = FakeStatsStore()
    private val errorHandler = ApiErrorHandler(MessageBus(), FakeStringProvider())

    private fun viewModel() = VisitorViewModel(repo, errorHandler, MessageBus(), notifications, stats)

    @Test fun `getVisitors sorts named first then by license, and feeds notifications`() {
        repo.visitors = listOf(
            Visitor(1, "BBB", "B", null),
            Visitor(2, "AAA", "A", "Zoe"),
            Visitor(3, "CCC", "C", "Anna"),
            Visitor(4, "DDD", "D", null),
        )
        val vm = viewModel()

        vm.getVisitors()

        val result = vm.visitors.value!!
        assertEquals(listOf("Anna", "Zoe", null, null), result.map { it.name })
        assertEquals(listOf("CCC", "AAA", "BBB", "DDD"), result.map { it.license })
        assertEquals(result, notifications.visitors)
    }

    @Test fun `addVisitor on success bumps stats, calls onSuccess, and refreshes`() {
        repo.addResult = Response(true)
        repo.visitors = listOf(Visitor(1, "AAA", "A", "X"))
        var onSuccessCalled = false
        val vm = viewModel()

        vm.addVisitor("AAA", "X") { onSuccessCalled = true }

        assertTrue(onSuccessCalled)
        assertEquals(1, stats.visitorCount)
        assertEquals(1, vm.visitors.value?.size)
    }

    @Test fun `getName matches the license case-insensitively`() {
        repo.visitors = listOf(Visitor(1, "12ABC3", "12-ABC-3", "Jan"))
        val vm = viewModel()
        vm.getVisitors()

        assertEquals("Jan", vm.getName("12abc3"))
        assertEquals("", vm.getName("unknown"))
    }
}
