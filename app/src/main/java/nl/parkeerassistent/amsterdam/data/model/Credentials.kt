package nl.parkeerassistent.amsterdam.data.model

import kotlinx.serialization.Serializable

/** A saved login (iOS `Credentials`). Uniquely identified by [username]. */
@Serializable
data class Credentials(
    val alias: String? = null,
    val username: String,
    val password: String,
)
