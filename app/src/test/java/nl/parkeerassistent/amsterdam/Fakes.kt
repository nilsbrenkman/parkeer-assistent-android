package nl.parkeerassistent.amsterdam

import nl.parkeerassistent.amsterdam.data.local.CredentialStore
import nl.parkeerassistent.amsterdam.data.model.AddParkingRequest
import nl.parkeerassistent.amsterdam.data.model.BalanceResponse
import nl.parkeerassistent.amsterdam.data.model.Credentials
import nl.parkeerassistent.amsterdam.data.model.Parking
import nl.parkeerassistent.amsterdam.data.model.ParkingMeter
import nl.parkeerassistent.amsterdam.data.model.ParkingResponse
import nl.parkeerassistent.amsterdam.data.model.PaymentResponse
import nl.parkeerassistent.amsterdam.data.model.RegimeResponse
import nl.parkeerassistent.amsterdam.data.model.Response
import nl.parkeerassistent.amsterdam.data.model.UserResponse
import nl.parkeerassistent.amsterdam.data.model.Visitor
import nl.parkeerassistent.amsterdam.data.remote.cookie.SessionCookieStore
import nl.parkeerassistent.amsterdam.data.repository.GeoRepository
import nl.parkeerassistent.amsterdam.data.repository.LoginRepository
import nl.parkeerassistent.amsterdam.data.repository.ParkingRepository
import nl.parkeerassistent.amsterdam.data.repository.PaymentRepository
import nl.parkeerassistent.amsterdam.data.repository.UserRepository
import nl.parkeerassistent.amsterdam.data.repository.VisitorRepository
import nl.parkeerassistent.amsterdam.notifications.ParkingNotifications
import nl.parkeerassistent.amsterdam.stats.StatsStore
import nl.parkeerassistent.amsterdam.util.DeviceInfo
import nl.parkeerassistent.amsterdam.util.StringProvider

/** Test doubles for the Context-backed collaborators + repositories. */

class FakeStringProvider : StringProvider {
    override fun get(resId: Int): String = "res:$resId"
}

/** In-memory [SessionCookieStore] for the cookie-jar whitelist/expiry tests. */
class FakeSessionCookieStore : SessionCookieStore {
    val map = mutableMapOf<String, String>()
    var cleared = false
    override fun get(name: String): String? = map[name]
    override fun put(name: String, value: String) { map[name] = value }
    override fun remove(name: String) { map.remove(name) }
    override fun clear() { map.clear(); cleared = true }
}

class FakeDeviceInfo(
    override val userId: String = "user-1",
    override val osVersion: String = "14",
    override val appVersion: String = "1.2.3",
    override val appBuild: String = "42",
) : DeviceInfo

class FakeStatsStore : StatsStore {
    var loginCount = 0
    var visitorCount = 0
    var parkingCount = 0
    var paymentCount = 0
    var requestReview = false
    var requestedMarked = false
    override fun incrementLogin() { loginCount++ }
    override fun incrementVisitor() { visitorCount++ }
    override fun incrementParking() { parkingCount++ }
    override fun incrementPayment() { paymentCount++ }
    override fun shouldRequestReview() = requestReview
    override fun markRequested() { requestedMarked = true }
}

class FakeCredentialStore : CredentialStore {
    val list = mutableListOf<Credentials>()
    override fun retrieve(): List<Credentials> = list.toList()
    override fun store(username: String, password: String, alias: String?) {
        list.removeAll { it.username == username }
        list.add(Credentials(alias, username, password))
    }
    override fun update(account: Credentials, username: String, password: String, alias: String?) {
        val i = list.indexOfFirst { it.username == account.username }
        if (i >= 0) list[i] = Credentials(alias, username, password)
    }
    override fun delete(account: Credentials) { list.removeAll { it.username == account.username } }
    override var recent: String? = null
    override var autoLoginEnabled: Boolean = true
}

class FakeParkingNotifications : ParkingNotifications {
    override var visitors: List<Visitor> = emptyList()
    var lastParking: ParkingResponse? = null
    override fun onParking(response: ParkingResponse) { lastParking = response }
}

class FakeLoginRepository : LoginRepository {
    var loginResult = Response(true, "ok")
    var loggedInResult = Response(true)
    var logoutResult = Response(true)
    var throwOnLogin: Throwable? = null
    override suspend fun isLoggedIn() = loggedInResult
    override suspend fun login(username: String, password: String): Response {
        throwOnLogin?.let { throw it }
        return loginResult
    }
    override suspend fun logout() = logoutResult
}

class FakeVisitorRepository : VisitorRepository {
    var visitors = listOf<Visitor>()
    var addResult = Response(true)
    override suspend fun getVisitors() = visitors
    override suspend fun addVisitor(license: String, name: String) = addResult
    override suspend fun deleteVisitor(id: Long) = Response(true)
}

class FakeUserRepository : UserRepository {
    lateinit var userResponse: UserResponse
    var balanceResponse = BalanceResponse("0")
    lateinit var regimeResponse: RegimeResponse
    override suspend fun getUser() = userResponse
    override suspend fun getBalance() = balanceResponse
    override suspend fun getRegime(parkingMeterId: Long) = regimeResponse
}

class FakeParkingRepository : ParkingRepository {
    var parkingResponse = ParkingResponse(emptyList(), emptyList())
    var addResult = Response(true)
    var stopResult = Response(true)
    var history = listOf<Parking>()
    var throwOnGetParking: Throwable? = null
    var throwOnAdd: Throwable? = null
    var throwOnStop: Throwable? = null
    var throwOnHistory: Throwable? = null
    val addRequests = mutableListOf<AddParkingRequest>()
    val stoppedIds = mutableListOf<Long>()
    override suspend fun getParking(): ParkingResponse {
        throwOnGetParking?.let { throw it }
        return parkingResponse
    }
    override suspend fun addParking(request: AddParkingRequest): Response {
        addRequests.add(request)
        throwOnAdd?.let { throw it }
        return addResult
    }
    override suspend fun stopParking(id: Long): Response {
        stoppedIds.add(id)
        throwOnStop?.let { throw it }
        return stopResult
    }
    override suspend fun getHistory(): List<Parking> {
        throwOnHistory?.let { throw it }
        return history
    }
}

class FakePaymentRepository : PaymentRepository {
    var response = PaymentResponse("https://pay.example/redirect")
    var throwOnCreate: Throwable? = null
    val requests = mutableListOf<Triple<Long, String, String>>()
    override suspend fun createPayment(amount: Long, brand: String, lang: String): PaymentResponse {
        requests.add(Triple(amount, brand, lang))
        throwOnCreate?.let { throw it }
        return response
    }
}

class FakeGeoRepository : GeoRepository {
    var nearby = listOf<ParkingMeter>()
    var inRegion = listOf<ParkingMeter>()
    var details: ParkingMeter? = null
    var throwOnNearby: Throwable? = null
    override suspend fun parkingMetersNearby(lat: Double, lon: Double, n: Int): List<ParkingMeter> {
        throwOnNearby?.let { throw it }
        return nearby
    }
    override suspend fun parkingMetersInRegion(lat: Double, lon: Double): List<ParkingMeter> = inRegion
    override suspend fun parkingMeterDetails(id: Long): ParkingMeter? = details
}
