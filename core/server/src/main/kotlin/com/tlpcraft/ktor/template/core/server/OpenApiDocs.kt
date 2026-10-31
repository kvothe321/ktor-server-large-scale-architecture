package com.tlpcraft.ktor.template.core.server

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.Responses
import io.ktor.openapi.jsonSchema

/** Documents a problem response for [status] with the shared [ApiProblem] schema. */
public fun Responses.Builder.problem(status: HttpStatusCode, description: String) {
    response(status.value) {
        this.description = description
        ContentType.Application.ProblemJson {
            schema = jsonSchema<ApiProblem>()
        }
    }
}
