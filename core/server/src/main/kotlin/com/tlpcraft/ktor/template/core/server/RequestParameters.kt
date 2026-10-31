package com.tlpcraft.ktor.template.core.server

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.ValidationError
import com.tlpcraft.ktor.template.core.domain.flatMap
import com.tlpcraft.ktor.template.core.domain.pageRequestOf
import com.tlpcraft.ktor.template.core.domain.validated
import com.tlpcraft.ktor.template.core.model.PageRequest
import io.ktor.server.application.ApplicationCall
import kotlin.uuid.Uuid

/** Reads `?page=&size=` (both optional) into a validated [PageRequest]. */
public fun ApplicationCall.pageRequest(): Outcome<PageRequest, ValidationError> {
    val rawPage = request.queryParameters["page"]
    val rawSize = request.queryParameters["size"]
    val page = rawPage?.toIntOrNull() ?: PageRequest.FIRST_PAGE
    val size = rawSize?.toIntOrNull() ?: PageRequest.DEFAULT_SIZE
    return validated(
        rules = {
            check("page", rawPage == null || rawPage.toIntOrNull() != null) { "must be an integer" }
            check("size", rawSize == null || rawSize.toIntOrNull() != null) { "must be an integer" }
        },
        create = {}
    ).flatMap { pageRequestOf(page, size) }
}

/** Reads the path parameter [name] as a [Uuid]. */
public fun ApplicationCall.uuidParameter(name: String): Outcome<Uuid, ValidationError> {
    val uuid = parameters[name]?.let(Uuid::parseOrNull)
    return if (uuid != null) Outcome.Success(uuid) else Outcome.Failure(ValidationError(name, "must be a valid UUID"))
}
