package nl.parkeerassistent.amsterdam.data.remote

import nl.parkeerassistent.amsterdam.FakeSessionCookieStore
import nl.parkeerassistent.amsterdam.data.remote.cookie.SessionCookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Maps non-2xx responses to [ApiException]: 401/403 clear the session and surface as
 * [ApiException.Unauthorized]; anything else becomes [ApiException.ServerError] carrying the body.
 */
class ErrorInterceptorTest {

    private lateinit var server: MockWebServer

    @Before fun setUp() { server = MockWebServer().apply { start() } }
    @After fun tearDown() = server.shutdown()

    private fun call(store: FakeSessionCookieStore) {
        val client = OkHttpClient.Builder()
            .addInterceptor(ErrorInterceptor(SessionCookieJar(store)))
            .build()
        client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()
    }

    @Test fun `passes through a successful response`() {
        server.enqueue(MockResponse().setBody("ok"))
        val store = FakeSessionCookieStore().apply { map["token"] = "t" }
        call(store)
        assertFalse(store.cleared)
        assertEquals("t", store.map["token"])
    }

    @Test fun `401 clears session and throws Unauthorized`() {
        server.enqueue(MockResponse().setResponseCode(401))
        val store = FakeSessionCookieStore().apply { map["token"] = "t"; map["product_id"] = "9" }
        try {
            call(store)
            fail("expected ApiException.Unauthorized")
        } catch (e: ApiException) {
            assertSame(ApiException.Unauthorized, e)
        }
        assertTrue(store.cleared)
        assertTrue(store.map.isEmpty())
    }

    @Test fun `403 clears session and throws Unauthorized`() {
        server.enqueue(MockResponse().setResponseCode(403))
        val store = FakeSessionCookieStore().apply { map["token"] = "t" }
        try {
            call(store)
            fail("expected ApiException.Unauthorized")
        } catch (e: ApiException) {
            assertSame(ApiException.Unauthorized, e)
        }
        assertTrue(store.cleared)
    }

    @Test fun `other errors become ServerError carrying the body`() {
        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))
        val store = FakeSessionCookieStore()
        try {
            call(store)
            fail("expected ApiException.ServerError")
        } catch (e: ApiException.ServerError) {
            assertEquals("boom", e.serverMessage)
        }
        assertFalse(store.cleared)
    }
}
