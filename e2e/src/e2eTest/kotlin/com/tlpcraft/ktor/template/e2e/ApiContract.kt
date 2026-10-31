package com.tlpcraft.ktor.template.e2e

import kotlinx.serialization.Serializable

// The API contract as a client sees it. Deliberately duplicated from the server's DTOs: if the
// server changes its wire format, these tests must break.

@Serializable
data class Health(val status: String, val checks: Map<String, String>)

@Serializable
data class Problem(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String? = null,
    val violations: List<Violation> = emptyList()
)

@Serializable
data class Violation(val field: String, val message: String)

@Serializable
data class PageOf<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Long
)

@Serializable
data class NoteInput(val title: String, val content: String)

@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class Image(
    val id: String,
    val filename: String,
    val contentType: String,
    val sizeBytes: Long,
    val uploadedAt: String
)

@Serializable
data class Weather(
    val latitude: Double,
    val longitude: Double,
    val temperatureCelsius: Double,
    val windSpeedKmh: Double,
    val condition: String,
    val weatherCode: Int,
    val observedAt: String,
    val fetchedAt: String,
    val freshness: String
)
