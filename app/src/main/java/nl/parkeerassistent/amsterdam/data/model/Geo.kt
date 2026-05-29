package nl.parkeerassistent.amsterdam.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ParkingMeter(
    val id: Int,
    val domein: Int,
    val name: String,
    val type: ParkingMeterType,
    val latitude: Double,
    val longitude: Double,
    val distance: Double? = null,
)

@Serializable
enum class ParkingMeterType {
    SIGN,
    METER,
}
