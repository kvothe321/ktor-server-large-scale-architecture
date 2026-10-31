package com.tlpcraft.ktor.template.feature.images.data

import com.tlpcraft.ktor.template.core.database.AppDatabase
import com.tlpcraft.ktor.template.core.logger.LoggerFactory
import com.tlpcraft.ktor.template.core.storage.FileStorage
import com.tlpcraft.ktor.template.feature.images.domain.ImageContentStore
import com.tlpcraft.ktor.template.feature.images.domain.ImageRepository

/** Creates the production [ImageRepository], backed by [database]. */
fun imageRepository(database: AppDatabase, loggerFactory: LoggerFactory): ImageRepository =
    ExposedImageRepository(database, loggerFactory.logger(ExposedImageRepository::class.java.name))

/** Creates the production [ImageContentStore], backed by [storage]. */
fun imageContentStore(storage: FileStorage, loggerFactory: LoggerFactory): ImageContentStore =
    FileStorageImageContentStore(storage, loggerFactory.logger(FileStorageImageContentStore::class.java.name))
