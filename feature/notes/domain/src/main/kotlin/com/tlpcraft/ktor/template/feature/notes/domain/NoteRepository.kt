package com.tlpcraft.ktor.template.feature.notes.domain

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.model.Page
import com.tlpcraft.ktor.template.core.model.PageRequest

interface NoteRepository {
    suspend fun insert(note: Note): Outcome<Note, NoteError>

    /** Fails with [NoteError.NotFound] when no note has [id]. */
    suspend fun find(id: NoteId): Outcome<Note, NoteError>

    /** Most recently created first. */
    suspend fun list(request: PageRequest): Outcome<Page<Note>, NoteError>

    /** Fails with [NoteError.NotFound] when no note has the same id. */
    suspend fun update(note: Note): Outcome<Note, NoteError>

    /** Fails with [NoteError.NotFound] when no note has [id]. */
    suspend fun delete(id: NoteId): Outcome<Unit, NoteError>
}
