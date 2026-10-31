package com.tlpcraft.ktor.template.feature.weather.data

import com.tlpcraft.ktor.template.core.database.AppDatabase
import com.tlpcraft.ktor.template.core.network.createHttpClient
import com.tlpcraft.ktor.template.core.testing.MutableClock
import com.tlpcraft.ktor.template.core.testing.RecordingLoggerFactory
import com.tlpcraft.ktor.template.core.testing.assertFailure
import com.tlpcraft.ktor.template.core.testing.assertSuccess
import com.tlpcraft.ktor.template.core.testing.testDatabase
import com.tlpcraft.ktor.template.feature.weather.domain.Coordinates
import com.tlpcraft.ktor.template.feature.weather.domain.Freshness
import com.tlpcraft.ktor.template.feature.weather.domain.WeatherError
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest

class CachingWeatherRepositoryTest {
    private val clock = MutableClock()
    private val database: AppDatabase = testDatabase()
    private val requests = mutableListOf<HttpRequestData>()
    private var providerUp = true

    private val engine = MockEngine { request ->
        requests += request
        if (providerUp) respondForecast(temperature = 21.5) else respond("down", HttpStatusCode.ServiceUnavailable)
    }

    private val repository = weatherRepository(
        client = createHttpClient(engine = engine),
        database = database,
        clock = clock,
        loggerFactory = RecordingLoggerFactory(),
        config = WeatherDataConfig(baseUrl = "https://weather.test", cacheTtl = 15.minutes)
    )

    private val cluj = Coordinates(46.7712, 23.6236)

    @AfterTest
    fun closeDatabase() = database.close()

    @Test
    fun `a miss fetches from the provider with rounded coordinates`() = runTest {
        val report = repository.currentWeather(cluj).assertSuccess()

        assertEquals(Freshness.LIVE, report.freshness)
        assertEquals(21.5, report.weather.temperatureCelsius)
        assertEquals(Coordinates(46.77, 23.62), report.weather.coordinates)
        val url = requests.single().url
        assertEquals("/v1/forecast", url.encodedPath)
        assertEquals("46.77", url.parameters["latitude"])
        assertEquals("23.62", url.parameters["longitude"])
    }

    @Test
    fun `a fresh cache entry is served without calling the provider`() = runTest {
        repository.currentWeather(cluj).assertSuccess()
        clock.advanceBy(10.minutes)

        val report = repository.currentWeather(Coordinates(46.7699, 23.6199)).assertSuccess()

        assertEquals(Freshness.CACHED, report.freshness)
        assertEquals(1, requests.size)
    }

    @Test
    fun `an expired entry is refreshed`() = runTest {
        repository.currentWeather(cluj).assertSuccess()
        clock.advanceBy(16.minutes)

        val report = repository.currentWeather(cluj).assertSuccess()

        assertEquals(Freshness.LIVE, report.freshness)
        assertEquals(clock.now(), report.fetchedAt)
        assertEquals(2, requests.size)
    }

    @Test
    fun `an expired entry is served stale when the provider is down`() = runTest {
        val original = repository.currentWeather(cluj).assertSuccess()
        clock.advanceBy(1.minutes * 60)
        providerUp = false

        val report = repository.currentWeather(cluj).assertSuccess()

        assertEquals(Freshness.STALE, report.freshness)
        assertEquals(original.fetchedAt, report.fetchedAt)
    }

    @Test
    fun `without a cache entry a provider failure is Unavailable`() = runTest {
        providerUp = false

        repository.currentWeather(cluj).assertFailure<WeatherError.Unavailable>()
    }

    private fun MockRequestHandleScope.respondForecast(temperature: Double): HttpResponseData = respond(
        content = """{"latitude":46.77,"longitude":23.62,"current":{"time":1767268800,"interval":900,""" +
            """"temperature_2m":$temperature,"wind_speed_10m":7.2,"weather_code":3}}""",
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    )
}
