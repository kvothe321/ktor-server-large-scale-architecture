package com.tlpcraft.ktor.template.feature.notes.domain

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.asFailure
import com.tlpcraft.ktor.template.core.domain.asSuccess
import com.tlpcraft.ktor.template.core.model.Page
import com.tlpcraft.ktor.template.core.model.PageRequest

/** In-memory [NoteRepository]. Set [failure] to make every call fail with it. */
class FakeNoteRepository(initial: List<Note> = emptyList()) : NoteRepository {
    private val notes = initial.associateBy { it.id }.toMutableMap()

    var failure: NoteError? = null

    val all: List<Note> get() = notes.values.toList()

    override suspend fun insert(note: Note): Outcome<Note, NoteError> = guarded {
        notes[note.id] = note
        note.asSuccess()
    }

    override suspend fun find(id: NoteId): Outcome<Note, NoteError> = guarded {
        notes[id]?.asSuccess() ?: NoteError.NotFound(id).asFailure()
    }

    override suspend fun list(request: PageRequest): Outcome<Page<Note>, NoteError> = guarded {
        val sorted = notes.values.sortedByDescending { it.createdAt }
        Page(sorted.drop(request.offset.toInt()).take(request.size), request, sorted.size.toLong()).asSuccess()
    }

    override suspend fun update(note: Note): Outcome<Note, NoteError> = guarded {
        if (note.id in notes) {
            notes[note.id] = note
            note.asSuccess()
        } else {
            NoteError.NotFound(note.id).asFailure()
        }
    }

    override suspend fun delete(id: NoteId): Outcome<Unit, NoteError> = guarded {
        notes.remove(id)?.let { Unit.asSuccess() } ?: NoteError.NotFound(id).asFailure()
    }

    private inline fun <T> guarded(block: () -> Outcome<T, NoteError>): Outcome<T, NoteError> = failure?.asFailure() ?: block()
}
