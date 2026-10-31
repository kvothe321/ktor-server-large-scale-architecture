package com.tlpcraft.ktor.template.feature.images.domain

/**
 * Supported image formats, recognised by their file signature ("magic bytes") rather than by the
 * client-declared content type or file extension, which are trivial to fake.
 */
enum class ImageFormat(val mediaType: String) {
    PNG("image/png"),
    JPEG("image/jpeg"),
    GIF("image/gif"),
    WEBP("image/webp");

    companion object {
        private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        private val JPEG_SIGNATURE = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        private val GIF87_SIGNATURE = "GIF87a".encodeToByteArray()
        private val GIF89_SIGNATURE = "GIF89a".encodeToByteArray()
        private val RIFF_SIGNATURE = "RIFF".encodeToByteArray()
        private val WEBP_SIGNATURE = "WEBP".encodeToByteArray()
        private const val WEBP_SIGNATURE_OFFSET = 8

        /** The format whose signature [bytes] start with, or null if none matches. */
        fun detect(bytes: ByteArray): ImageFormat? = when {
            bytes.startsWith(PNG_SIGNATURE) -> PNG
            bytes.startsWith(JPEG_SIGNATURE) -> JPEG
            bytes.startsWith(GIF87_SIGNATURE) || bytes.startsWith(GIF89_SIGNATURE) -> GIF
            bytes.startsWith(RIFF_SIGNATURE) && bytes.startsWith(WEBP_SIGNATURE, offset = WEBP_SIGNATURE_OFFSET) -> WEBP
            else -> null
        }

        private fun ByteArray.startsWith(prefix: ByteArray, offset: Int = 0): Boolean =
            size >= offset + prefix.size && prefix.indices.all { this[offset + it] == prefix[it] }
    }
}
