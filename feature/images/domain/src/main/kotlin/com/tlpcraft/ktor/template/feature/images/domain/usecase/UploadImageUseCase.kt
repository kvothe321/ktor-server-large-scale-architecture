package com.tlpcraft.ktor.template.feature.images.domain.usecase

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.ValidationError
import com.tlpcraft.ktor.template.core.domain.flatMap
import com.tlpcraft.ktor.template.core.domain.mapError
import com.tlpcraft.ktor.template.core.domain.onFailure
import com.tlpcraft.ktor.template.core.domain.validated
import com.tlpcraft.ktor.template.feature.images.domain.ImageContentStore
import com.tlpcraft.ktor.template.feature.images.domain.ImageError
import com.tlpcraft.ktor.template.feature.images.domain.ImageFormat
import com.tlpcraft.ktor.template.feature.images.domain.ImageId
import com.tlpcraft.ktor.template.feature.images.domain.ImageMetadata
import com.tlpcraft.ktor.template.feature.images.domain.ImageRepository
import com.tlpcraft.ktor.template.feature.images.domain.ImageUpload
import com.tlpcraft.ktor.template.feature.images.domain.ImageUploadPolicy
import kotlin.time.Clock

/**
 * Validates an upload against [policy], stores its bytes and then its metadata. If the metadata
 * cannot be saved, the stored bytes are removed again so no orphan file is left behind.
 */
class UploadImageUseCase(
    private val repository: ImageRepository,
    private val contentStore: ImageContentStore,
    private val policy: ImageUploadPolicy,
    private val clock: Clock
) {
    suspend operator fun invoke(upload: ImageUpload): Outcome<ImageMetadata, ImageError> = validate(upload).flatMap { metadata ->
        contentStore.put(metadata.id, upload.bytes).flatMap {
            repository.insert(metadata).onFailure { contentStore.delete(metadata.id) }
        }
    }

    private fun validate(upload: ImageUpload): Outcome<ImageMetadata, ImageError> {
        val size = upload.bytes.size.toLong()
        if (size > policy.maxSizeBytes) return Outcome.Failure(ImageError.TooLarge(policy.maxSizeBytes))
        val filename = upload.filename.substringAfterLast('/').substringAfterLast('\\').trim()
        return validFilename(filename, size)
            .mapError(ImageError::Invalid)
            .flatMap {
                val format = ImageFormat.detect(upload.bytes)?.takeIf { it in policy.allowedFormats }
                if (format == null) {
                    Outcome.Failure(ImageError.UnsupportedFormat(policy.allowedFormats))
                } else {
                    Outcome.Success(ImageMetadata(ImageId.random(), filename, format, size, clock.now()))
                }
            }
    }

    private fun validFilename(filename: String, size: Long): Outcome<Unit, ValidationError> = validated(
        rules = {
            check("file", size > 0) { "must not be empty" }
            check("filename", filename.isNotEmpty()) { "must not be blank" }
            check("filename", filename.length <= ImageUploadPolicy.MAX_FILENAME_LENGTH) {
                "must be at most ${ImageUploadPolicy.MAX_FILENAME_LENGTH} characters"
            }
        },
        create = {}
    )
}
