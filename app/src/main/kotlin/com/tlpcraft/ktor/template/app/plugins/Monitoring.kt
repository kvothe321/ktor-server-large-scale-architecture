package com.tlpcraft.ktor.template.app.plugins

import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.request.path
import kotlin.uuid.Uuid
import org.slf4j.event.Level

private const val MAX_CALL_ID_LENGTH = 64
private val CALL_ID_PATTERN = Regex("[A-Za-z0-9._-]+")

/**
 * Request correlation and access logging. Each call gets an id (taken from `X-Request-Id` when the
 * client sends a sane one), echoed in the response and put in the MDC as `call-id` so every log line
 * of the request carries it.
 */
internal fun Application.configureMonitoring() {
    install(DefaultHeaders)
    install(CallId) {
        retrieveFromHeader(HttpHeaders.XRequestId)
        generate { Uuid.random().toString() }
        verify { it.length <= MAX_CALL_ID_LENGTH && CALL_ID_PATTERN.matches(it) }
        replyToHeader(HttpHeaders.XRequestId)
    }
    install(CallLogging) {
        level = Level.INFO
        disableDefaultColors()
        callIdMdc("call-id")
        filter { call -> !call.request.path().startsWith("/health") }
    }
}
