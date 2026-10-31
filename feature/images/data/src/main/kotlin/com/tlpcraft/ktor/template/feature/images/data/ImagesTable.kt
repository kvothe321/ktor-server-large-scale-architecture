package com.tlpcraft.ktor.template.feature.images.data

import com.tlpcraft.ktor.template.feature.images.domain.ImageUploadPolicy
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

/** Mirrors `V3__create_images.sql`. */
internal object ImagesTable : Table("images") {
    private const val FORMAT_LENGTH = 16

    val id = uuid("id")
    val originalFilename = varchar("original_filename", ImageUploadPolicy.MAX_FILENAME_LENGTH)
    val format = varchar("format", FORMAT_LENGTH)
    val sizeBytes = long("size_bytes")
    val uploadedAt = timestamp("uploaded_at")

    override val primaryKey = PrimaryKey(id)
}
