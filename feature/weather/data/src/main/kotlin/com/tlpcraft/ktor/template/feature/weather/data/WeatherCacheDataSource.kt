package com.tlpcraft.ktor.template.feature.weather.data

import com.tlpcraft.ktor.template.core.database.AppDatabase
import com.tlpcraft.ktor.template.feature.weather.domain.Coordinates
import com.tlpcraft.ktor.template.feature.weather.domain.CurrentWeather
import kotlin.time.Instant
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert

/** Mirrors `V2__create_weather_cache.sql`. */
internal object WeatherCacheTable : Table("weather_cache") {
    val latitude = double("latitude")
    val longitude = double("longitude")
    val temperatureCelsius = double("temperature_celsius")
    val windSpeedKmh = double("wind_speed_kmh")
    val weatherCode = integer("weather_code")
    val observedAt = timestamp("observed_at")
    val fetchedAt = timestamp("fetched_at")

    override val primaryKey = PrimaryKey(latitude, longitude)
}

internal data class CachedWeather(
    val weather: CurrentWeather,
    val fetchedAt: Instant
)

/** One cached reading per coordinate pair. Exceptions propagate; the repository decides what they mean. */
internal class WeatherCacheDataSource(private val database: AppDatabase) {
    suspend fun find(coordinates: Coordinates): CachedWeather? = database.transaction {
        WeatherCacheTable.selectAll()
            .where { (WeatherCacheTable.latitude eq coordinates.latitude) and (WeatherCacheTable.longitude eq coordinates.longitude) }
            .singleOrNull()
            ?.toCachedWeather()
    }

    suspend fun save(entry: CachedWeather) {
        database.transaction {
            WeatherCacheTable.upsert {
                it[latitude] = entry.weather.coordinates.latitude
                it[longitude] = entry.weather.coordinates.longitude
                it[temperatureCelsius] = entry.weather.temperatureCelsius
                it[windSpeedKmh] = entry.weather.windSpeedKmh
                it[weatherCode] = entry.weather.weatherCode
                it[observedAt] = entry.weather.observedAt
                it[fetchedAt] = entry.fetchedAt
            }
        }
    }
}

private fun ResultRow.toCachedWeather() = CachedWeather(
    weather = CurrentWeather(
        coordinates = Coordinates(this[WeatherCacheTable.latitude], this[WeatherCacheTable.longitude]),
        temperatureCelsius = this[WeatherCacheTable.temperatureCelsius],
        windSpeedKmh = this[WeatherCacheTable.windSpeedKmh],
        weatherCode = this[WeatherCacheTable.weatherCode],
        observedAt = this[WeatherCacheTable.observedAt]
    ),
    fetchedAt = this[WeatherCacheTable.fetchedAt]
)
