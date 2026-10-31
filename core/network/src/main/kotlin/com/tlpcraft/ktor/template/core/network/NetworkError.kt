package com.tlpcraft.ktor.template.core.network

import com.tlpcraft.ktor.template.core.domain.DomainError

/** Why an outgoing HTTP call failed. Data sources translate these into feature-specific errors. */
public sealed interface NetworkError : DomainError {
    public data class Timeout(override val cause: Throwable) : NetworkError {
        override val message: String get() = "Request timed out"
    }

    public data class Unreachable(override val cause: Throwable) : NetworkError {
        override val message: String get() = "Remote host unreachable: ${cause.message}"
    }

    public data class HttpStatus(public val code: Int, public val body: String) : NetworkError {
        override val message: String get() = "Unexpected HTTP status $code"
    }

    public data class InvalidResponse(override val cause: Throwable) : NetworkError {
        override val message: String get() = "Response could not be decoded: ${cause.message}"
    }
}
