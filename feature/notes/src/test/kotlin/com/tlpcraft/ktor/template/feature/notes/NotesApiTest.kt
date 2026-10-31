package com.tlpcraft.ktor.template.feature.notes

import com.tlpcraft.ktor.template.core.server.ApiProblem
import com.tlpcraft.ktor.template.core.server.PageResponse
import com.tlpcraft.ktor.template.core.testing.TestEnvironment
import com.tlpcraft.ktor.template.core.testing.testFeature
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

class NotesApiTest {
    @Serializable
    private data class NoteBody(val title: String, val content: String)

    @Serializable
    private data class NoteJson(val id: Uuid, val title: String, val content: String, val createdAt: Instant, val updatedAt: Instant)

    private val environment = TestEnvironment()

    private fun notesTest(block: suspend (HttpClient) -> Unit) = testFeature(NotesFeature, environment) { client -> block(client) }

    private suspend fun HttpClient.createNote(title: String, content: String = "body"): HttpResponse = post("/api/v1/notes") {
        contentType(ContentType.Application.Json)
        setBody(NoteBody(title, content))
    }

    @Test
    fun `POST creates a note and GET returns it`() = notesTest { client ->
        val response = client.createNote("Groceries", "Milk")
        assertEquals(HttpStatusCode.Created, response.status)
        val created = response.body<NoteJson>()

        val fetched = client.get("/api/v1/notes/${created.id}")

        assertEquals(HttpStatusCode.OK, fetched.status)
        assertEquals(created, fetched.body<NoteJson>())
        assertEquals(environment.clock.now(), created.createdAt)
    }

    @Test
    fun `POST with invalid fields lists every violation`() = notesTest { client ->
        val response = client.createNote(title = "   ", content = "x".repeat(10_001))

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(listOf("title", "content"), response.body<ApiProblem>().violations.map { it.field })
    }

    @Test
    fun `POST with malformed JSON is a 400 problem`() = notesTest { client ->
        val response = client.post("/api/v1/notes") {
            contentType(ContentType.Application.Json)
            setBody("""{"content": "missing title"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ContentType.Application.ProblemJson, response.contentType()?.withoutParameters())
    }

    @Test
    fun `GET list paginates newest first`() = notesTest { client ->
        repeat(3) { index ->
            client.createNote("note $index")
            environment.clock.advanceBy(1.minutes)
        }

        val firstPage = client.get("/api/v1/notes?page=1&size=2").body<PageResponse<NoteJson>>()
        val secondPage = client.get("/api/v1/notes?page=2&size=2").body<PageResponse<NoteJson>>()

        assertEquals(listOf("note 2", "note 1"), firstPage.items.map { it.title })
        assertEquals(listOf("note 0"), secondPage.items.map { it.title })
        assertEquals(3, firstPage.totalItems)
        assertEquals(2, firstPage.totalPages)
    }

    @Test
    fun `GET list rejects invalid paging`() = notesTest { client ->
        val response = client.get("/api/v1/notes?size=0")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(listOf("size"), response.body<ApiProblem>().violations.map { it.field })
    }

    @Test
    fun `PUT replaces content and bumps updatedAt`() = notesTest { client ->
        val created = client.createNote("Draft").body<NoteJson>()
        environment.clock.advanceBy(5.minutes)

        val response = client.put("/api/v1/notes/${created.id}") {
            contentType(ContentType.Application.Json)
            setBody(NoteBody("Final", "Done"))
        }

        val updated = response.body<NoteJson>()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Final", updated.title)
        assertEquals(created.createdAt, updated.createdAt)
        assertEquals(environment.clock.now(), updated.updatedAt)
    }

    @Test
    fun `DELETE removes the note`() = notesTest { client ->
        val created = client.createNote("Temporary").body<NoteJson>()

        assertEquals(HttpStatusCode.NoContent, client.delete("/api/v1/notes/${created.id}").status)
        assertEquals(HttpStatusCode.NotFound, client.get("/api/v1/notes/${created.id}").status)
        assertEquals(HttpStatusCode.NotFound, client.delete("/api/v1/notes/${created.id}").status)
    }

    @Test
    fun `unknown and malformed ids are 404 and 400`() = notesTest { client ->
        assertEquals(HttpStatusCode.NotFound, client.get("/api/v1/notes/${Uuid.random()}").status)
        val malformed = client.get("/api/v1/notes/not-a-uuid")
        assertEquals(HttpStatusCode.BadRequest, malformed.status)
        assertEquals(listOf("id"), malformed.body<ApiProblem>().violations.map { it.field })
    }
}
