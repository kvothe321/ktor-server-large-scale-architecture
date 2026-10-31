package com.tlpcraft.ktor.template.feature.weather.data

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.map
import com.tlpcraft.ktor.template.core.network.NetworkError
import com.tlpcraft.ktor.template.core.network.safeRequest
import com.tlpcraft.ktor.template.feature.weather.domain.Coordinates
import com.tlpcraft.ktor.template.feature.weather.domain.CurrentWeather
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reads current conditions from the Open-Meteo forecast API (free, no API key). */
internal class OpenMeteoDataSource(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun currentWeather(coordinates: Coordinates): Outcome<CurrentWeather, NetworkError> = safeRequest<ForecastDto> {
        client.get("$baseUrl/v1/forecast") {
            parameter("latitude", coordinates.latitude)
            parameter("longitude", coordinates.longitude)
            parameter("current", "temperature_2m,wind_speed_10m,weather_code")
            parameter("timeformat", "unixtime")
        }
    }.map { it.current.toDomain(coordinates) }
}

@Serializable
internal data class ForecastDto(val current: CurrentDto)

@Serializable
internal data class CurrentDto(
    @SerialName("time") val epochSeconds: Long,
    @SerialName("temperature_2m") val temperatureCelsius: Double,
    @SerialName("wind_speed_10m") val windSpeedKmh: Double,
    @SerialName("weather_code") val weatherCode: Int
)

private fun CurrentDto.toDomain(coordinates: Coordinates) = CurrentWeather(
    coordinates = coordinates,
    temperatureCelsius = temperatureCelsius,
    windSpeedKmh = windSpeedKmh,
    weatherCode = weatherCode,
    observedAt = Instant.fromEpochSeconds(epochSeconds)
)
