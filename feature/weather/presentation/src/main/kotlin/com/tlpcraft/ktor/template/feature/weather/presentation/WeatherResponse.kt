package com.tlpcraft.ktor.template.feature.weather.presentation

import com.tlpcraft.ktor.template.feature.weather.domain.Freshness
import com.tlpcraft.ktor.template.feature.weather.domain.WeatherCondition
import com.tlpcraft.ktor.template.feature.weather.domain.WeatherReport
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
internal data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val temperatureCelsius: Double,
    val windSpeedKmh: Double,
    val condition: WeatherCondition,
    val weatherCode: Int,
    val observedAt: Instant,
    val fetchedAt: Instant,
    val freshness: Freshness
)

internal fun WeatherReport.toResponse(): WeatherResponse = WeatherResponse(
    latitude = weather.coordinates.latitude,
    longitude = weather.coordinates.longitude,
    temperatureCelsius = weather.temperatureCelsius,
    windSpeedKmh = weather.windSpeedKmh,
    condition = weather.condition,
    weatherCode = weather.weatherCode,
    observedAt = weather.observedAt,
    fetchedAt = fetchedAt,
    freshness = freshness
)
