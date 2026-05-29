package nl.parkeerassistent.amsterdam.data.repository

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import nl.parkeerassistent.amsterdam.data.remote.GeoApi
import nl.parkeerassistent.amsterdam.data.remote.LoginApi
import nl.parkeerassistent.amsterdam.data.remote.ParkingApi
import nl.parkeerassistent.amsterdam.data.remote.VisitorApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

/**
 * Exercises the Retrofit interfaces + repositories + kotlinx-serialization mapping against a
 * MockWebServer — i.e. the app↔server contract (method/path, request bodies, response decoding,
 * and the repositories' list-unwrapping). Interceptors/cookie jar are out of scope here.
 */
class RepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var retrofit: Retrofit

    @Before fun setUp() {
        server = MockWebServer().apply { start() }
        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
        retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @After fun tearDown() = server.shutdown()

    private fun enqueue(body: String) = server.enqueue(MockResponse().setBody(body))

    @Test fun `login posts credentials and decodes Response`() = runBlocking {
        enqueue("""{"success":true,"message":"Success"}""")
        val repo = LoginRepositoryImpl(retrofit.create<LoginApi>())

        val response = repo.login("alice", "secret")

        assertTrue(response.success)
        assertEquals("Success", response.message)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/login", request.path)
        assertTrue(request.body.readUtf8().contains("\"username\":\"alice\""))
    }

    @Test fun `getVisitors unwraps the visitors list`() = runBlocking {
        enqueue("""{"visitors":[{"id":7,"license":"12ABC3","formattedLicense":"12-ABC-3","name":"Jan"}]}""")
        val repo = VisitorRepositoryImpl(retrofit.create<VisitorApi>())

        val visitors = repo.getVisitors()

        assertEquals(1, visitors.size)
        assertEquals(7L, visitors[0].id)
        assertEquals("Jan", visitors[0].name)
        assertEquals("/visitor", server.takeRequest().path)
    }

    @Test fun `deleteVisitor uses DELETE with the id in the path`() = runBlocking {
        enqueue("""{"success":true}""")
        VisitorRepositoryImpl(retrofit.create<VisitorApi>()).deleteVisitor(42)

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/visitor/42", request.path)
    }

    @Test fun `getParking decodes active and scheduled, name optional`() = runBlocking {
        enqueue(
            """{"active":[{"id":1,"license":"AA","startTime":"s","endTime":"e","cost":1.2}],
                "scheduled":[{"id":2,"license":"BB","name":"Bob","startTime":"s","endTime":"e","cost":0.0}]}""",
        )
        val response = ParkingRepositoryImpl(retrofit.create<ParkingApi>()).getParking()

        assertEquals(1, response.active.size)
        assertEquals(null, response.active[0].name)
        assertEquals("Bob", response.scheduled[0].name)
    }

    @Test fun `getHistory unwraps the history list`() = runBlocking {
        enqueue("""{"history":[{"id":1,"license":"AA","startTime":"s","endTime":"e","cost":3.5}]}""")
        val history = ParkingRepositoryImpl(retrofit.create<ParkingApi>()).getHistory()

        assertEquals(1, history.size)
        assertEquals(3.5, history[0].cost, 0.0001)
        assertEquals("/parking/history", server.takeRequest().path)
    }

    @Test fun `parkingMetersNearby decodes meters and sends lat lon n`() = runBlocking {
        enqueue("""[{"id":5,"domein":1,"name":"Nieuwmarkt","type":"METER","latitude":52.3,"longitude":4.9,"distance":80.0}]""")
        val meters = GeoRepositoryImpl(retrofit.create<GeoApi>()).parkingMetersNearby(52.3, 4.9, 25)

        assertEquals(1, meters.size)
        assertEquals("Nieuwmarkt", meters[0].name)
        val path = server.takeRequest().path!!
        assertTrue(path.startsWith("/geo/parking-meters/nearby"))
        assertTrue(path.contains("lat=52.3") && path.contains("lon=4.9") && path.contains("n=25"))
    }
}
