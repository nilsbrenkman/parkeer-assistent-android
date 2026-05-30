package nl.parkeerassistent.amsterdam

import nl.parkeerassistent.amsterdam.data.local.CredentialStore
import nl.parkeerassistent.amsterdam.data.model.BalanceResponse
import nl.parkeerassistent.amsterdam.data.model.Credentials
import nl.parkeerassistent.amsterdam.data.model.ParkingResponse
import nl.parkeerassistent.amsterdam.data.model.RegimeResponse
import nl.parkeerassistent.amsterdam.data.model.Response
import nl.parkeerassistent.amsterdam.data.model.UserResponse
import nl.parkeerassistent.amsterdam.data.model.Visitor
import nl.parkeerassistent.amsterdam.data.repository.LoginRepository
import nl.parkeerassistent.amsterdam.data.repository.UserRepository
import nl.parkeerassistent.amsterdam.data.repository.VisitorRepository
import nl.parkeerassistent.amsterdam.notifications.ParkingNotifications
import nl.parkeerassistent.amsterdam.stats.StatsStore
import nl.parkeerassistent.amsterdam.util.StringProvider

/** Test doubles for the Context-backed collaborators + repositories. */

class FakeStringProvider : StringProvider {
    override fun get(resId: Int): String = "res:$resId"
}

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
