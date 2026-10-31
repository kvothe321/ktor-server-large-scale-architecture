package com.tlpcraft.ktor.template.feature.images

import com.tlpcraft.ktor.template.core.data.DefaultDispatcherProvider
import com.tlpcraft.ktor.template.core.server.ApiProblem
import com.tlpcraft.ktor.template.core.server.PageResponse
import com.tlpcraft.ktor.template.core.storage.FileStorage
import com.tlpcraft.ktor.template.core.storage.LocalFileStorage
import com.tlpcraft.ktor.template.core.testing.testFeature
import com.tlpcraft.ktor.template.feature.images.domain.TestImages
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
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.io.TempDir
import org.koin.dsl.module

class ImagesApiTest {
    @Serializable
    private data class ImageJson(val id: Uuid, val filename: String, val contentType: String, val sizeBytes: Long)

    @TempDir
    lateinit var storageRoot: Path

    private fun imagesTest(block: suspend (HttpClient) -> Unit) = testFeature(
        feature = ImagesFeature,
        config = mapOf("images.maxSizeBytes" to "1024"),
        extraModules = listOf(module { single<FileStorage> { LocalFileStorage(storageRoot, DefaultDispatcherProvider) } })
    ) { client -> block(client) }

    private suspend fun HttpClient.upload(bytes: ByteArray, filename: String = "cat.png", field: String = "file"): HttpResponse = submitFormWithBinaryData(
        url = "/api/v1/images",
        formData = formData {
            append(
                field,
                bytes,
                Headers.build {
                    append(HttpHeaders.ContentType, "application/octet-stream")
                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                }
            )
        }
    )

    @Test
    fun `upload stores the image and returns its metadata`() = imagesTest { client ->
        val response = client.upload(TestImages.png)

        assertEquals(HttpStatusCode.Created, response.status)
        val image = response.body<ImageJson>()
        assertEquals(ImageJson(image.id, "cat.png", "image/png", TestImages.png.size.toLong()), image)
        assertEquals("/api/v1/images/${image.id}", response.headers[HttpHeaders.Location])
        assertEquals(listOf(storageRoot.resolve(image.id.toString())), storageRoot.listDirectoryEntries())
    }

    @Test
    fun `download returns the original bytes with the detected content type`() = imagesTest { client ->
        val image = client.upload(TestImages.jpeg, filename = "photo.jpg").body<ImageJson>()

        val response = client.get("/api/v1/images/${image.id}/content")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ContentType.Image.JPEG, response.contentType())
        assertContentEquals(TestImages.jpeg, response.readRawBytes())
        assertEquals("inline; filename=photo.jpg", response.headers[HttpHeaders.ContentDisposition])
    }

    @Test
    fun `metadata and list endpoints return uploaded images`() = imagesTest { client ->
        val first = client.upload(TestImages.gif, "a.gif").body<ImageJson>()
        val second = client.upload(TestImages.webp, "b.webp").body<ImageJson>()

        assertEquals(first, client.get("/api/v1/images/${first.id}").body<ImageJson>())
        val page = client.get("/api/v1/images?size=10").body<PageResponse<ImageJson>>()
        assertEquals(setOf(first, second), page.items.toSet())
        assertEquals(2, page.totalItems)
    }

    @Test
    fun `oversized upload is 413`() = imagesTest { client ->
        val response = client.upload(TestImages.png + ByteArray(2048))

        assertEquals(HttpStatusCode.PayloadTooLarge, response.status)
        assertTrue(storageRoot.listDirectoryEntries().isEmpty())
    }

    @Test
    fun `non-image content is 415 regardless of its name`() = imagesTest { client ->
        val response = client.upload(TestImages.text, filename = "evil.png")

        assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
        assertEquals("urn:problem-type:unsupported-media-type", response.body<ApiProblem>().type)
    }

    @Test
    fun `missing file part is a validation problem`() = imagesTest { client ->
        val response = client.upload(TestImages.png, field = "attachment")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(listOf("file"), response.body<ApiProblem>().violations.map { it.field })
    }

    @Test
    fun `delete removes metadata and file`() = imagesTest { client ->
        val image = client.upload(TestImages.png).body<ImageJson>()

        assertEquals(HttpStatusCode.NoContent, client.delete("/api/v1/images/${image.id}").status)
        assertEquals(HttpStatusCode.NotFound, client.get("/api/v1/images/${image.id}").status)
        assertEquals(HttpStatusCode.NotFound, client.get("/api/v1/images/${image.id}/content").status)
        assertTrue(storageRoot.listDirectoryEntries().isEmpty())
    }
}
