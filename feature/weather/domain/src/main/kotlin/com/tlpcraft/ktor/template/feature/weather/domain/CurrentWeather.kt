package com.tlpcraft.ktor.template.feature.weather.domain

import kotlin.time.Instant

data class CurrentWeather(
    val coordinates: Coordinates,
    val temperatureCelsius: Double,
    val windSpeedKmh: Double,
    val weatherCode: Int,
    val observedAt: Instant
) {
    val condition: WeatherCondition get() = WeatherCondition.fromWmoCode(weatherCode)
}

/** [weather] together with how fresh it is, so clients can tell live data from a fallback. */
data class WeatherReport(
    val weather: CurrentWeather,
    val fetchedAt: Instant,
    val freshness: Freshness
)

enum class Freshness {
    /** Fetched from the provider for this request. */
    LIVE,

    /** Served from the cache, within its time-to-live. */
    CACHED,

    /** The provider failed, so an expired cache entry was served instead. */
    STALE
}
