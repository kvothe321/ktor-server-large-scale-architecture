package com.tlpcraft.ktor.template.feature.weather.domain

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.flatMap
import com.tlpcraft.ktor.template.core.domain.mapError

class GetCurrentWeatherUseCase(private val repository: WeatherRepository) {
    suspend operator fun invoke(latitude: Double, longitude: Double): Outcome<WeatherReport, WeatherError> = Coordinates.of(latitude, longitude)
        .mapError(WeatherError::Invalid)
        .flatMap { repository.currentWeather(it) }
}
