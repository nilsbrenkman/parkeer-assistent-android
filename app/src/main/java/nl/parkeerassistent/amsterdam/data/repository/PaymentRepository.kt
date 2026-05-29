package nl.parkeerassistent.amsterdam.data.repository

import nl.parkeerassistent.amsterdam.data.model.PaymentRequest
import nl.parkeerassistent.amsterdam.data.model.PaymentResponse
import nl.parkeerassistent.amsterdam.data.remote.PaymentApi

interface PaymentRepository {
    /** Creates a payment and returns the URL the user is sent to; they return to the app manually. */
    suspend fun createPayment(amount: Long, brand: String, lang: String): PaymentResponse
}

class PaymentRepositoryImpl(private val api: PaymentApi) : PaymentRepository {
    override suspend fun createPayment(amount: Long, brand: String, lang: String): PaymentResponse =
        api.createPayment(PaymentRequest(amount, brand, lang))
}
