package com.tlpcraft.ktor.template.feature.images.presentation

import com.tlpcraft.ktor.template.feature.images.domain.ImageMetadata
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
internal data class ImageResponse(
    val id: Uuid,
    val filename: String,
    val contentType: String,
    val sizeBytes: Long,
    val uploadedAt: Instant
)

internal fun ImageMetadata.toResponse(): ImageResponse = ImageResponse(
    id = id.value,
    filename = originalFilename,
    contentType = format.mediaType,
    sizeBytes = sizeBytes,
    uploadedAt = uploadedAt
)
