package com.tlpcraft.ktor.template.feature.weather.domain

import com.tlpcraft.ktor.template.core.domain.Outcome

/** Returns [result] for every call and records the requested coordinates. */
class FakeWeatherRepository(var result: Outcome<WeatherReport, WeatherError>) : WeatherRepository {
    private val recordedRequests = mutableListOf<Coordinates>()

    val requests: List<Coordinates> get() = recordedRequests.toList()

    override suspend fun currentWeather(coordinates: Coordinates): Outcome<WeatherReport, WeatherError> {
        recordedRequests += coordinates
        return result
    }
}
