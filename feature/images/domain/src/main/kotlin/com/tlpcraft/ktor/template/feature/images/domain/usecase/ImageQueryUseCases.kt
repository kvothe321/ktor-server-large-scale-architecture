package com.tlpcraft.ktor.template.feature.images.domain.usecase

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.flatMap
import com.tlpcraft.ktor.template.core.domain.map
import com.tlpcraft.ktor.template.core.domain.onSuccess
import com.tlpcraft.ktor.template.core.model.Page
import com.tlpcraft.ktor.template.core.model.PageRequest
import com.tlpcraft.ktor.template.feature.images.domain.ImageContent
import com.tlpcraft.ktor.template.feature.images.domain.ImageContentStore
import com.tlpcraft.ktor.template.feature.images.domain.ImageError
import com.tlpcraft.ktor.template.feature.images.domain.ImageId
import com.tlpcraft.ktor.template.feature.images.domain.ImageMetadata
import com.tlpcraft.ktor.template.feature.images.domain.ImageRepository

class GetImageUseCase(private val repository: ImageRepository) {
    suspend operator fun invoke(id: ImageId): Outcome<ImageMetadata, ImageError> = repository.find(id)
}

class ListImagesUseCase(private val repository: ImageRepository) {
    suspend operator fun invoke(request: PageRequest): Outcome<Page<ImageMetadata>, ImageError> = repository.list(request)
}

class DownloadImageUseCase(
    private val repository: ImageRepository,
    private val contentStore: ImageContentStore
) {
    suspend operator fun invoke(id: ImageId): Outcome<ImageContent, ImageError> = repository.find(id).flatMap { metadata ->
        contentStore.get(id).map { bytes -> ImageContent(metadata, bytes) }
    }
}

/**
 * Deletes the metadata first, which is what makes the image disappear for clients. Removing the
 * bytes afterwards is best effort: a failure there leaves only an unreachable file, never a
 * dangling metadata row, so it does not fail the request (the store logs it).
 */
class DeleteImageUseCase(
    private val repository: ImageRepository,
    private val contentStore: ImageContentStore
) {
    suspend operator fun invoke(id: ImageId): Outcome<Unit, ImageError> = repository.delete(id).onSuccess {
        contentStore.delete(id)
    }
}
