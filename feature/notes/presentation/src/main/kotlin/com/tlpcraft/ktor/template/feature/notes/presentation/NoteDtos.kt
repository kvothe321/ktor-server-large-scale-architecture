package com.tlpcraft.ktor.template.feature.notes.presentation

import com.tlpcraft.ktor.template.feature.notes.domain.Note
import com.tlpcraft.ktor.template.feature.notes.domain.NoteDraft
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
internal data class NoteRequest(
    val title: String,
    val content: String = ""
)

@Serializable
internal data class NoteResponse(
    val id: Uuid,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

internal fun NoteRequest.toDraft(): NoteDraft = NoteDraft(title = title, content = content)

internal fun Note.toResponse(): NoteResponse = NoteResponse(
    id = id.value,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)
