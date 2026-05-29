package nl.parkeerassistent.amsterdam.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.parkeerassistent.amsterdam.data.repository.PaymentRepository
import nl.parkeerassistent.amsterdam.stats.StatsStore
import nl.parkeerassistent.amsterdam.ui.common.ApiErrorHandler
import nl.parkeerassistent.amsterdam.util.Log

/**
 * Port of iOS `PaymentStore`. Creates a payment and hands the returned URL to [onSuccess]
 * (the screen opens it in a Custom Tab); the user completes payment externally and returns to
 * the app on their own, at which point the balance should be refreshed.
 */
class PaymentViewModel(
    private val paymentRepository: PaymentRepository,
    private val errorHandler: ApiErrorHandler,
    private val stats: StatsStore,
) : ViewModel() {

    private val _isPaymentInProgress = MutableStateFlow(false)
    val isPaymentInProgress: StateFlow<Boolean> = _isPaymentInProgress.asStateFlow()

    fun payment(amount: Long, brand: String, lang: String, onSuccess: (url: String) -> Unit) {
        viewModelScope.launch {
            val response = try {
                paymentRepository.createPayment(amount, brand, lang)
            } catch (e: Exception) {
                Log.error("payment failed", e)
                errorHandler.handle(e)
                return@launch
            }
            stats.incrementPayment()
            _isPaymentInProgress.value = true
            onSuccess(response.url)
        }
    }
}
