package nl.parkeerassistent.amsterdam.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequest(
    val amount: Long,
    val brand: String,
    val lang: String,
)

/**
 * Result of `POST payment`: a URL the user is sent to (e.g. iDEAL). The user completes payment
 * externally and returns to the app on their own; there is no completion/status callback.
 */
@Serializable
data class PaymentResponse(
    val url: String,
)
