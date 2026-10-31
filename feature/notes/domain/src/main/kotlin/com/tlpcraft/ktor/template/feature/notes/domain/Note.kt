package com.tlpcraft.ktor.template.feature.notes.domain

import kotlin.time.Instant
import kotlin.uuid.Uuid

@JvmInline
value class NoteId(val value: Uuid) {
    companion object {
        fun random(): NoteId = NoteId(Uuid.random())
    }
}

data class Note(
    val id: NoteId,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
