package nl.parkeerassistent.amsterdam.ui.parkingmeter

import nl.parkeerassistent.amsterdam.FakeGeoRepository
import nl.parkeerassistent.amsterdam.FakeStringProvider
import nl.parkeerassistent.amsterdam.MainDispatcherRule
import nl.parkeerassistent.amsterdam.data.model.ParkingMeter
import nl.parkeerassistent.amsterdam.data.model.ParkingMeterType
import nl.parkeerassistent.amsterdam.data.remote.ApiException
import nl.parkeerassistent.amsterdam.ui.common.ApiErrorHandler
import nl.parkeerassistent.amsterdam.ui.common.MessageBus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ParkingMeterViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val repo = FakeGeoRepository()
    private val errorHandler = ApiErrorHandler(MessageBus(), FakeStringProvider())

    private fun viewModel() = ParkingMeterViewModel(repo, errorHandler)

    @Test fun `fetchNearby publishes the meters`() {
        repo.nearby = listOf(
            ParkingMeter(5, 1, "Nieuwmarkt", ParkingMeterType.METER, 52.3, 4.9, 80.0),
        )
        val vm = viewModel()

        vm.fetchNearby(52.3, 4.9)

        assertEquals(1, vm.meters.value.size)
        assertEquals("Nieuwmarkt", vm.meters.value.first().name)
    }

    @Test fun `fetchNearby on error keeps the list empty`() {
        repo.throwOnNearby = ApiException.ServerError("boom")
        val vm = viewModel()

        vm.fetchNearby(52.3, 4.9)

        assertTrue(vm.meters.value.isEmpty())
    }
}
