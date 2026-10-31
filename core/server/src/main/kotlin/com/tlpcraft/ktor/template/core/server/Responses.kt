package com.tlpcraft.ktor.template.core.server

import com.tlpcraft.ktor.template.core.domain.DomainError
import com.tlpcraft.ktor.template.core.domain.Outcome
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.response.respondText

public suspend fun ApplicationCall.respondProblem(problem: ApiProblem) {
    respondText(
        text = AppJson.encodeToString(ApiProblem.serializer(), problem),
        contentType = ContentType.Application.ProblemJson,
        status = HttpStatusCode.fromValue(problem.status)
    )
}

/**
 * Responds with [transform]ed success value and [successStatus], or with the problem [toProblem]
 * maps the error to. Keeps route handlers to a single expression.
 */
public suspend inline fun <T, E : DomainError, reified R : Any> ApplicationCall.respondOutcome(
    outcome: Outcome<T, E>,
    successStatus: HttpStatusCode = HttpStatusCode.OK,
    toProblem: (E) -> ApiProblem,
    transform: (T) -> R
) {
    when (outcome) {
        is Outcome.Success -> respond(successStatus, transform(outcome.value))
        is Outcome.Failure -> respondProblem(toProblem(outcome.error))
    }
}

/** Responds `204 No Content` on success. */
public suspend inline fun <E : DomainError> ApplicationCall.respondNoContent(outcome: Outcome<Unit, E>, toProblem: (E) -> ApiProblem) {
    when (outcome) {
        is Outcome.Success -> respond(HttpStatusCode.NoContent)
        is Outcome.Failure -> respondProblem(toProblem(outcome.error))
    }
}
