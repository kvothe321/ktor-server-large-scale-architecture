package com.tlpcraft.ktor.template.feature.images.presentation

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.ValidationError
import com.tlpcraft.ktor.template.feature.images.domain.ImageError
import com.tlpcraft.ktor.template.feature.images.domain.ImageUpload
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.utils.io.readBuffer
import kotlinx.io.readByteArray

/**
 * Reads the file part named [fieldName], buffering at most [maxSizeBytes] + 1 bytes. That bounds
 * memory use whatever the client sends, while the extra byte still lets the use case see that
 * the file is too large and reject it with its own typed error.
 */
internal suspend fun MultiPartData.readImageUpload(fieldName: String, maxSizeBytes: Long): Outcome<ImageUpload, ImageError> {
    while (true) {
        val part = readPart() ?: return Outcome.Failure(ImageError.Invalid(ValidationError(fieldName, "is required")))
        try {
            if (part is PartData.FileItem && part.name == fieldName) {
                val bytes = part.provider().readBuffer(maxSizeBytes + 1).readByteArray()
                return Outcome.Success(ImageUpload(filename = part.originalFileName.orEmpty(), bytes = bytes))
            }
        } finally {
            part.release()
        }
    }
}
