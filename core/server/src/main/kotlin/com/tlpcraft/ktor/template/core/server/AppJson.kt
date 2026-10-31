package com.tlpcraft.ktor.template.core.server

import kotlinx.serialization.json.Json

/** The JSON format of this API, shared by server content negotiation and test clients. */
public val AppJson: Json = Json {
    encodeDefaults = true
    explicitNulls = false
    ignoreUnknownKeys = true
}
