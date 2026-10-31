package com.tlpcraft.ktor.template.app.health

import kotlinx.serialization.Serializable

@Serializable
internal data class HealthResponse(
    val status: Status,
    val checks: Map<String, Status>
) {
    enum class Status { UP, DOWN }
}
