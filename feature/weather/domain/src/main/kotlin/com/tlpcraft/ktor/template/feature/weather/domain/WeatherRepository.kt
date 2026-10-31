package com.tlpcraft.ktor.template.feature.weather.domain

import com.tlpcraft.ktor.template.core.domain.Outcome

interface WeatherRepository {
    suspend fun currentWeather(coordinates: Coordinates): Outcome<WeatherReport, WeatherError>
}
