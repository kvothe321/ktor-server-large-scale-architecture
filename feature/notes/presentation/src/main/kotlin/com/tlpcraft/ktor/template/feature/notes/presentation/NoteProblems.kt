package com.tlpcraft.ktor.template.feature.notes.presentation

import com.tlpcraft.ktor.template.core.server.ApiProblem
import com.tlpcraft.ktor.template.feature.notes.domain.NoteError

internal fun NoteError.toProblem(): ApiProblem = when (this) {
    is NoteError.NotFound -> ApiProblem.notFound("Note ${id.value} does not exist.")
    is NoteError.Invalid -> ApiProblem.validation(error)
    is NoteError.Unavailable -> ApiProblem.serviceUnavailable("Notes are temporarily unavailable.")
}
