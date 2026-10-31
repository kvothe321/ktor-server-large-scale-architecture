package com.tlpcraft.ktor.template.feature.images.domain

/** What an upload may be. */
data class ImageUploadPolicy(
    val maxSizeBytes: Long = DEFAULT_MAX_SIZE_BYTES,
    val allowedFormats: Set<ImageFormat> = ImageFormat.entries.toSet()
) {
    init {
        require(maxSizeBytes > 0) { "maxSizeBytes must be positive" }
        require(allowedFormats.isNotEmpty()) { "at least one format must be allowed" }
    }

    companion object {
        const val DEFAULT_MAX_SIZE_BYTES: Long = 5L * 1024 * 1024
        const val MAX_FILENAME_LENGTH: Int = 255
    }
}
