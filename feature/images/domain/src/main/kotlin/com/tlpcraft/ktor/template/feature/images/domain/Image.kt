package com.tlpcraft.ktor.template.feature.images.domain

import kotlin.time.Instant
import kotlin.uuid.Uuid

@JvmInline
value class ImageId(val value: Uuid) {
    companion object {
        fun random(): ImageId = ImageId(Uuid.random())
    }
}

data class ImageMetadata(
    val id: ImageId,
    val originalFilename: String,
    val format: ImageFormat,
    val sizeBytes: Long,
    val uploadedAt: Instant
)

/** An image's metadata together with its bytes. */
class ImageContent(
    val metadata: ImageMetadata,
    val bytes: ByteArray
)

/** A file as received from a client: name and content are both untrusted. */
class ImageUpload(
    val filename: String,
    val bytes: ByteArray
)
