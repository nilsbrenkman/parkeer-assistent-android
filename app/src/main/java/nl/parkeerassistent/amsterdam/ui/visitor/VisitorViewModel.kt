package nl.parkeerassistent.amsterdam.ui.visitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.parkeerassistent.amsterdam.data.model.Visitor
import nl.parkeerassistent.amsterdam.data.repository.VisitorRepository
import nl.parkeerassistent.amsterdam.notifications.ParkingNotifications
import nl.parkeerassistent.amsterdam.stats.StatsStore
import nl.parkeerassistent.amsterdam.ui.common.ApiErrorHandler
import nl.parkeerassistent.amsterdam.ui.common.MessageBus
import nl.parkeerassistent.amsterdam.ui.common.MessageType
import nl.parkeerassistent.amsterdam.util.Log
import nl.parkeerassistent.amsterdam.util.VisitorNameCache

/** Port of iOS `VisitorStore`. `null` means "not yet loaded". */
class VisitorViewModel(
    private val visitorRepository: VisitorRepository,
    private val errorHandler: ApiErrorHandler,
    private val messageBus: MessageBus,
    private val notifications: ParkingNotifications,
    private val stats: StatsStore,
) : ViewModel() {

    private val _visitors = MutableStateFlow<List<Visitor>?>(null)
    val visitors: StateFlow<List<Visitor>?> = _visitors.asStateFlow()

    fun getVisitors() {
        viewModelScope.launch {
            val list = try {
                visitorRepository.getVisitors()
            } catch (e: Exception) {
                Log.error("getVisitors failed", e)
                errorHandler.handle(e)
                return@launch
            }
            val sorted = list.sortedWith(VISITOR_ORDER)
            if (sorted == _visitors.value) return@launch
            _visitors.value = sorted
            notifications.visitors = sorted
            VisitorNameCache.map.clear()
            sorted.forEach { VisitorNameCache.map[it.license] = it.name ?: "" }
        }
    }

    fun addVisitor(license: String, name: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val response = try {
                visitorRepository.addVisitor(license, name)
            } catch (e: Exception) {
                Log.error("addVisitor failed", e)
                errorHandler.handle(e)
                return@launch
            }
            if (response.success) {
                onSuccess?.invoke()
                stats.incrementVisitor()
                _visitors.value = null
                getVisitors()
            } else {
                messageBus.show(response.message, MessageType.ERROR)
            }
        }
    }

    fun deleteVisitor(visitor: Visitor) {
        viewModelScope.launch {
            try {
                val response = visitorRepository.deleteVisitor(visitor.id)
                if (!response.success) messageBus.show(response.message, MessageType.ERROR)
            } catch (e: Exception) {
                Log.error("deleteVisitor failed", e)
                errorHandler.handle(e)
                return@launch
            }
            getVisitors()
        }
    }

    fun getName(license: String): String =
        _visitors.value
            ?.firstOrNull { it.license.equals(license, ignoreCase = true) }
            ?.name
            ?: ""

    private companion object {
        // iOS orders named visitors (by name) before unnamed ones (by license).
        val VISITOR_ORDER: Comparator<Visitor> = compareBy(
            { it.name == null },
            { it.name?.lowercase() ?: "" },
            { it.license.lowercase() },
        )
    }
}
