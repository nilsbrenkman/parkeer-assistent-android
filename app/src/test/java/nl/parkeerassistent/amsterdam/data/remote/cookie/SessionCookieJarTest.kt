package nl.parkeerassistent.amsterdam.data.remote.cookie

import nl.parkeerassistent.amsterdam.FakeSessionCookieStore
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The cookie-jar whitelist/expiry logic (deferred to Phase 8). Only `token` and `product_id` are
 * ever persisted or replayed; empty/expired cookies clear the corresponding entry.
 */
class SessionCookieJarTest {

    private val url = "https://parkeerassistent.nl/".toHttpUrl()

    private fun cookie(name: String, value: String, expiresAt: Long? = null): Cookie =
        Cookie.Builder().name(name).value(value).domain("parkeerassistent.nl").path("/")
            .apply { if (expiresAt != null) expiresAt(expiresAt) }
            .build()

    @Test fun `saves only whitelisted cookies`() {
        val store = FakeSessionCookieStore()
        SessionCookieJar(store).saveFromResponse(
            url,
            listOf(cookie("token", "abc"), cookie("product_id", "99")),
        )
        assertEquals("abc", store.map["token"])
        assertEquals("99", store.map["product_id"])
    }

    @Test fun `ignores non-whitelisted cookies`() {
        val store = FakeSessionCookieStore()
        SessionCookieJar(store).saveFromResponse(
            url,
            listOf(cookie("session", "x"), cookie("JSESSIONID", "y")),
        )
        assertTrue(store.map.isEmpty())
    }

    @Test fun `empty value removes the stored cookie`() {
        val store = FakeSessionCookieStore().apply { map["token"] = "old" }
        SessionCookieJar(store).saveFromResponse(url, listOf(cookie("token", "")))
        assertNull(store.map["token"])
    }

    @Test fun `expired cookie removes the stored cookie`() {
        val store = FakeSessionCookieStore().apply { map["token"] = "old" }
        SessionCookieJar(store).saveFromResponse(
            url,
            listOf(cookie("token", "new", expiresAt = System.currentTimeMillis() - 1_000)),
        )
        assertNull(store.map["token"])
    }

    @Test fun `loadForRequest replays both cookies scoped to the host`() {
        val store = FakeSessionCookieStore().apply { map["token"] = "abc"; map["product_id"] = "99" }
        val cookies = SessionCookieJar(store).loadForRequest(url).associateBy { it.name }

        assertEquals(2, cookies.size)
        assertEquals("abc", cookies["token"]?.value)
        assertEquals("parkeerassistent.nl", cookies["token"]?.domain)
        assertEquals("/", cookies["token"]?.path)
        assertEquals("99", cookies["product_id"]?.value)
    }

    @Test fun `loadForRequest omits cookies that are not present`() {
        val store = FakeSessionCookieStore().apply { map["token"] = "abc" }
        val cookies = SessionCookieJar(store).loadForRequest(url)
        assertEquals(1, cookies.size)
        assertEquals("token", cookies[0].name)
    }

    @Test fun `loadForRequest returns empty with no session`() {
        assertTrue(SessionCookieJar(FakeSessionCookieStore()).loadForRequest(url).isEmpty())
    }

    @Test fun `clear drops the whole session`() {
        val store = FakeSessionCookieStore().apply { map["token"] = "abc"; map["product_id"] = "9" }
        SessionCookieJar(store).clear()
        assertTrue(store.cleared)
        assertTrue(store.map.isEmpty())
    }
}
