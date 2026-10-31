package com.tlpcraft.ktor.template.feature.images

import com.tlpcraft.ktor.template.core.server.ServerFeature
import com.tlpcraft.ktor.template.feature.images.data.imageContentStore
import com.tlpcraft.ktor.template.feature.images.data.imageRepository
import com.tlpcraft.ktor.template.feature.images.domain.ImageUploadPolicy
import com.tlpcraft.ktor.template.feature.images.domain.usecase.DeleteImageUseCase
import com.tlpcraft.ktor.template.feature.images.domain.usecase.DownloadImageUseCase
import com.tlpcraft.ktor.template.feature.images.domain.usecase.GetImageUseCase
import com.tlpcraft.ktor.template.feature.images.domain.usecase.ListImagesUseCase
import com.tlpcraft.ktor.template.feature.images.domain.usecase.UploadImageUseCase
import com.tlpcraft.ktor.template.feature.images.presentation.ImageRoutes
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.routing.Route
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.ext.get

/**
 * Image upload, download and deletion. Bytes go to the `FileStorage` binding, metadata to the database.
 *
 * Configuration (`images` section): `maxSizeBytes` (default 5 MiB).
 */
object ImagesFeature : ServerFeature {
    override fun koinModule(config: ApplicationConfig): Module {
        val policy = ImageUploadPolicy(
            maxSizeBytes = config.propertyOrNull("images.maxSizeBytes")?.getString()?.toLong() ?: ImageUploadPolicy.DEFAULT_MAX_SIZE_BYTES
        )
        return module {
            single { policy }
            single { imageRepository(database = get(), loggerFactory = get()) }
            single { imageContentStore(storage = get(), loggerFactory = get()) }
            singleOf(::UploadImageUseCase)
            singleOf(::GetImageUseCase)
            singleOf(::ListImagesUseCase)
            singleOf(::DownloadImageUseCase)
            singleOf(::DeleteImageUseCase)
            singleOf(::ImageRoutes)
        }
    }

    override fun routes(route: Route) {
        route.get<ImageRoutes>().register(route)
    }
}
