package nl.parkeerassistent.amsterdam.ui.parkingmeter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.parkeerassistent.amsterdam.data.model.ParkingMeter
import nl.parkeerassistent.amsterdam.data.repository.GeoRepository
import nl.parkeerassistent.amsterdam.ui.common.ApiErrorHandler
import nl.parkeerassistent.amsterdam.util.Log

/** Port of iOS `ParkingMeterStore`: nearby parking meters for the picker. */
class ParkingMeterViewModel(
    private val geoRepository: GeoRepository,
    private val errorHandler: ApiErrorHandler,
) : ViewModel() {

    private val _meters = MutableStateFlow<List<ParkingMeter>>(emptyList())
    val meters: StateFlow<List<ParkingMeter>> = _meters.asStateFlow()

    fun fetchNearby(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                _meters.value = geoRepository.parkingMetersNearby(lat, lon)
            } catch (e: Exception) {
                Log.error("fetchNearby failed", e)
                errorHandler.handle(e)
            }
        }
    }
}
