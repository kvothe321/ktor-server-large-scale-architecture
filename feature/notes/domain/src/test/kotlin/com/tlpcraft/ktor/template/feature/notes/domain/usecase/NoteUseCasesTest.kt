package com.tlpcraft.ktor.template.feature.notes.domain.usecase

import com.tlpcraft.ktor.template.core.model.PageRequest
import com.tlpcraft.ktor.template.core.testing.MutableClock
import com.tlpcraft.ktor.template.core.testing.assertFailure
import com.tlpcraft.ktor.template.core.testing.assertSuccess
import com.tlpcraft.ktor.template.feature.notes.domain.FakeNoteRepository
import com.tlpcraft.ktor.template.feature.notes.domain.Note
import com.tlpcraft.ktor.template.feature.notes.domain.NoteDraft
import com.tlpcraft.ktor.template.feature.notes.domain.NoteError
import com.tlpcraft.ktor.template.feature.notes.domain.NoteId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest

class NoteUseCasesTest {
    private val clock = MutableClock()
    private val repository = FakeNoteRepository()

    private fun note(title: String = "Title") = Note(NoteId.random(), title, "Body", clock.now(), clock.now())

    @Test
    fun `create stores a trimmed note stamped with the current time`() = runTest {
        val created = CreateNoteUseCase(repository, clock)(NoteDraft(title = "  Groceries  ", content = "Milk")).assertSuccess()

        assertEquals("Groceries", created.title)
        assertEquals(clock.now(), created.createdAt)
        assertEquals(listOf(created), repository.all)
    }

    @Test
    fun `create reports every invalid field and stores nothing`() = runTest {
        val draft = NoteDraft(title = " ", content = "x".repeat(NoteDraft.MAX_CONTENT_LENGTH + 1))

        val error = CreateNoteUseCase(repository, clock)(draft).assertFailure<NoteError.Invalid>()

        assertEquals(listOf("title", "content"), error.error.violations.map { it.field })
        assertTrue(repository.all.isEmpty())
    }

    @Test
    fun `update changes content and bumps updatedAt only`() = runTest {
        val original = note()
        repository.insert(original)
        clock.advanceBy(5.minutes)

        val updated = UpdateNoteUseCase(repository, clock)(original.id, NoteDraft("New", "Text")).assertSuccess()

        assertEquals(original.createdAt, updated.createdAt)
        assertEquals(clock.now(), updated.updatedAt)
        assertEquals("New", updated.title)
    }

    @Test
    fun `update of a missing note is NotFound`() = runTest {
        val id = NoteId.random()

        assertEquals(id, UpdateNoteUseCase(repository, clock)(id, NoteDraft("T", "C")).assertFailure<NoteError.NotFound>().id)
    }

    @Test
    fun `update validates before touching the repository`() = runTest {
        repository.failure = NoteError.Unavailable(IllegalStateException("down"))

        UpdateNoteUseCase(repository, clock)(NoteId.random(), NoteDraft("", "")).assertFailure<NoteError.Invalid>()
    }

    @Test
    fun `list returns newest first`() = runTest {
        val older = note("older")
        clock.advanceBy(1.minutes)
        val newer = note("newer")
        repository.insert(older)
        repository.insert(newer)

        val page = ListNotesUseCase(repository)(PageRequest(page = 1, size = 10)).assertSuccess()

        assertEquals(listOf(newer, older), page.items)
    }

    @Test
    fun `get and delete pass repository failures through`() = runTest {
        repository.failure = NoteError.Unavailable(IllegalStateException("down"))

        GetNoteUseCase(repository)(NoteId.random()).assertFailure<NoteError.Unavailable>()
        DeleteNoteUseCase(repository)(NoteId.random()).assertFailure<NoteError.Unavailable>()
    }
}
