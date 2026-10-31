package com.tlpcraft.ktor.template.feature.weather

import com.tlpcraft.ktor.template.core.network.createHttpClient
import com.tlpcraft.ktor.template.core.server.ApiProblem
import com.tlpcraft.ktor.template.core.testing.testFeature
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.koin.dsl.module
import org.koin.dsl.onClose

class WeatherApiTest {
    @Serializable
    private data class WeatherJson(val latitude: Double, val temperatureCelsius: Double, val condition: String, val freshness: String)

    private var providerUp = true
    private var providerCalls = 0

    private val providerModule = module {
        single<HttpClient> {
            createHttpClient(
                engine = MockEngine { request ->
                    providerCalls++
                    assertEquals("weather.test", request.url.host)
                    if (providerUp) {
                        respond(
                            """{"current":{"time":1767268800,"temperature_2m":-2.5,"wind_speed_10m":15.0,"weather_code":71}}""",
                            HttpStatusCode.OK,
                            headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        )
                    } else {
                        respond("down", HttpStatusCode.BadGateway)
                    }
                }
            )
        } onClose { it?.close() }
    }

    private fun weatherTest(block: suspend (HttpClient) -> Unit) = testFeature(
        feature = WeatherFeature,
        config = mapOf("weather.baseUrl" to "https://weather.test", "weather.cacheTtl" to "15m"),
        extraModules = listOf(providerModule)
    ) { client -> block(client) }

    @Test
    fun `returns live weather then serves the cache`() = weatherTest { client ->
        val first = client.get("/api/v1/weather/current?latitude=46.77&longitude=23.62")
        assertEquals(HttpStatusCode.OK, first.status)
        assertEquals(WeatherJson(46.77, -2.5, "SNOW", "LIVE"), first.body<WeatherJson>())

        val second = client.get("/api/v1/weather/current?latitude=46.77&longitude=23.62").body<WeatherJson>()

        assertEquals("CACHED", second.freshness)
        assertEquals(1, providerCalls)
    }

    @Test
    fun `missing and malformed parameters are reported per field`() = weatherTest { client ->
        val response = client.get("/api/v1/weather/current?latitude=abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(listOf("latitude", "longitude"), response.body<ApiProblem>().violations.map { it.field })
        assertEquals(0, providerCalls)
    }

    @Test
    fun `out of range coordinates are a validation problem`() = weatherTest { client ->
        val response = client.get("/api/v1/weather/current?latitude=100&longitude=0")

        assertEquals(listOf("latitude"), response.body<ApiProblem>().violations.map { it.field })
    }

    @Test
    fun `provider outage without cache is a 503 problem`() = weatherTest { client ->
        providerUp = false

        val response = client.get("/api/v1/weather/current?latitude=1&longitude=1")

        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        assertEquals("urn:problem-type:service-unavailable", response.body<JsonObject>()["type"].toString().trim('"'))
    }
}
