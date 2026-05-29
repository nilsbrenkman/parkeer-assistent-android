package nl.parkeerassistent.amsterdam.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val balance: String,
    val hourRate: Double,
    val productId: Long,
    val zoneId: Long,
    val parkingMeterId: Long,
    val regime: Regime,
)

@Serializable
data class BalanceResponse(
    val balance: String,
)

@Serializable
data class RegimeResponse(
    val hourRate: Double,
    val zoneId: Long,
    val regime: Regime,
)

@Serializable
data class Regime(
    val days: List<RegimeDay>,
)

@Serializable
data class RegimeDay(
    val weekday: String,
    val startTime: String,
    val endTime: String,
)
