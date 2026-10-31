package com.tlpcraft.ktor.template.core.domain

import com.tlpcraft.ktor.template.core.model.FieldViolation

/** Input failed one or more validation rules. Every violated rule is reported, not just the first. */
public data class ValidationError(public val violations: List<FieldViolation>) : DomainError {
    public constructor(field: String, message: String) : this(listOf(FieldViolation(field, message)))

    override val message: String
        get() = violations.joinToString(prefix = "Validation failed: ") { "${it.field} ${it.message}" }
}

/** Collects [FieldViolation]s for the rules declared in a [validated] block. */
public class ValidationScope @PublishedApi internal constructor() {
    private val violations = mutableListOf<FieldViolation>()

    /** Records a violation of [field] with [message] when [condition] is false. */
    public fun check(field: String, condition: Boolean, message: () -> String) {
        if (!condition) violations += FieldViolation(field, message())
    }

    @PublishedApi
    internal fun violations(): List<FieldViolation> = violations.toList()
}

/**
 * Runs every rule in [rules] and, if none is violated, returns [create]'s value. Otherwise returns
 * a [ValidationError] listing all violations.
 *
 * ```
 * validated(
 *     rules = { check("title", title.isNotBlank()) { "must not be blank" } },
 *     create = { NoteContent(title, body) }
 * )
 * ```
 */
public inline fun <T> validated(rules: ValidationScope.() -> Unit, create: () -> T): Outcome<T, ValidationError> {
    val violations = ValidationScope().apply(rules).violations()
    return if (violations.isEmpty()) Outcome.Success(create()) else Outcome.Failure(ValidationError(violations))
}
