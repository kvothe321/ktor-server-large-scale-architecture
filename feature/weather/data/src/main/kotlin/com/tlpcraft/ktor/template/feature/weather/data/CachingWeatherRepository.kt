package com.tlpcraft.ktor.template.feature.weather.data

import com.tlpcraft.ktor.template.core.data.outcomeCatching
import com.tlpcraft.ktor.template.core.database.AppDatabase
import com.tlpcraft.ktor.template.core.domain.DomainError
import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.getOrNull
import com.tlpcraft.ktor.template.core.logger.Logger
import com.tlpcraft.ktor.template.core.logger.LoggerFactory
import com.tlpcraft.ktor.template.feature.weather.domain.Coordinates
import com.tlpcraft.ktor.template.feature.weather.domain.Freshness
import com.tlpcraft.ktor.template.feature.weather.domain.WeatherError
import com.tlpcraft.ktor.template.feature.weather.domain.WeatherReport
import com.tlpcraft.ktor.template.feature.weather.domain.WeatherRepository
import io.ktor.client.HttpClient
import kotlin.math.roundToLong
import kotlin.time.Clock
import kotlin.time.Duration

/** Settings of the weather data layer. */
data class WeatherDataConfig(
    val baseUrl: String,
    val cacheTtl: Duration
)

/** Creates the production [WeatherRepository]: Open-Meteo behind a database cache. */
fun weatherRepository(
    client: HttpClient,
    database: AppDatabase,
    clock: Clock,
    loggerFactory: LoggerFactory,
    config: WeatherDataConfig
): WeatherRepository = CachingWeatherRepository(
    remote = OpenMeteoDataSource(client, config.baseUrl),
    cache = WeatherCacheDataSource(database),
    clock = clock,
    cacheTtl = config.cacheTtl,
    logger = loggerFactory.logger(CachingWeatherRepository::class.java.name)
)

/**
 * Cache-first: a cached reading younger than [cacheTtl] is served without a network call. Otherwise
 * the provider is asked and the cache refreshed. If the provider fails, an expired reading is still
 * better than nothing and is served as [Freshness.STALE]. The cache is an optimisation, so its own
 * failures are logged and treated as a miss rather than failing the request.
 */
internal class CachingWeatherRepository(
    private val remote: OpenMeteoDataSource,
    private val cache: WeatherCacheDataSource,
    private val clock: Clock,
    private val cacheTtl: Duration,
    private val logger: Logger
) : WeatherRepository {
    override suspend fun currentWeather(coordinates: Coordinates): Outcome<WeatherReport, WeatherError> {
        val key = coordinates.toCacheKey()
        val cached = cacheOperation("read") { cache.find(key) }.getOrNull()
        val now = clock.now()
        if (cached != null && now - cached.fetchedAt < cacheTtl) {
            return Outcome.Success(WeatherReport(cached.weather, cached.fetchedAt, Freshness.CACHED))
        }
        return when (val fetched = remote.currentWeather(key)) {
            is Outcome.Success -> {
                cacheOperation("write") { cache.save(CachedWeather(fetched.value, fetchedAt = now)) }
                Outcome.Success(WeatherReport(fetched.value, fetchedAt = now, Freshness.LIVE))
            }

            is Outcome.Failure -> {
                logger.warn("Weather provider failed", "latitude" to key.latitude, "longitude" to key.longitude, "reason" to fetched.error.message)
                if (cached != null) {
                    Outcome.Success(WeatherReport(cached.weather, cached.fetchedAt, Freshness.STALE))
                } else {
                    Outcome.Failure(WeatherError.Unavailable(fetched.error.message, fetched.error.cause))
                }
            }
        }
    }

    private suspend fun <T> cacheOperation(name: String, block: suspend () -> T): Outcome<T, CacheError> = outcomeCatching(
        onError = { exception ->
            logger.warn("Weather cache $name failed", throwable = exception)
            CacheError(exception)
        },
        block = { block() }
    )

    private data class CacheError(override val cause: Throwable) : DomainError {
        override val message: String get() = "Weather cache failure"
    }
}

/**
 * Rounds to two decimals (about 1 km), so nearby requests share a cache entry and the provider
 * is queried for exactly the coordinates that get cached.
 */
internal fun Coordinates.toCacheKey(): Coordinates = Coordinates(latitude.roundTo2(), longitude.roundTo2())

private const val TWO_DECIMALS = 100.0

private fun Double.roundTo2(): Double = (this * TWO_DECIMALS).roundToLong() / TWO_DECIMALS
