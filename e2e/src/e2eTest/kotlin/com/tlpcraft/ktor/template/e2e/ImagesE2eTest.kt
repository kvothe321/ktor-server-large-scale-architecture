package com.tlpcraft.ktor.template.e2e

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class ImagesE2eTest {
    private val client = e2eClient()

    /** A complete, valid 1x1 PNG. */
    private val png = byteArrayOf(
        0x89.toByte(),
        0x50,
        0x4E,
        0x47,
        0x0D,
        0x0A,
        0x1A,
        0x0A,
        0x00,
        0x00,
        0x00,
        0x0D,
        0x49,
        0x48,
        0x44,
        0x52,
        0x00,
        0x00,
        0x00,
        0x01,
        0x00,
        0x00,
        0x00,
        0x01,
        0x08,
        0x02,
        0x00,
        0x00,
        0x00,
        0x90.toByte(),
        0x77,
        0x53,
        0xDE.toByte(),
        0x00,
        0x00,
        0x00,
        0x0C,
        0x49,
        0x44,
        0x41,
        0x54,
        0x78,
        0x9C.toByte(),
        0x63,
        0xF8.toByte(),
        0xCF.toByte(),
        0xC0.toByte(),
        0x00,
        0x00,
        0x03,
        0x01,
        0x01,
        0x00,
        0xC9.toByte(),
        0xFE.toByte(),
        0x92.toByte(),
        0xEF.toByte(),
        0x00,
        0x00,
        0x00,
        0x00,
        0x49,
        0x45,
        0x4E,
        0x44,
        0xAE.toByte(),
        0x42,
        0x60,
        0x82.toByte()
    )

    @AfterTest
    fun close() = client.close()

    private suspend fun HttpClient.upload(bytes: ByteArray, filename: String): HttpResponse = submitFormWithBinaryData(
        url = "api/v1/images",
        formData = formData {
            append(
                "file",
                bytes,
                Headers.build {
                    append(HttpHeaders.ContentType, "application/octet-stream")
                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                }
            )
        }
    )

    @Test
    fun `an image can be uploaded, inspected, downloaded and deleted`() = runTest {
        val response = client.upload(png, "pixel.png")
        assertEquals(HttpStatusCode.Created, response.status)
        val image = response.body<Image>()
        assertEquals("image/png", image.contentType)
        assertEquals(png.size.toLong(), image.sizeBytes)
        assertTrue(response.headers[HttpHeaders.Location].orEmpty().endsWith("/api/v1/images/${image.id}"))

        assertEquals(image, client.get("api/v1/images/${image.id}").body<Image>())
        assertTrue(client.get("api/v1/images?size=100").body<PageOf<Image>>().items.any { it.id == image.id })

        val download = client.get("api/v1/images/${image.id}/content")
        assertEquals(ContentType.Image.PNG, download.contentType())
        assertContentEquals(png, download.readRawBytes())

        assertEquals(HttpStatusCode.NoContent, client.delete("api/v1/images/${image.id}").status)
        assertEquals(HttpStatusCode.NotFound, client.get("api/v1/images/${image.id}/content").status)
    }

    @Test
    fun `content that is not an image is rejected whatever its name`() = runTest {
        val response = client.upload("plain text".encodeToByteArray(), "fake.png")

        assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
    }

    @Test
    fun `files over the default 5 MiB limit are rejected`() = runTest {
        val response = client.upload(png + ByteArray(6 * 1024 * 1024), "huge.png")

        assertEquals(HttpStatusCode.PayloadTooLarge, response.status)
    }
}
