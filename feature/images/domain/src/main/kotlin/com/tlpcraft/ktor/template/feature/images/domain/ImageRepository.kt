package com.tlpcraft.ktor.template.feature.images.domain

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.model.Page
import com.tlpcraft.ktor.template.core.model.PageRequest

/** Image metadata. */
interface ImageRepository {
    suspend fun insert(metadata: ImageMetadata): Outcome<ImageMetadata, ImageError>

    /** Fails with [ImageError.NotFound] when no image has [id]. */
    suspend fun find(id: ImageId): Outcome<ImageMetadata, ImageError>

    /** Most recently uploaded first. */
    suspend fun list(request: PageRequest): Outcome<Page<ImageMetadata>, ImageError>

    /** Fails with [ImageError.NotFound] when no image has [id]. */
    suspend fun delete(id: ImageId): Outcome<Unit, ImageError>
}

/** Image bytes, kept apart from metadata so they can live in blob storage. */
interface ImageContentStore {
    suspend fun put(id: ImageId, bytes: ByteArray): Outcome<Unit, ImageError>

    /** Fails with [ImageError.NotFound] when nothing is stored for [id]. */
    suspend fun get(id: ImageId): Outcome<ByteArray, ImageError>

    /** Succeeds when nothing is stored for [id], so it is safe to retry. */
    suspend fun delete(id: ImageId): Outcome<Unit, ImageError>
}
