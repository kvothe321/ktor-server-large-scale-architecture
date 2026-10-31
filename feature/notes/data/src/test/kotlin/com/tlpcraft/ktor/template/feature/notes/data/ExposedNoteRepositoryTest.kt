package com.tlpcraft.ktor.template.feature.notes.data

import com.tlpcraft.ktor.template.core.model.PageRequest
import com.tlpcraft.ktor.template.core.testing.RecordingLoggerFactory
import com.tlpcraft.ktor.template.core.testing.assertFailure
import com.tlpcraft.ktor.template.core.testing.assertSuccess
import com.tlpcraft.ktor.template.core.testing.testDatabase
import com.tlpcraft.ktor.template.feature.notes.domain.Note
import com.tlpcraft.ktor.template.feature.notes.domain.NoteError
import com.tlpcraft.ktor.template.feature.notes.domain.NoteId
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest

class ExposedNoteRepositoryTest {
    private val database = testDatabase()
    private val loggers = RecordingLoggerFactory()
    private val repository = noteRepository(database, loggers)

    private val nanoPrecise = Instant.parse("2026-03-01T10:15:30.123456789Z")
    private val note = Note(NoteId.random(), "Title", "Body", createdAt = nanoPrecise, updatedAt = nanoPrecise)

    @AfterTest
    fun close() = database.close()

    @Test
    fun `the note returned by insert equals the one read back`() = runTest {
        val inserted = repository.insert(note).assertSuccess()

        assertEquals(inserted, repository.find(note.id).assertSuccess())
        assertEquals(Instant.parse("2026-03-01T10:15:30.123456Z"), inserted.createdAt)
    }

    @Test
    fun `update and delete of a missing note are NotFound`() = runTest {
        repository.update(note).assertFailure<NoteError.NotFound>()
        repository.delete(note.id).assertFailure<NoteError.NotFound>()
    }

    @Test
    fun `database failures become Unavailable and are logged`() = runTest {
        database.close()

        repository.list(PageRequest()).assertFailure<NoteError.Unavailable>()
        assertEquals("Notes query failed", loggers.events.single().message)
    }
}
