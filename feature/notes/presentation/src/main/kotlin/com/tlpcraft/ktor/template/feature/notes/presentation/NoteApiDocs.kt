package com.tlpcraft.ktor.template.feature.notes.presentation

import com.tlpcraft.ktor.template.core.server.PageResponse
import com.tlpcraft.ktor.template.core.server.problem
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.Operation
import io.ktor.openapi.jsonSchema
import io.ktor.server.routing.openapi.RouteOperationFunction

/** OpenAPI descriptions of the notes endpoints, kept apart from the handlers in [NoteRoutes]. */
internal object NoteApiDocs {
    private const val TAG = "Notes"

    val create: RouteOperationFunction = {
        tag(TAG)
        summary = "Create a note"
        requestBody { schema = jsonSchema<NoteRequest>() }
        responses {
            HttpStatusCode.Created {
                description = "The created note"
                schema = jsonSchema<NoteResponse>()
            }
            problem(HttpStatusCode.BadRequest, "Malformed body or invalid fields")
        }
    }

    val list: RouteOperationFunction = {
        tag(TAG)
        summary = "List notes, newest first"
        parameters {
            query("page") { description = "1-based page number (default 1)" }
            query("size") { description = "Page size, 1 to 100 (default 20)" }
        }
        responses {
            HttpStatusCode.OK {
                description = "One page of notes"
                schema = jsonSchema<PageResponse<NoteResponse>>()
            }
            problem(HttpStatusCode.BadRequest, "Invalid paging parameters")
        }
    }

    val get: RouteOperationFunction = {
        tag(TAG)
        summary = "Get a note"
        idParameter()
        responses {
            HttpStatusCode.OK {
                description = "The note"
                schema = jsonSchema<NoteResponse>()
            }
            problem(HttpStatusCode.NotFound, "No note with this id")
        }
    }

    val update: RouteOperationFunction = {
        tag(TAG)
        summary = "Replace a note's title and content"
        idParameter()
        requestBody { schema = jsonSchema<NoteRequest>() }
        responses {
            HttpStatusCode.OK {
                description = "The updated note"
                schema = jsonSchema<NoteResponse>()
            }
            problem(HttpStatusCode.BadRequest, "Malformed body or invalid fields")
            problem(HttpStatusCode.NotFound, "No note with this id")
        }
    }

    val delete: RouteOperationFunction = {
        tag(TAG)
        summary = "Delete a note"
        idParameter()
        responses {
            HttpStatusCode.NoContent { description = "Deleted" }
            problem(HttpStatusCode.NotFound, "No note with this id")
        }
    }

    private fun Operation.Builder.idParameter() = parameters {
        path("id") { description = "Note id (UUID)" }
    }
}
