package nl.parkeerassistent.amsterdam.ui.payment

import nl.parkeerassistent.amsterdam.FakePaymentRepository
import nl.parkeerassistent.amsterdam.FakeStatsStore
import nl.parkeerassistent.amsterdam.FakeStringProvider
import nl.parkeerassistent.amsterdam.MainDispatcherRule
import nl.parkeerassistent.amsterdam.data.model.PaymentResponse
import nl.parkeerassistent.amsterdam.data.remote.ApiException
import nl.parkeerassistent.amsterdam.ui.common.ApiErrorHandler
import nl.parkeerassistent.amsterdam.ui.common.MessageBus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PaymentViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val repo = FakePaymentRepository()
    private val stats = FakeStatsStore()
    private val errorHandler = ApiErrorHandler(MessageBus(), FakeStringProvider())

    private fun viewModel() = PaymentViewModel(repo, errorHandler, stats)

    @Test fun `payment forwards args, bumps stats, marks in-progress and hands back the url`() {
        repo.response = PaymentResponse("https://pay.example/ideal")
        var url: String? = null
        val vm = viewModel()

        vm.payment(amount = 1000, brand = "IDEAL", lang = "nl") { url = it }

        assertEquals("https://pay.example/ideal", url)
        assertEquals(1, stats.paymentCount)
        assertTrue(vm.isPaymentInProgress.value)
        assertEquals(Triple(1000L, "IDEAL", "nl"), repo.requests.single())
    }

    @Test fun `payment failure does not bump stats, stays not in-progress, no callback`() {
        repo.throwOnCreate = ApiException.ServerError("declined")
        var url: String? = null
        val vm = viewModel()

        vm.payment(amount = 500, brand = "IDEAL", lang = "en") { url = it }

        assertNull(url)
        assertEquals(0, stats.paymentCount)
        assertFalse(vm.isPaymentInProgress.value)
    }
}
