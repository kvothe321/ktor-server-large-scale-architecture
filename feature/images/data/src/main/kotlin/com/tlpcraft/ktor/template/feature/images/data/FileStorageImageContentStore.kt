package com.tlpcraft.ktor.template.feature.images.data

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.mapError
import com.tlpcraft.ktor.template.core.domain.onFailure
import com.tlpcraft.ktor.template.core.logger.Logger
import com.tlpcraft.ktor.template.core.storage.FileStorage
import com.tlpcraft.ktor.template.core.storage.StorageError
import com.tlpcraft.ktor.template.core.storage.StorageKey
import com.tlpcraft.ktor.template.feature.images.domain.ImageContentStore
import com.tlpcraft.ktor.template.feature.images.domain.ImageError
import com.tlpcraft.ktor.template.feature.images.domain.ImageId

/** Stores image bytes in [FileStorage], one blob per image id. */
internal class FileStorageImageContentStore(
    private val storage: FileStorage,
    private val logger: Logger
) : ImageContentStore {
    override suspend fun put(id: ImageId, bytes: ByteArray): Outcome<Unit, ImageError> = storage.write(id.toKey(), bytes).toImageError(id, "write")

    override suspend fun get(id: ImageId): Outcome<ByteArray, ImageError> = storage.read(id.toKey()).toImageError(id, "read")

    override suspend fun delete(id: ImageId): Outcome<Unit, ImageError> = storage.delete(id.toKey()).toImageError(id, "delete")

    private fun <T> Outcome<T, StorageError>.toImageError(id: ImageId, operation: String): Outcome<T, ImageError> = onFailure { error ->
        if (error is StorageError.Io) logger.error("Image content $operation failed", "imageId" to id.value, throwable = error.cause)
    }.mapError { error ->
        when (error) {
            is StorageError.NotFound -> ImageError.NotFound(id)
            is StorageError.Io -> ImageError.Unavailable(error.cause)
        }
    }

    private fun ImageId.toKey(): StorageKey = StorageKey(value.toString())
}
