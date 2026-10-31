package com.tlpcraft.ktor.template.app

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.io.TempDir

/** Boots the real application module (all features, real wiring) on an in-memory database. */
class ApplicationTest {
    @TempDir
    lateinit var storageRoot: Path

    private fun appTest(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        environment {
            config = MapApplicationConfig(
                "database.jdbcUrl" to "jdbc:h2:mem:app-${Uuid.random()};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "database.username" to "sa",
                "database.password" to "",
                "database.maximumPoolSize" to "2",
                "storage.root" to storageRoot.toString(),
                "httpClient.requestTimeout" to "2s",
                "httpClient.connectTimeout" to "1s"
            )
        }
        application { module() }
        block()
    }

    @Test
    fun `health reports UP with the database check`() = appTest {
        val response = client.get("/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"status":"UP","checks":{"database":"UP"}}""", response.bodyAsText())
    }

    @Test
    fun `every feature is wired and reachable`() = appTest {
        assertEquals(HttpStatusCode.OK, client.get("/api/v1/notes").status)
        assertEquals(HttpStatusCode.OK, client.get("/api/v1/images").status)
        assertEquals(HttpStatusCode.BadRequest, client.get("/api/v1/weather/current").status)
    }

    @Test
    fun `a valid client request id is echoed, otherwise one is generated`() = appTest {
        val echoed = client.get("/health") { header(HttpHeaders.XRequestId, "trace-123") }
        val generated = client.get("/health") { header(HttpHeaders.XRequestId, "not valid!") }

        assertEquals("trace-123", echoed.headers[HttpHeaders.XRequestId])
        val generatedId = assertNotNull(generated.headers[HttpHeaders.XRequestId])
        assertNotNull(Uuid.parseOrNull(generatedId))
    }

    @Test
    fun `OpenAPI document covers every endpoint and Swagger UI is served`() = appTest {
        val document = Json.parseToJsonElement(client.get("/swagger/openapi.json").bodyAsText()).jsonObject
        val paths = assertNotNull(document["paths"] as? JsonObject).keys

        assertEquals(
            setOf(
                "/health",
                "/api/v1/weather/current",
                "/api/v1/notes",
                "/api/v1/notes/{id}",
                "/api/v1/images",
                "/api/v1/images/{id}",
                "/api/v1/images/{id}/content"
            ),
            paths
        )
        assertTrue(client.get("/swagger").bodyAsText().contains("swagger-ui"))
    }
}
