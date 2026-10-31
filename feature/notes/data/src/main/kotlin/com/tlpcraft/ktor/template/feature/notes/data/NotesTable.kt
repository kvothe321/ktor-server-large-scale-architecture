package com.tlpcraft.ktor.template.feature.notes.data

import com.tlpcraft.ktor.template.feature.notes.domain.NoteDraft
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

/** Mirrors `V1__create_notes.sql`; Flyway owns the schema, this object only maps it. */
internal object NotesTable : Table("notes") {
    val id = uuid("id")
    val title = varchar("title", NoteDraft.MAX_TITLE_LENGTH)
    val content = text("content")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}
