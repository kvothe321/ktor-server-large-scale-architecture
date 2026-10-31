package com.tlpcraft.ktor.template.core.server

import com.tlpcraft.ktor.template.core.domain.ValidationError
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

/**
 * The error body of every failed request, following RFC 9457 (Problem Details for HTTP APIs) and
 * served as `application/problem+json`. [type] is a stable identifier clients can switch on.
 */
@Serializable
public data class ApiProblem(
    public val type: String,
    public val title: String,
    public val status: Int,
    public val detail: String? = null,
    public val violations: List<ApiViolation> = emptyList()
) {
    public companion object {
        public fun badRequest(detail: String, violations: List<ApiViolation> = emptyList()): ApiProblem =
            of(HttpStatusCode.BadRequest, "bad-request", detail, violations)

        public fun validation(error: ValidationError): ApiProblem = of(
            status = HttpStatusCode.BadRequest,
            slug = "validation-failed",
            detail = "One or more fields are invalid.",
            violations = error.violations.map { ApiViolation(it.field, it.message) }
        )

        public fun notFound(detail: String): ApiProblem = of(HttpStatusCode.NotFound, "not-found", detail)

        public fun methodNotAllowed(): ApiProblem = of(HttpStatusCode.MethodNotAllowed, "method-not-allowed", null)

        public fun payloadTooLarge(detail: String): ApiProblem = of(HttpStatusCode.PayloadTooLarge, "payload-too-large", detail)

        public fun unsupportedMediaType(detail: String): ApiProblem = of(HttpStatusCode.UnsupportedMediaType, "unsupported-media-type", detail)

        public fun serviceUnavailable(detail: String): ApiProblem = of(HttpStatusCode.ServiceUnavailable, "service-unavailable", detail)

        /** Never exposes internals: the cause is logged server-side, the client gets a generic message. */
        public fun internal(): ApiProblem = of(HttpStatusCode.InternalServerError, "internal-error", "An unexpected error occurred.")

        private fun of(
            status: HttpStatusCode,
            slug: String,
            detail: String?,
            violations: List<ApiViolation> = emptyList()
        ): ApiProblem = ApiProblem(
            type = "$TYPE_PREFIX$slug",
            title = status.description,
            status = status.value,
            detail = detail,
            violations = violations
        )

        private const val TYPE_PREFIX = "urn:problem-type:"
    }
}

@Serializable
public data class ApiViolation(
    public val field: String,
    public val message: String
)
