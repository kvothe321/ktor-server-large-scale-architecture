package com.tlpcraft.ktor.template.e2e

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Where the server under test lives. Set by the `e2eTest` task; overridable with `E2E_BASE_URL`. */
val baseUrl: String = System.getProperty("e2e.baseUrl") ?: System.getenv("E2E_BASE_URL") ?: "http://localhost:8080"

/** A plain HTTP client, as any API consumer would use: JSON, timeouts, no knowledge of the server code. */
fun e2eClient(): HttpClient = HttpClient(CIO) {
    expectSuccess = false
    defaultRequest { url(baseUrl.trimEnd('/') + "/") }
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 5_000
    }
}
