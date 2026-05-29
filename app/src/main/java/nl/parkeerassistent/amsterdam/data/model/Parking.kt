package nl.parkeerassistent.amsterdam.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Parking(
    val id: Long,
    val license: String,
    val name: String? = null,
    val startTime: String,
    val endTime: String,
    val cost: Double,
)

@Serializable
data class ParkingResponse(
    val active: List<Parking>,
    val scheduled: List<Parking>,
)

@Serializable
data class AddParkingRequest(
    val license: String,
    val timeMinutes: Int,
    val start: String? = null,
    val productId: Long,
    val zoneId: Long,
    val parkingMeterId: Long,
)

/** Parking history is a flat list of [Parking] entries (no dedicated history type). */
@Serializable
data class HistoryResponse(
    val history: List<Parking>,
)
