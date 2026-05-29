package nl.parkeerassistent.amsterdam.ui.parking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.parkeerassistent.amsterdam.data.model.AddParkingRequest
import nl.parkeerassistent.amsterdam.data.model.Parking
import nl.parkeerassistent.amsterdam.data.model.ParkingResponse
import nl.parkeerassistent.amsterdam.data.model.Visitor
import nl.parkeerassistent.amsterdam.data.repository.ParkingRepository
import nl.parkeerassistent.amsterdam.notifications.ParkingNotifications
import nl.parkeerassistent.amsterdam.stats.StatsStore
import nl.parkeerassistent.amsterdam.ui.common.ApiErrorHandler
import nl.parkeerassistent.amsterdam.ui.common.MessageBus
import nl.parkeerassistent.amsterdam.ui.common.MessageType
import nl.parkeerassistent.amsterdam.util.DateUtil
import nl.parkeerassistent.amsterdam.util.Log
import java.time.OffsetDateTime

/**
 * Port of iOS `ParkingStore`. Unlike iOS (which is handed the `UserStore`), this VM stays
 * decoupled: the caller supplies product/zone/meter ids and refreshes the user balance after a
 * successful start/stop via [onSuccess] (orchestrated by the screen in Phase 6).
 */
class ParkingViewModel(
    private val parkingRepository: ParkingRepository,
    private val errorHandler: ApiErrorHandler,
    private val messageBus: MessageBus,
    private val notifications: ParkingNotifications,
    private val stats: StatsStore,
) : ViewModel() {

    private val _parking = MutableStateFlow<ParkingResponse?>(null)
    val parking: StateFlow<ParkingResponse?> = _parking.asStateFlow()

    private val _history = MutableStateFlow<List<Parking>?>(null)
    val history: StateFlow<List<Parking>?> = _history.asStateFlow()

    fun getParking() {
        viewModelScope.launch {
            val response = try {
                parkingRepository.getParking()
            } catch (e: Exception) {
                Log.error("getParking failed", e)
                errorHandler.handle(e)
                return@launch
            }
            notifications.onParking(response)
            if (response != _parking.value) _parking.value = response
        }
    }

    fun getHistory() {
        viewModelScope.launch {
            try {
                _history.value = parkingRepository.getHistory()
            } catch (e: Exception) {
                Log.error("getHistory failed", e)
                errorHandler.handle(e)
            }
        }
    }

    fun startParking(
        visitor: Visitor,
        timeMinutes: Int,
        start: OffsetDateTime?,
        productId: Long,
        zoneId: Long,
        parkingMeterId: Long,
        onSuccess: (() -> Unit)? = null,
    ) {
        viewModelScope.launch {
            val request = AddParkingRequest(
                license = visitor.license,
                timeMinutes = timeMinutes,
                start = start?.let { DateUtil.toWire(it) },
                productId = productId,
                zoneId = zoneId,
                parkingMeterId = parkingMeterId,
            )
            val response = try {
                parkingRepository.addParking(request)
            } catch (e: Exception) {
                Log.error("startParking failed", e)
                errorHandler.handle(e)
                return@launch
            }
            if (response.success) {
                stats.incrementParking()
                _parking.value = null
                getParking()
                onSuccess?.invoke()
            } else {
                messageBus.show(response.message, MessageType.ERROR)
            }
        }
    }

    fun stopParking(parking: Parking, onSuccess: (() -> Unit)? = null) {
        // Optimistically remove from the current list (mirrors iOS).
        _parking.value?.let { current ->
            _parking.value = current.copy(
                active = current.active.filterNot { it.id == parking.id },
                scheduled = current.scheduled.filterNot { it.id == parking.id },
            )
        }
        viewModelScope.launch {
            try {
                val response = parkingRepository.stopParking(parking.id)
                if (!response.success) messageBus.show(response.message, MessageType.ERROR)
            } catch (e: Exception) {
                Log.error("stopParking failed", e)
                errorHandler.handle(e)
                return@launch
            }
            getParking()
            onSuccess?.invoke()
        }
    }
}
