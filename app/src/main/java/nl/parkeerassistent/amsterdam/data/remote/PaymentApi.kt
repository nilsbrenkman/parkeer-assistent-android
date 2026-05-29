package nl.parkeerassistent.amsterdam.data.remote

import nl.parkeerassistent.amsterdam.data.model.PaymentRequest
import nl.parkeerassistent.amsterdam.data.model.PaymentResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentApi {

    @POST("payment")
    suspend fun createPayment(@Body body: PaymentRequest): PaymentResponse
}
