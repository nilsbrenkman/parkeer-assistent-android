package nl.parkeerassistent.amsterdam.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.parkeerassistent.amsterdam.R
import nl.parkeerassistent.amsterdam.data.model.Regime
import nl.parkeerassistent.amsterdam.data.repository.UserRepository
import nl.parkeerassistent.amsterdam.ui.common.ApiErrorHandler
import nl.parkeerassistent.amsterdam.ui.common.MessageBus
import nl.parkeerassistent.amsterdam.ui.common.MessageType
import nl.parkeerassistent.amsterdam.util.DateUtil
import nl.parkeerassistent.amsterdam.util.Log
import nl.parkeerassistent.amsterdam.util.StringProvider
import java.time.LocalDate
import java.time.LocalTime

data class UserUiState(
    val balance: String? = null,
    val hourRate: Double? = null,
    val timeBalance: Int = 0,
    val regime: Regime? = null,
    val regimeTimeStart: LocalTime? = null,
    val regimeTimeEnd: LocalTime? = null,
    val productId: Long? = null,
    val zoneId: Long? = null,
    val parkingMeterId: Long? = null,
    val isLoaded: Boolean = false,
)

/** Port of iOS `UserStore`. */
class UserViewModel(
    private val userRepository: UserRepository,
    private val errorHandler: ApiErrorHandler,
    private val messageBus: MessageBus,
    private val strings: StringProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(UserUiState())
    val state: StateFlow<UserUiState> = _state.asStateFlow()

    fun reset() {
        _state.value = UserUiState()
    }

    fun getUser(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val response = try {
                userRepository.getUser()
            } catch (e: Exception) {
                Log.error("getUser failed", e)
                errorHandler.handle(e)
                return@launch
            }
            _state.update {
                it.copy(
                    balance = response.balance,
                    hourRate = response.hourRate,
                    regime = response.regime,
                    productId = response.productId,
                    zoneId = response.zoneId,
                    parkingMeterId = response.parkingMeterId,
                    // iOS uses a fixed 0.01 €/min here for the initial estimate.
                    timeBalance = DateUtil.calculateTimeBalance(response.balance, 0.01),
                    isLoaded = true,
                )
            }
            setRegimeForDate(LocalDate.now())
            onComplete?.invoke()
        }
    }

    fun getBalance() {
        viewModelScope.launch {
            val response = try {
                userRepository.getBalance()
            } catch (e: Exception) {
                Log.error("getBalance failed", e)
                errorHandler.handle(e)
                return@launch
            }
            if (response.balance == _state.value.balance) return@launch
            _state.update {
                it.copy(
                    balance = response.balance,
                    timeBalance = DateUtil.calculateTimeBalance(response.balance, it.hourRate),
                )
            }
        }
    }

    /** Recomputes the regime window for [date]; no network (port of iOS `getRegime`). */
    fun getRegime(date: LocalDate) {
        if (_state.value.regime != null) setRegimeForDate(date)
    }

    fun setRegimeForDate(date: LocalDate) {
        val regime = _state.value.regime
        val day = regime?.let { DateUtil.getRegimeDay(it, date) }
        if (regime == null || day == null) {
            _state.update { it.copy(regimeTimeStart = LocalTime.MIDNIGHT, regimeTimeEnd = LocalTime.MIDNIGHT) }
            if (regime != null) messageBus.show(strings.get(R.string.parking_free_parking), MessageType.WARN)
            return
        }
        _state.update {
            it.copy(
                regimeTimeStart = DateUtil.parseTime(day.startTime),
                regimeTimeEnd = DateUtil.parseTime(day.endTime),
            )
        }
    }

    fun setParkingMeter(parkingMeterId: Long) {
        _state.update { it.copy(parkingMeterId = parkingMeterId) }
        viewModelScope.launch {
            val response = try {
                userRepository.getRegime(parkingMeterId)
            } catch (e: Exception) {
                Log.error("setParkingMeter regime fetch failed", e)
                messageBus.show(strings.get(R.string.parking_invalid_zone), MessageType.ERROR)
                return@launch
            }
            _state.update {
                it.copy(hourRate = response.hourRate, zoneId = response.zoneId, regime = response.regime)
            }
        }
    }
}
