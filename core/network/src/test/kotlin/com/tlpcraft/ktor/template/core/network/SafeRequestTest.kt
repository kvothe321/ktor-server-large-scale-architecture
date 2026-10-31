package com.tlpcraft.ktor.template.core.network

import com.tlpcraft.ktor.template.core.testing.assertFailure
import com.tlpcraft.ktor.template.core.testing.assertSuccess
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable

class SafeRequestTest {
    @Serializable
    private data class Greeting(val text: String)

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun client(engine: MockEngine) = createHttpClient(engine = engine)

    @Test
    fun `decodes a successful response and ignores unknown fields`() = runTest {
        val client = client(MockEngine { respond("""{"text":"hi","extra":1}""", HttpStatusCode.OK, jsonHeaders) })

        assertEquals(Greeting("hi"), safeRequest<Greeting> { client.get("https://example.test") }.assertSuccess())
    }

    @Test
    fun `non-2xx status becomes HttpStatus`() = runTest {
        val client = client(MockEngine { respond("nope", HttpStatusCode.BadGateway) })

        val error = safeRequest<Greeting> { client.get("https://example.test") }.assertFailure<NetworkError.HttpStatus>()

        assertEquals(502, error.code)
        assertEquals("nope", error.body)
    }

    @Test
    fun `transport failure becomes Unreachable`() = runTest {
        val client = client(MockEngine { throw IOException("connection refused") })

        safeRequest<Greeting> { client.get("https://example.test") }.assertFailure<NetworkError.Unreachable>()
    }

    @Test
    fun `malformed body becomes InvalidResponse`() = runTest {
        val client = client(MockEngine { respond("""{"wrong":true}""", HttpStatusCode.OK, jsonHeaders) })

        safeRequest<Greeting> { client.get("https://example.test") }.assertFailure<NetworkError.InvalidResponse>()
    }
}
