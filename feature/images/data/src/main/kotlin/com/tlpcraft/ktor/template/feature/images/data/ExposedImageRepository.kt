package com.tlpcraft.ktor.template.feature.images.data

import com.tlpcraft.ktor.template.core.data.outcomeCatching
import com.tlpcraft.ktor.template.core.database.AppDatabase
import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.asFailure
import com.tlpcraft.ktor.template.core.domain.asSuccess
import com.tlpcraft.ktor.template.core.domain.flatMap
import com.tlpcraft.ktor.template.core.logger.Logger
import com.tlpcraft.ktor.template.core.model.Page
import com.tlpcraft.ktor.template.core.model.PageRequest
import com.tlpcraft.ktor.template.feature.images.domain.ImageError
import com.tlpcraft.ktor.template.feature.images.domain.ImageFormat
import com.tlpcraft.ktor.template.feature.images.domain.ImageId
import com.tlpcraft.ktor.template.feature.images.domain.ImageMetadata
import com.tlpcraft.ktor.template.feature.images.domain.ImageRepository
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll

internal class ExposedImageRepository(
    private val database: AppDatabase,
    private val logger: Logger
) : ImageRepository {
    override suspend fun insert(metadata: ImageMetadata): Outcome<ImageMetadata, ImageError> = query {
        ImagesTable.insert {
            it[id] = metadata.id.value
            it[originalFilename] = metadata.originalFilename
            it[format] = metadata.format.name
            it[sizeBytes] = metadata.sizeBytes
            it[uploadedAt] = metadata.uploadedAt
        }
        metadata
    }

    override suspend fun find(id: ImageId): Outcome<ImageMetadata, ImageError> = query {
        ImagesTable.selectAll().where { ImagesTable.id eq id.value }.singleOrNull()?.toMetadata()
    }.flatMap { it?.asSuccess() ?: ImageError.NotFound(id).asFailure() }

    override suspend fun list(request: PageRequest): Outcome<Page<ImageMetadata>, ImageError> = query {
        val total = ImagesTable.selectAll().count()
        val items = ImagesTable.selectAll()
            .orderBy(ImagesTable.uploadedAt to SortOrder.DESC, ImagesTable.id to SortOrder.ASC)
            .limit(request.size)
            .offset(request.offset)
            .map { it.toMetadata() }
        Page(items, request, total)
    }

    override suspend fun delete(id: ImageId): Outcome<Unit, ImageError> = query {
        ImagesTable.deleteWhere { ImagesTable.id eq id.value }
    }.flatMap { deleted -> if (deleted > 0) Unit.asSuccess() else ImageError.NotFound(id).asFailure() }

    private suspend fun <T> query(block: JdbcTransaction.() -> T): Outcome<T, ImageError> = outcomeCatching(
        onError = { exception ->
            logger.error("Images query failed", throwable = exception)
            ImageError.Unavailable(exception)
        },
        block = { database.transaction(block) }
    )
}

private fun ResultRow.toMetadata() = ImageMetadata(
    id = ImageId(this[ImagesTable.id]),
    originalFilename = this[ImagesTable.originalFilename],
    format = ImageFormat.valueOf(this[ImagesTable.format]),
    sizeBytes = this[ImagesTable.sizeBytes],
    uploadedAt = this[ImagesTable.uploadedAt]
)
