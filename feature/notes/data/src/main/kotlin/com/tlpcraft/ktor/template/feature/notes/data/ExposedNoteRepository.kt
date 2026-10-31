package com.tlpcraft.ktor.template.feature.notes.data

import com.tlpcraft.ktor.template.core.data.outcomeCatching
import com.tlpcraft.ktor.template.core.database.AppDatabase
import com.tlpcraft.ktor.template.core.database.toDatabasePrecision
import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.asFailure
import com.tlpcraft.ktor.template.core.domain.asSuccess
import com.tlpcraft.ktor.template.core.domain.flatMap
import com.tlpcraft.ktor.template.core.logger.Logger
import com.tlpcraft.ktor.template.core.logger.LoggerFactory
import com.tlpcraft.ktor.template.core.model.Page
import com.tlpcraft.ktor.template.core.model.PageRequest
import com.tlpcraft.ktor.template.feature.notes.domain.Note
import com.tlpcraft.ktor.template.feature.notes.domain.NoteError
import com.tlpcraft.ktor.template.feature.notes.domain.NoteId
import com.tlpcraft.ktor.template.feature.notes.domain.NoteRepository
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

/** Creates the production [NoteRepository], backed by [database]. */
fun noteRepository(database: AppDatabase, loggerFactory: LoggerFactory): NoteRepository =
    ExposedNoteRepository(database, loggerFactory.logger(ExposedNoteRepository::class.java.name))

internal class ExposedNoteRepository(
    private val database: AppDatabase,
    private val logger: Logger
) : NoteRepository {
    override suspend fun insert(note: Note): Outcome<Note, NoteError> {
        val stored = note.toDatabasePrecision()
        return query {
            NotesTable.insert { it.write(stored) }
            stored
        }
    }

    override suspend fun find(id: NoteId): Outcome<Note, NoteError> = query {
        NotesTable.selectAll().where { NotesTable.id eq id.value }.singleOrNull()?.toNote()
    }.flatMap { it?.asSuccess() ?: NoteError.NotFound(id).asFailure() }

    override suspend fun list(request: PageRequest): Outcome<Page<Note>, NoteError> = query {
        val total = NotesTable.selectAll().count()
        val items = NotesTable.selectAll()
            .orderBy(NotesTable.createdAt to SortOrder.DESC, NotesTable.id to SortOrder.ASC)
            .limit(request.size)
            .offset(request.offset)
            .map { it.toNote() }
        Page(items, request, total)
    }

    override suspend fun update(note: Note): Outcome<Note, NoteError> {
        val stored = note.toDatabasePrecision()
        return query {
            NotesTable.update({ NotesTable.id eq stored.id.value }) { it.write(stored) }
        }.flatMap { updated -> if (updated > 0) stored.asSuccess() else NoteError.NotFound(stored.id).asFailure() }
    }

    override suspend fun delete(id: NoteId): Outcome<Unit, NoteError> = query {
        NotesTable.deleteWhere { NotesTable.id eq id.value }
    }.flatMap { deleted -> if (deleted > 0) Unit.asSuccess() else NoteError.NotFound(id).asFailure() }

    private suspend fun <T> query(block: JdbcTransaction.() -> T): Outcome<T, NoteError> = outcomeCatching(
        onError = { exception ->
            logger.error("Notes query failed", throwable = exception)
            NoteError.Unavailable(exception)
        },
        block = { database.transaction(block) }
    )
}

private fun Note.toDatabasePrecision(): Note = copy(createdAt = createdAt.toDatabasePrecision(), updatedAt = updatedAt.toDatabasePrecision())

private fun UpdateBuilder<*>.write(note: Note) {
    this[NotesTable.id] = note.id.value
    this[NotesTable.title] = note.title
    this[NotesTable.content] = note.content
    this[NotesTable.createdAt] = note.createdAt
    this[NotesTable.updatedAt] = note.updatedAt
}

private fun ResultRow.toNote(): Note = Note(
    id = NoteId(this[NotesTable.id]),
    title = this[NotesTable.title],
    content = this[NotesTable.content],
    createdAt = this[NotesTable.createdAt],
    updatedAt = this[NotesTable.updatedAt]
)
