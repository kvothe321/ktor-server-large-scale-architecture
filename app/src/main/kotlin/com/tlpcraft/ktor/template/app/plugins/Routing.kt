package com.tlpcraft.ktor.template.app.plugins

import com.tlpcraft.ktor.template.app.health.healthRoutes
import com.tlpcraft.ktor.template.core.server.API_PREFIX
import com.tlpcraft.ktor.template.core.server.ServerFeature
import io.ktor.http.ContentType
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.application.Application
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

/** Health check, Swagger UI (with the OpenAPI document generated from the routes) and every feature under [API_PREFIX]. */
internal fun Application.configureRouting(features: List<ServerFeature>) {
    routing {
        healthRoutes()
        swaggerUI(path = "/swagger") {
            info = OpenApiInfo(
                title = "Ktor Server Template API",
                version = "1.0.0",
                description = "Reference multi-module Ktor backend: weather, notes and images."
            )
            source = OpenApiDocSource.Routing(contentType = ContentType.Application.Json)
            remotePath = "openapi.json"
        }
        route(API_PREFIX) {
            features.forEach { feature -> feature.routes(this) }
        }
    }
}
