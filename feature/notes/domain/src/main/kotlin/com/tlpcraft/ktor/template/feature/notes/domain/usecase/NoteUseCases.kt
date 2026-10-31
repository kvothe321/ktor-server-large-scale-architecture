package com.tlpcraft.ktor.template.feature.notes.domain.usecase

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.flatMap
import com.tlpcraft.ktor.template.core.domain.mapError
import com.tlpcraft.ktor.template.core.model.Page
import com.tlpcraft.ktor.template.core.model.PageRequest
import com.tlpcraft.ktor.template.feature.notes.domain.Note
import com.tlpcraft.ktor.template.feature.notes.domain.NoteDraft
import com.tlpcraft.ktor.template.feature.notes.domain.NoteError
import com.tlpcraft.ktor.template.feature.notes.domain.NoteId
import com.tlpcraft.ktor.template.feature.notes.domain.NoteRepository
import kotlin.time.Clock

class CreateNoteUseCase(
    private val repository: NoteRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(draft: NoteDraft): Outcome<Note, NoteError> = draft.validate()
        .mapError(NoteError::Invalid)
        .flatMap { valid ->
            val now = clock.now()
            repository.insert(Note(id = NoteId.random(), title = valid.title, content = valid.content, createdAt = now, updatedAt = now))
        }
}

class GetNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: NoteId): Outcome<Note, NoteError> = repository.find(id)
}

class ListNotesUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(request: PageRequest): Outcome<Page<Note>, NoteError> = repository.list(request)
}

class UpdateNoteUseCase(
    private val repository: NoteRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(id: NoteId, draft: NoteDraft): Outcome<Note, NoteError> = draft.validate()
        .mapError(NoteError::Invalid)
        .flatMap { valid ->
            repository.find(id).flatMap { existing ->
                repository.update(existing.copy(title = valid.title, content = valid.content, updatedAt = clock.now()))
            }
        }
}

class DeleteNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: NoteId): Outcome<Unit, NoteError> = repository.delete(id)
}
