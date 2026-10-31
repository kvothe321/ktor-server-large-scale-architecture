package com.tlpcraft.ktor.template.core.network

import io.ktor.client.plugins.logging.LogLevel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Settings shared by every outgoing HTTP client. */
public data class HttpClientConfig(
    public val requestTimeout: Duration = 10.seconds,
    public val connectTimeout: Duration = 5.seconds,
    public val logLevel: LogLevel = LogLevel.INFO
)
