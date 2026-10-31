package com.tlpcraft.ktor.template.feature.images.domain.usecase

import com.tlpcraft.ktor.template.core.testing.MutableClock
import com.tlpcraft.ktor.template.core.testing.assertFailure
import com.tlpcraft.ktor.template.core.testing.assertSuccess
import com.tlpcraft.ktor.template.feature.images.domain.FakeImageContentStore
import com.tlpcraft.ktor.template.feature.images.domain.FakeImageRepository
import com.tlpcraft.ktor.template.feature.images.domain.ImageError
import com.tlpcraft.ktor.template.feature.images.domain.ImageFormat
import com.tlpcraft.ktor.template.feature.images.domain.ImageId
import com.tlpcraft.ktor.template.feature.images.domain.ImageUpload
import com.tlpcraft.ktor.template.feature.images.domain.ImageUploadPolicy
import com.tlpcraft.ktor.template.feature.images.domain.TestImages
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class ImageUseCasesTest {
    private val clock = MutableClock()
    private val repository = FakeImageRepository()
    private val store = FakeImageContentStore()
    private val policy = ImageUploadPolicy(maxSizeBytes = 64)
    private val upload = UploadImageUseCase(repository, store, policy, clock)

    @Test
    fun `upload stores bytes and metadata with a sanitised filename`() = runTest {
        val metadata = upload(ImageUpload("../../etc/cat.png", TestImages.png)).assertSuccess()

        assertEquals("cat.png", metadata.originalFilename)
        assertEquals(ImageFormat.PNG, metadata.format)
        assertEquals(TestImages.png.size.toLong(), metadata.sizeBytes)
        assertEquals(clock.now(), metadata.uploadedAt)
        assertEquals(listOf(metadata), repository.all)
        assertEquals(setOf(metadata.id), store.storedIds)
    }

    @Test
    fun `upload over the size limit is TooLarge`() = runTest {
        assertEquals(64, upload(ImageUpload("big.png", TestImages.png + ByteArray(64))).assertFailure<ImageError.TooLarge>().maxSizeBytes)
        assertTrue(store.storedIds.isEmpty())
    }

    @Test
    fun `upload of a non-image is UnsupportedFormat even with an image name`() = runTest {
        upload(ImageUpload("fake.png", TestImages.text)).assertFailure<ImageError.UnsupportedFormat>()
    }

    @Test
    fun `upload of a format outside the policy is UnsupportedFormat`() = runTest {
        val pngOnly = UploadImageUseCase(repository, store, ImageUploadPolicy(allowedFormats = setOf(ImageFormat.PNG)), clock)

        assertEquals(setOf(ImageFormat.PNG), pngOnly(ImageUpload("a.gif", TestImages.gif)).assertFailure<ImageError.UnsupportedFormat>().allowed)
    }

    @Test
    fun `empty file and blank name are validation errors`() = runTest {
        val error = upload(ImageUpload("  ", ByteArray(0))).assertFailure<ImageError.Invalid>()

        assertEquals(listOf("file", "filename"), error.error.violations.map { it.field })
    }

    @Test
    fun `stored bytes are removed when metadata cannot be saved`() = runTest {
        repository.failure = ImageError.Unavailable(IllegalStateException("db down"))

        upload(ImageUpload("cat.png", TestImages.png)).assertFailure<ImageError.Unavailable>()

        assertTrue(store.storedIds.isEmpty())
    }

    @Test
    fun `download returns metadata and bytes`() = runTest {
        val metadata = upload(ImageUpload("cat.png", TestImages.png)).assertSuccess()

        val content = DownloadImageUseCase(repository, store)(metadata.id).assertSuccess()

        assertEquals(metadata, content.metadata)
        assertContentEquals(TestImages.png, content.bytes)
    }

    @Test
    fun `delete removes metadata and bytes`() = runTest {
        val metadata = upload(ImageUpload("cat.png", TestImages.png)).assertSuccess()

        DeleteImageUseCase(repository, store)(metadata.id).assertSuccess()

        assertTrue(repository.all.isEmpty())
        assertTrue(store.storedIds.isEmpty())
        GetImageUseCase(repository)(metadata.id).assertFailure<ImageError.NotFound>()
    }

    @Test
    fun `delete succeeds even if removing the bytes fails`() = runTest {
        val metadata = upload(ImageUpload("cat.png", TestImages.png)).assertSuccess()
        store.failure = ImageError.Unavailable(IllegalStateException("disk"))

        DeleteImageUseCase(repository, store)(metadata.id).assertSuccess()

        assertTrue(repository.all.isEmpty())
    }

    @Test
    fun `delete of an unknown image is NotFound`() = runTest {
        DeleteImageUseCase(repository, store)(ImageId.random()).assertFailure<ImageError.NotFound>()
    }
}
