package com.tlpcraft.ktor.template.feature.weather

import com.tlpcraft.ktor.template.core.server.ServerFeature
import com.tlpcraft.ktor.template.feature.weather.data.WeatherDataConfig
import com.tlpcraft.ktor.template.feature.weather.data.weatherRepository
import com.tlpcraft.ktor.template.feature.weather.domain.GetCurrentWeatherUseCase
import com.tlpcraft.ktor.template.feature.weather.presentation.WeatherRoutes
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.routing.Route
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.ext.get

/**
 * Current weather from Open-Meteo, cached in the local database.
 *
 * Configuration (`weather` section): `baseUrl` (default Open-Meteo), `cacheTtl` (ISO-8601 or
 * Kotlin duration such as `15m`, default 15 minutes). Requires an `HttpClient` binding.
 */
object WeatherFeature : ServerFeature {
    private const val DEFAULT_BASE_URL = "https://api.open-meteo.com"
    private val DEFAULT_CACHE_TTL = 15.minutes

    override fun koinModule(config: ApplicationConfig): Module {
        val dataConfig = WeatherDataConfig(
            baseUrl = config.propertyOrNull("weather.baseUrl")?.getString() ?: DEFAULT_BASE_URL,
            cacheTtl = config.propertyOrNull("weather.cacheTtl")?.getString()?.let(Duration::parse) ?: DEFAULT_CACHE_TTL
        )
        return module {
            single {
                weatherRepository(client = get(), database = get(), clock = get(), loggerFactory = get(), config = dataConfig)
            }
            singleOf(::GetCurrentWeatherUseCase)
            singleOf(::WeatherRoutes)
        }
    }

    override fun routes(route: Route) {
        route.get<WeatherRoutes>().register(route)
    }
}
