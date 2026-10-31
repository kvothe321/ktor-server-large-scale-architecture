package com.tlpcraft.ktor.template.e2e

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
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/** Works against a shared server: every test creates its own data and never assumes an empty database. */
class NotesE2eTest {
    private val client = e2eClient()

    @AfterTest
    fun close() = client.close()

    private suspend fun HttpClient.createNote(input: NoteInput): HttpResponse = post("api/v1/notes") {
        contentType(ContentType.Application.Json)
        setBody(input)
    }

    @Test
    fun `a note goes through its whole lifecycle`() = runTest {
        val created = client.createNote(NoteInput("E2E groceries", "Milk")).run {
            assertEquals(HttpStatusCode.Created, status)
            body<Note>()
        }
        assertEquals(created, client.get("api/v1/notes/${created.id}").body<Note>())

        val listed = client.get("api/v1/notes?page=1&size=100").body<PageOf<Note>>()
        assertTrue(listed.items.any { it.id == created.id })

        val updated = client.put("api/v1/notes/${created.id}") {
            contentType(ContentType.Application.Json)
            setBody(NoteInput("E2E groceries (done)", "Bought"))
        }.body<Note>()
        assertEquals("E2E groceries (done)", updated.title)
        assertEquals(created.createdAt, updated.createdAt)

        assertEquals(HttpStatusCode.NoContent, client.delete("api/v1/notes/${created.id}").status)
        val gone = client.get("api/v1/notes/${created.id}")
        assertEquals(HttpStatusCode.NotFound, gone.status)
        assertEquals("Note ${created.id} does not exist.", gone.body<Problem>().detail)
    }

    @Test
    fun `invalid input is rejected with every violation`() = runTest {
        val response = client.createNote(NoteInput(title = " ", content = "x".repeat(10_001)))

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(listOf("title", "content"), response.body<Problem>().violations.map { it.field })
    }

    @Test
    fun `malformed JSON is a bad request`() = runTest {
        val response = client.post("api/v1/notes") {
            contentType(ContentType.Application.Json)
            setBody("""{"title": """)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `paging parameters are validated`() = runTest {
        val response = client.get("api/v1/notes?page=0&size=500")

        assertEquals(listOf("page", "size"), response.body<Problem>().violations.map { it.field })
    }
}
