package nl.parkeerassistent.amsterdam.ui.user

import nl.parkeerassistent.amsterdam.FakeStringProvider
import nl.parkeerassistent.amsterdam.FakeUserRepository
import nl.parkeerassistent.amsterdam.MainDispatcherRule
import nl.parkeerassistent.amsterdam.data.model.BalanceResponse
import nl.parkeerassistent.amsterdam.data.model.Regime
import nl.parkeerassistent.amsterdam.data.model.RegimeDay
import nl.parkeerassistent.amsterdam.data.model.UserResponse
import nl.parkeerassistent.amsterdam.ui.common.ApiErrorHandler
import nl.parkeerassistent.amsterdam.ui.common.MessageBus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class UserViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val repo = FakeUserRepository()
    private val errorHandler = ApiErrorHandler(MessageBus(), FakeStringProvider())

    private fun viewModel() = UserViewModel(repo, errorHandler, MessageBus(), FakeStringProvider())

    @Test fun `getUser populates state and the initial time balance`() {
        repo.userResponse = UserResponse(
            balance = "20.00", hourRate = 2.40, productId = 1, zoneId = 2, parkingMeterId = 3,
            regime = Regime(listOf(RegimeDay("MON", "09:00", "21:00"))),
        )
        val vm = viewModel()

        vm.getUser()

        val state = vm.state.value
        assertEquals("20.00", state.balance)
        assertEquals(2.40, state.hourRate!!, 0.0001)
        assertEquals(3L, state.parkingMeterId)
        assertTrue(state.isLoaded)
        // iOS uses a fixed 0.01 €/min for the initial estimate: 20 / 0.01 * 60.
        assertEquals(120_000, state.timeBalance)
    }

    @Test fun `getBalance recomputes the time balance from the hour rate`() {
        repo.userResponse = UserResponse("20.00", 2.40, 1, 2, 3, Regime(emptyList()))
        val vm = viewModel()
        vm.getUser()

        repo.balanceResponse = BalanceResponse("12.00")
        vm.getBalance()

        assertEquals("12.00", vm.state.value.balance)
        assertEquals(300, vm.state.value.timeBalance) // 12 / 2.40 * 60
    }
}
