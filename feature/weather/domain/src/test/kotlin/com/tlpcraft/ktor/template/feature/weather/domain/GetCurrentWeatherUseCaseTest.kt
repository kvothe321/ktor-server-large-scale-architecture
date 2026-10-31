package com.tlpcraft.ktor.template.feature.weather.domain

import com.tlpcraft.ktor.template.core.domain.asSuccess
import com.tlpcraft.ktor.template.core.testing.assertFailure
import com.tlpcraft.ktor.template.core.testing.assertSuccess
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest

class GetCurrentWeatherUseCaseTest {
    private val now = Instant.parse("2026-01-01T12:00:00Z")
    private val report = WeatherReport(
        weather = CurrentWeather(Coordinates(46.77, 23.6), temperatureCelsius = 4.5, windSpeedKmh = 12.0, weatherCode = 3, observedAt = now),
        fetchedAt = now,
        freshness = Freshness.LIVE
    )
    private val repository = FakeWeatherRepository(report.asSuccess())

    @Test
    fun `valid coordinates are passed to the repository`() = runTest {
        assertEquals(report, GetCurrentWeatherUseCase(repository)(latitude = 46.77, longitude = 23.6).assertSuccess())
        assertEquals(listOf(Coordinates(46.77, 23.6)), repository.requests)
    }

    @Test
    fun `out of range coordinates fail without calling the repository`() = runTest {
        val error = GetCurrentWeatherUseCase(repository)(latitude = 91.0, longitude = -181.0).assertFailure<WeatherError.Invalid>()

        assertEquals(listOf("latitude", "longitude"), error.error.violations.map { it.field })
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun `WMO codes map to conditions`() {
        assertEquals(WeatherCondition.CLEAR, WeatherCondition.fromWmoCode(0))
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWmoCode(81))
        assertEquals(WeatherCondition.SNOW, WeatherCondition.fromWmoCode(86))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromWmoCode(95))
        assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromWmoCode(42))
    }
}
