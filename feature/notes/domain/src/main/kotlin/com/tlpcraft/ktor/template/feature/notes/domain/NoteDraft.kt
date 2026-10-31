package com.tlpcraft.ktor.template.feature.notes.domain

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.ValidationError
import com.tlpcraft.ktor.template.core.domain.validated

/** Client-provided note content, not yet validated. */
data class NoteDraft(
    val title: String,
    val content: String
) {
    /** Trims the title and checks the note's invariants. */
    fun validate(): Outcome<NoteDraft, ValidationError> {
        val trimmedTitle = title.trim()
        return validated(
            rules = {
                check("title", trimmedTitle.isNotEmpty()) { "must not be blank" }
                check("title", trimmedTitle.length <= MAX_TITLE_LENGTH) { "must be at most $MAX_TITLE_LENGTH characters" }
                check("content", content.length <= MAX_CONTENT_LENGTH) { "must be at most $MAX_CONTENT_LENGTH characters" }
            },
            create = { copy(title = trimmedTitle) }
        )
    }

    companion object {
        const val MAX_TITLE_LENGTH: Int = 200
        const val MAX_CONTENT_LENGTH: Int = 10_000
    }
}
