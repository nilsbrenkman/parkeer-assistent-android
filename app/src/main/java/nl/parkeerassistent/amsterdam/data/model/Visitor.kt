package nl.parkeerassistent.amsterdam.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Visitor(
    val id: Long,
    val license: String,
    val formattedLicense: String,
    val name: String? = null,
)

@Serializable
data class VisitorResponse(
    val visitors: List<Visitor>,
)

@Serializable
data class AddVisitorRequest(
    val license: String,
    val name: String,
)
