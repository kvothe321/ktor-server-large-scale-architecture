package com.tlpcraft.ktor.template.feature.images.domain

import com.tlpcraft.ktor.template.core.domain.DomainError
import com.tlpcraft.ktor.template.core.domain.ValidationError

sealed interface ImageError : DomainError {
    data class Invalid(val error: ValidationError) : ImageError {
        override val message: String get() = error.message
    }

    data class TooLarge(val maxSizeBytes: Long) : ImageError {
        override val message: String get() = "Image exceeds the $maxSizeBytes byte limit"
    }

    data class UnsupportedFormat(val allowed: Set<ImageFormat>) : ImageError {
        override val message: String get() = "Not a supported image; allowed: ${allowed.joinToString()}"
    }

    data class NotFound(val id: ImageId) : ImageError {
        override val message: String get() = "Image ${id.value} not found"
    }

    data class Unavailable(override val cause: Throwable?) : ImageError {
        override val message: String get() = "Image storage unavailable: ${cause?.message}"
    }
}
