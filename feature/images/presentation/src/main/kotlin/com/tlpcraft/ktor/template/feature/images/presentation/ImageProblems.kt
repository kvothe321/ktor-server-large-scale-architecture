package com.tlpcraft.ktor.template.feature.images.presentation

import com.tlpcraft.ktor.template.core.server.ApiProblem
import com.tlpcraft.ktor.template.feature.images.domain.ImageError

internal fun ImageError.toProblem(): ApiProblem = when (this) {
    is ImageError.Invalid -> ApiProblem.validation(error)

    is ImageError.TooLarge -> ApiProblem.payloadTooLarge("Images may be at most $maxSizeBytes bytes.")

    is ImageError.UnsupportedFormat -> ApiProblem.unsupportedMediaType(
        "Content is not a supported image. Allowed: ${allowed.joinToString { it.mediaType }}."
    )

    is ImageError.NotFound -> ApiProblem.notFound("Image ${id.value} does not exist.")

    is ImageError.Unavailable -> ApiProblem.serviceUnavailable("Images are temporarily unavailable.")
}
