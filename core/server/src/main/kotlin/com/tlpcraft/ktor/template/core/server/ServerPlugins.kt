package com.tlpcraft.ktor.template.core.server

import com.tlpcraft.ktor.template.core.logger.LoggerFactory
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.PayloadTooLargeException
import io.ktor.server.plugins.UnsupportedMediaTypeException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path

/** JSON content negotiation using [AppJson]. */
public fun Application.configureSerialization() {
    install(ContentNegotiation) { json(AppJson) }
}

/**
 * Maps everything that escapes a route handler to an [ApiProblem]. Expected failures travel as
 * typed errors and never reach this point; what does is either a malformed request (4xx) or a bug
 * (logged, then reported as a generic 500).
 */
public fun Application.configureErrorHandling(loggerFactory: LoggerFactory) {
    val logger = loggerFactory.logger("com.tlpcraft.ktor.template.core.server.ErrorHandling")
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            call.respondProblem(ApiProblem.badRequest(cause.message ?: "Malformed request."))
        }
        exception<NotFoundException> { call, cause ->
            call.respondProblem(ApiProblem.notFound(cause.message ?: "Resource not found."))
        }
        exception<UnsupportedMediaTypeException> { call, cause ->
            call.respondProblem(ApiProblem.unsupportedMediaType(cause.message ?: "Unsupported media type."))
        }
        exception<PayloadTooLargeException> { call, cause ->
            call.respondProblem(ApiProblem.payloadTooLarge(cause.message ?: "Payload too large."))
        }
        exception<Throwable> { call, cause ->
            logger.error(
                "Unhandled exception",
                "method" to call.request.httpMethod.value,
                "path" to call.request.path(),
                throwable = cause
            )
            call.respondProblem(ApiProblem.internal())
        }
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respondProblem(ApiProblem.notFound("No resource at ${call.request.path()}."))
        }
        status(HttpStatusCode.MethodNotAllowed) { call, _ ->
            call.respondProblem(ApiProblem.methodNotAllowed())
        }
    }
}
