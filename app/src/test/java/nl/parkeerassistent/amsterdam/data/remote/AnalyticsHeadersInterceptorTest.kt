package nl.parkeerassistent.amsterdam.data.remote

import nl.parkeerassistent.amsterdam.FakeDeviceInfo
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Verifies every request carries the `X-ParkeerAssistent-*` analytics headers (port of iOS). */
class AnalyticsHeadersInterceptorTest {

    private lateinit var server: MockWebServer

    @Before fun setUp() { server = MockWebServer().apply { start() } }
    @After fun tearDown() = server.shutdown()

    @Test fun `adds analytics headers from device info`() {
        server.enqueue(MockResponse().setBody("{}"))
        val device = FakeDeviceInfo(userId = "u-7", osVersion = "14", appVersion = "1.2.3", appBuild = "42")
        val client = OkHttpClient.Builder()
            .addInterceptor(AnalyticsHeadersInterceptor(device))
            .build()

        client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()

        val request = server.takeRequest()
        assertEquals("u-7", request.getHeader("X-ParkeerAssistent-UserId"))
        assertEquals("Android", request.getHeader("X-ParkeerAssistent-OS"))
        assertEquals("14", request.getHeader("X-ParkeerAssistent-SDK"))
        assertEquals("1.2.3", request.getHeader("X-ParkeerAssistent-Version"))
        assertEquals("42", request.getHeader("X-ParkeerAssistent-Build"))
    }
}
