package com.tlpcraft.ktor.template.app

import com.tlpcraft.ktor.template.app.plugins.configureDependencyInjection
import com.tlpcraft.ktor.template.app.plugins.configureMonitoring
import com.tlpcraft.ktor.template.app.plugins.configureRouting
import com.tlpcraft.ktor.template.core.logger.Slf4jLoggerFactory
import com.tlpcraft.ktor.template.core.server.ServerFeature
import com.tlpcraft.ktor.template.core.server.configureErrorHandling
import com.tlpcraft.ktor.template.core.server.configureSerialization
import com.tlpcraft.ktor.template.feature.images.ImagesFeature
import com.tlpcraft.ktor.template.feature.notes.NotesFeature
import com.tlpcraft.ktor.template.feature.weather.WeatherFeature
import io.ktor.server.application.Application

/** Every feature served by this application. Adding a feature module means adding it here. */
internal val serverFeatures: List<ServerFeature> = listOf(WeatherFeature, NotesFeature, ImagesFeature)

/** Entry point referenced from `application.conf` (`ktor.application.modules`). */
fun Application.module() {
    val config = AppConfig.from(environment.config)
    configureDependencyInjection(config, serverFeatures)
    configureMonitoring()
    configureSerialization()
    configureErrorHandling(Slf4jLoggerFactory)
    configureRouting(serverFeatures)
}
