package nl.parkeerassistent.amsterdam.ui.parking

import nl.parkeerassistent.amsterdam.FakeParkingNotifications
import nl.parkeerassistent.amsterdam.FakeParkingRepository
import nl.parkeerassistent.amsterdam.FakeStatsStore
import nl.parkeerassistent.amsterdam.FakeStringProvider
import nl.parkeerassistent.amsterdam.MainDispatcherRule
import nl.parkeerassistent.amsterdam.data.model.Parking
import nl.parkeerassistent.amsterdam.data.model.ParkingResponse
import nl.parkeerassistent.amsterdam.data.model.Response
import nl.parkeerassistent.amsterdam.data.model.Visitor
import nl.parkeerassistent.amsterdam.data.remote.ApiException
import nl.parkeerassistent.amsterdam.ui.common.ApiErrorHandler
import nl.parkeerassistent.amsterdam.ui.common.MessageBus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ParkingViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val repo = FakeParkingRepository()
    private val notifications = FakeParkingNotifications()
    private val stats = FakeStatsStore()
    private val errorHandler = ApiErrorHandler(MessageBus(), FakeStringProvider())

    private fun viewModel() = ParkingViewModel(repo, errorHandler, MessageBus(), notifications, stats)

    private fun parking(id: Long) = Parking(id, "AA$id", null, "s", "e", 1.0)

    @Test fun `getParking publishes the response and feeds notifications`() {
        repo.parkingResponse = ParkingResponse(listOf(parking(1)), listOf(parking(2)))
        val vm = viewModel()

        vm.getParking()

        assertEquals(repo.parkingResponse, vm.parking.value)
        assertEquals(repo.parkingResponse, notifications.lastParking)
    }

    @Test fun `getParking on error leaves parking null`() {
        repo.throwOnGetParking = ApiException.ServerError("boom")
        val vm = viewModel()

        vm.getParking()

        assertNull(vm.parking.value)
    }

    @Test fun `getHistory populates the history list`() {
        repo.history = listOf(parking(1), parking(2))
        val vm = viewModel()

        vm.getHistory()

        assertEquals(2, vm.history.value?.size)
    }

    @Test fun `startParking sends a request, bumps stats, refreshes and calls onSuccess`() {
        repo.addResult = Response(true)
        repo.parkingResponse = ParkingResponse(listOf(parking(1)), emptyList())
        var onSuccessCalled = false
        val vm = viewModel()

        vm.startParking(
            visitor = Visitor(1, "12ABC3", "12-ABC-3", "Jan"),
            timeMinutes = 60,
            start = null,
            productId = 10,
            zoneId = 20,
            parkingMeterId = 30,
        ) { onSuccessCalled = true }

        assertTrue(onSuccessCalled)
        assertEquals(1, stats.parkingCount)
        assertEquals(1, repo.addRequests.size)
        val request = repo.addRequests.first()
        assertEquals("12ABC3", request.license)
        assertEquals(60, request.timeMinutes)
        assertEquals(30L, request.parkingMeterId)
        assertNull(request.start)
        // _parking was refreshed via getParking()
        assertEquals(repo.parkingResponse, vm.parking.value)
    }

    @Test fun `startParking failure does not bump stats nor call onSuccess`() {
        repo.addResult = Response(false, "no funds")
        var onSuccessCalled = false
        val vm = viewModel()

        vm.startParking(
            visitor = Visitor(1, "AA", "AA", null),
            timeMinutes = 30,
            start = null,
            productId = 1,
            zoneId = 2,
            parkingMeterId = 3,
        ) { onSuccessCalled = true }

        assertTrue(!onSuccessCalled)
        assertEquals(0, stats.parkingCount)
    }

    @Test fun `stopParking optimistically removes the row then refreshes`() {
        val active = parking(1)
        repo.parkingResponse = ParkingResponse(listOf(active), emptyList())
        val vm = viewModel()
        vm.getParking()
        assertEquals(1, vm.parking.value?.active?.size)

        // After stop, the server returns an empty active list.
        repo.parkingResponse = ParkingResponse(emptyList(), emptyList())
        var onSuccessCalled = false
        vm.stopParking(active) { onSuccessCalled = true }

        assertTrue(onSuccessCalled)
        assertEquals(listOf(1L), repo.stoppedIds)
        assertEquals(0, vm.parking.value?.active?.size)
    }
}
