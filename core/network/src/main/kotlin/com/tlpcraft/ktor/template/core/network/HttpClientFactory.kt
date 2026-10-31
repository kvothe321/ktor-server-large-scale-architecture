package com.tlpcraft.ktor.template.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** JSON settings for third-party APIs: tolerate fields we do not model. */
public val RemoteJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * Creates an [HttpClient] with JSON, timeouts and logging. Pass a mock [engine] in tests.
 * The caller owns the client and must close it.
 */
public fun createHttpClient(config: HttpClientConfig = HttpClientConfig(), engine: HttpClientEngine = CIO.create()): HttpClient = HttpClient(engine) {
    expectSuccess = false
    install(ContentNegotiation) { json(RemoteJson) }
    install(HttpTimeout) {
        requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds
        connectTimeoutMillis = config.connectTimeout.inWholeMilliseconds
    }
    install(Logging) { level = config.logLevel }
}
