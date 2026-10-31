package com.tlpcraft.ktor.template.feature.notes.domain

import com.tlpcraft.ktor.template.core.domain.DomainError
import com.tlpcraft.ktor.template.core.domain.ValidationError

sealed interface NoteError : DomainError {
    data class NotFound(val id: NoteId) : NoteError {
        override val message: String get() = "Note ${id.value} not found"
    }

    data class Invalid(val error: ValidationError) : NoteError {
        override val message: String get() = error.message
    }

    /** Notes could not be read or written, e.g. the database is down. */
    data class Unavailable(override val cause: Throwable) : NoteError {
        override val message: String get() = "Notes storage unavailable: ${cause.message}"
    }
}
