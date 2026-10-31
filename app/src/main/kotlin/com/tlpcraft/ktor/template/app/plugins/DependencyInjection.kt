package com.tlpcraft.ktor.template.app.plugins

import com.tlpcraft.ktor.template.app.AppConfig
import com.tlpcraft.ktor.template.app.coreModule
import com.tlpcraft.ktor.template.core.server.ServerFeature
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.plugin.KoinIsolated
import org.koin.logger.slf4jLogger

/** An isolated Koin context per application instance: no global state shared between tests. */
internal fun Application.configureDependencyInjection(config: AppConfig, features: List<ServerFeature>) {
    val featureModules = features.map { it.koinModule(environment.config) }
    install(KoinIsolated) {
        slf4jLogger()
        modules(listOf(coreModule(config)) + featureModules)
    }
}
