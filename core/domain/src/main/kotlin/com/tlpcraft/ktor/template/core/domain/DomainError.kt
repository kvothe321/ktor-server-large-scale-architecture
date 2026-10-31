package com.tlpcraft.ktor.template.core.domain

/**
 * Base type for typed errors carried by [Outcome.Failure].
 *
 * Errors are expressed in domain terms. Infrastructure exceptions (SQL, I/O, HTTP) are translated
 * into a [DomainError] subtype in the data layer and never leak upwards. Implement it with a
 * `sealed interface` per feature so call sites can handle every case with an exhaustive `when`.
 */
public interface DomainError {
    /** Human-readable description for logs. Map to client-facing text in the presentation layer. */
    public val message: String

    /** The underlying failure, if any. For observability only, never for control flow. */
    public val cause: Throwable? get() = null
}
