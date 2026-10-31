package com.tlpcraft.ktor.template.feature.notes.presentation

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.flatMap
import com.tlpcraft.ktor.template.core.domain.map
import com.tlpcraft.ktor.template.core.domain.mapError
import com.tlpcraft.ktor.template.core.server.pageRequest
import com.tlpcraft.ktor.template.core.server.respondNoContent
import com.tlpcraft.ktor.template.core.server.respondOutcome
import com.tlpcraft.ktor.template.core.server.toResponse
import com.tlpcraft.ktor.template.core.server.uuidParameter
import com.tlpcraft.ktor.template.feature.notes.domain.NoteError
import com.tlpcraft.ktor.template.feature.notes.domain.NoteId
import com.tlpcraft.ktor.template.feature.notes.domain.usecase.CreateNoteUseCase
import com.tlpcraft.ktor.template.feature.notes.domain.usecase.DeleteNoteUseCase
import com.tlpcraft.ktor.template.feature.notes.domain.usecase.GetNoteUseCase
import com.tlpcraft.ktor.template.feature.notes.domain.usecase.ListNotesUseCase
import com.tlpcraft.ktor.template.feature.notes.domain.usecase.UpdateNoteUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/** HTTP endpoints of the notes feature, mounted at `/notes`. */
class NoteRoutes(
    private val createNote: CreateNoteUseCase,
    private val getNote: GetNoteUseCase,
    private val listNotes: ListNotesUseCase,
    private val updateNote: UpdateNoteUseCase,
    private val deleteNote: DeleteNoteUseCase
) {
    fun register(route: Route) {
        route.route("/notes") {
            post {
                val request = call.receive<NoteRequest>()
                call.respondOutcome(createNote(request.toDraft()), HttpStatusCode.Created, NoteError::toProblem) { it.toResponse() }
            }.describe(NoteApiDocs.create)

            get {
                val page = call.pageRequest().mapError(NoteError::Invalid).flatMap { listNotes(it) }
                call.respondOutcome(page, toProblem = NoteError::toProblem) { notes -> notes.toResponse { it.toResponse() } }
            }.describe(NoteApiDocs.list)

            route("/{id}") {
                get {
                    val note = call.noteId().flatMap { getNote(it) }
                    call.respondOutcome(note, toProblem = NoteError::toProblem) { it.toResponse() }
                }.describe(NoteApiDocs.get)

                put {
                    val request = call.receive<NoteRequest>()
                    val note = call.noteId().flatMap { updateNote(it, request.toDraft()) }
                    call.respondOutcome(note, toProblem = NoteError::toProblem) { it.toResponse() }
                }.describe(NoteApiDocs.update)

                delete {
                    call.respondNoContent(call.noteId().flatMap { deleteNote(it) }, NoteError::toProblem)
                }.describe(NoteApiDocs.delete)
            }
        }
    }

    private fun ApplicationCall.noteId(): Outcome<NoteId, NoteError> = uuidParameter("id").map(::NoteId).mapError(NoteError::Invalid)
}
