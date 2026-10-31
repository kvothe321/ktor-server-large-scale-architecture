package com.tlpcraft.ktor.template.app.health

import com.tlpcraft.ktor.template.core.database.AppDatabase
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import org.koin.ktor.ext.inject

/** `GET /health`: 200 when every dependency is reachable, 503 otherwise. Suitable as a readiness probe. */
internal fun Route.healthRoutes() {
    val database by inject<AppDatabase>()
    get("/health") {
        val checks = mapOf("database" to if (database.isHealthy()) HealthResponse.Status.UP else HealthResponse.Status.DOWN)
        val healthy = checks.values.all { it == HealthResponse.Status.UP }
        call.respond(
            if (healthy) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable,
            HealthResponse(if (healthy) HealthResponse.Status.UP else HealthResponse.Status.DOWN, checks)
        )
    }.describe {
        tag("Health")
        summary = "Service health"
        responses {
            HttpStatusCode.OK {
                description = "All dependencies are reachable"
                schema = jsonSchema<HealthResponse>()
            }
            HttpStatusCode.ServiceUnavailable {
                description = "At least one dependency is down"
                schema = jsonSchema<HealthResponse>()
            }
        }
    }
}
