package com.tlpcraft.ktor.template.core.server

import com.tlpcraft.ktor.template.core.testing.LogEvent
import com.tlpcraft.ktor.template.core.testing.RecordingLoggerFactory
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorHandlingTest {
    private val loggers = RecordingLoggerFactory()

    private fun problemTest(block: suspend io.ktor.server.testing.ApplicationTestBuilder.() -> Unit) = testApplication {
        application {
            configureSerialization()
            configureErrorHandling(loggers)
            routing {
                get("/boom") { error("kaboom") }
                get("/thing") { call.respondProblem(ApiProblem.notFound("Thing 42 does not exist.")) }
                get("/page") {
                    val page = call.pageRequest()
                    call.respondOutcome(page, toProblem = ApiProblem::validation) { "page ${it.page} size ${it.size}" }
                }
            }
        }
        block()
    }

    private fun decode(body: String) = AppJson.decodeFromString(ApiProblem.serializer(), body)

    @Test
    fun `unknown route is a problem+json 404`() = problemTest {
        val response = client.get("/missing")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(ContentType.Application.ProblemJson, response.contentType()?.withoutParameters())
        assertEquals("urn:problem-type:not-found", decode(response.bodyAsText()).type)
    }

    @Test
    fun `a route's own 404 problem is not replaced by the fallback`() = problemTest {
        val response = client.get("/thing")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("Thing 42 does not exist.", decode(response.bodyAsText()).detail)
    }

    @Test
    fun `wrong method is a 405 problem`() = problemTest {
        assertEquals(HttpStatusCode.MethodNotAllowed, client.post("/boom").status)
    }

    @Test
    fun `unhandled exception is logged and hidden behind a generic 500`() = problemTest {
        val response = client.get("/boom")

        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertEquals("An unexpected error occurred.", decode(response.bodyAsText()).detail)
        val event = loggers.events.single()
        assertEquals(LogEvent.Level.ERROR, event.level)
        assertEquals("/boom", event.fields["path"])
    }

    @Test
    fun `invalid paging parameters are reported per field`() = problemTest {
        val response = client.get("/page?page=abc&size=1000")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(listOf("page"), decode(response.bodyAsText()).violations.map { it.field })
    }

    @Test
    fun `out of range page size is a validation problem`() = problemTest {
        val response = client.get("/page?page=1&size=1000")

        assertEquals(listOf("size"), decode(response.bodyAsText()).violations.map { it.field })
    }
}
