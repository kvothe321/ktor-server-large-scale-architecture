package com.tlpcraft.ktor.template.feature.images.domain

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.asFailure
import com.tlpcraft.ktor.template.core.domain.asSuccess
import com.tlpcraft.ktor.template.core.model.Page
import com.tlpcraft.ktor.template.core.model.PageRequest

/** In-memory [ImageRepository]. Set [failure] to make every call fail with it. */
class FakeImageRepository : ImageRepository {
    private val images = mutableMapOf<ImageId, ImageMetadata>()

    var failure: ImageError? = null

    val all: List<ImageMetadata> get() = images.values.toList()

    override suspend fun insert(metadata: ImageMetadata): Outcome<ImageMetadata, ImageError> = guarded {
        images[metadata.id] = metadata
        metadata.asSuccess()
    }

    override suspend fun find(id: ImageId): Outcome<ImageMetadata, ImageError> = guarded {
        images[id]?.asSuccess() ?: ImageError.NotFound(id).asFailure()
    }

    override suspend fun list(request: PageRequest): Outcome<Page<ImageMetadata>, ImageError> = guarded {
        val sorted = images.values.sortedByDescending { it.uploadedAt }
        Page(sorted.drop(request.offset.toInt()).take(request.size), request, sorted.size.toLong()).asSuccess()
    }

    override suspend fun delete(id: ImageId): Outcome<Unit, ImageError> = guarded {
        images.remove(id)?.let { Unit.asSuccess() } ?: ImageError.NotFound(id).asFailure()
    }

    private inline fun <T> guarded(block: () -> Outcome<T, ImageError>): Outcome<T, ImageError> = failure?.asFailure() ?: block()
}

/** In-memory [ImageContentStore]. Set [failure] to make every call fail with it. */
class FakeImageContentStore : ImageContentStore {
    private val contents = mutableMapOf<ImageId, ByteArray>()

    var failure: ImageError? = null

    val storedIds: Set<ImageId> get() = contents.keys.toSet()

    override suspend fun put(id: ImageId, bytes: ByteArray): Outcome<Unit, ImageError> = guarded {
        contents[id] = bytes.copyOf()
        Unit.asSuccess()
    }

    override suspend fun get(id: ImageId): Outcome<ByteArray, ImageError> = guarded {
        contents[id]?.copyOf()?.asSuccess() ?: ImageError.NotFound(id).asFailure()
    }

    override suspend fun delete(id: ImageId): Outcome<Unit, ImageError> = guarded {
        contents.remove(id)
        Unit.asSuccess()
    }

    private inline fun <T> guarded(block: () -> Outcome<T, ImageError>): Outcome<T, ImageError> = failure?.asFailure() ?: block()
}

/** Minimal byte sequences that pass signature detection. */
object TestImages {
    val png: ByteArray = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x01)
    val jpeg: ByteArray = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0x00)
    val gif: ByteArray = "GIF89a".encodeToByteArray() + byteArrayOf(0x01, 0x00)
    val webp: ByteArray = "RIFF".encodeToByteArray() + byteArrayOf(0x24, 0, 0, 0) + "WEBPVP8 ".encodeToByteArray()
    val text: ByteArray = "definitely not an image".encodeToByteArray()
}
