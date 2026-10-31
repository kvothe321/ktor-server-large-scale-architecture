package com.tlpcraft.ktor.template.e2e

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class PlatformE2eTest {
    private val client = e2eClient()

    @AfterTest
    fun close() = client.close()

    @Test
    fun `health is UP with a database check`() = runTest {
        val response = client.get("health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(Health("UP", mapOf("database" to "UP")), response.body<Health>())
    }

    @Test
    fun `request ids are echoed back`() = runTest {
        val response = client.get("health") { header(HttpHeaders.XRequestId, "e2e-trace-1") }

        assertEquals("e2e-trace-1", response.headers[HttpHeaders.XRequestId])
    }

    @Test
    fun `OpenAPI document describes every public endpoint`() = runTest {
        val document = client.get("swagger/openapi.json").body<JsonObject>()
        val paths = assertNotNull(document["paths"]).jsonObject.keys

        assertTrue(
            paths.containsAll(
                setOf(
                    "/health",
                    "/api/v1/weather/current",
                    "/api/v1/notes",
                    "/api/v1/notes/{id}",
                    "/api/v1/images",
                    "/api/v1/images/{id}",
                    "/api/v1/images/{id}/content"
                )
            ),
            "Missing paths in $paths"
        )
    }

    @Test
    fun `unknown routes answer with a problem document`() = runTest {
        val response = client.get("api/v1/does-not-exist")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(ContentType.Application.ProblemJson, response.contentType()?.withoutParameters())
        assertEquals(404, response.body<Problem>().status)
    }
}
