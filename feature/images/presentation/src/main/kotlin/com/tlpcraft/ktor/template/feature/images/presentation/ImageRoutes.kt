package com.tlpcraft.ktor.template.feature.images.presentation

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.flatMap
import com.tlpcraft.ktor.template.core.domain.map
import com.tlpcraft.ktor.template.core.domain.mapError
import com.tlpcraft.ktor.template.core.domain.onSuccess
import com.tlpcraft.ktor.template.core.server.pageRequest
import com.tlpcraft.ktor.template.core.server.respondNoContent
import com.tlpcraft.ktor.template.core.server.respondOutcome
import com.tlpcraft.ktor.template.core.server.respondProblem
import com.tlpcraft.ktor.template.core.server.toResponse
import com.tlpcraft.ktor.template.core.server.uuidParameter
import com.tlpcraft.ktor.template.feature.images.domain.ImageError
import com.tlpcraft.ktor.template.feature.images.domain.ImageId
import com.tlpcraft.ktor.template.feature.images.domain.ImageUploadPolicy
import com.tlpcraft.ktor.template.feature.images.domain.usecase.DeleteImageUseCase
import com.tlpcraft.ktor.template.feature.images.domain.usecase.DownloadImageUseCase
import com.tlpcraft.ktor.template.feature.images.domain.usecase.GetImageUseCase
import com.tlpcraft.ktor.template.feature.images.domain.usecase.ListImagesUseCase
import com.tlpcraft.ktor.template.feature.images.domain.usecase.UploadImageUseCase
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.path
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.header
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/** HTTP endpoints of the images feature, mounted at `/images`. */
class ImageRoutes(
    private val policy: ImageUploadPolicy,
    private val uploadImage: UploadImageUseCase,
    private val getImage: GetImageUseCase,
    private val listImages: ListImagesUseCase,
    private val downloadImage: DownloadImageUseCase,
    private val deleteImage: DeleteImageUseCase
) {
    fun register(route: Route) {
        route.route("/images") {
            post {
                val uploaded = call.receiveMultipart()
                    .readImageUpload(fieldName = "file", maxSizeBytes = policy.maxSizeBytes)
                    .flatMap { uploadImage(it) }
                    .onSuccess { call.response.header(HttpHeaders.Location, "${call.request.path().trimEnd('/')}/${it.id.value}") }
                call.respondOutcome(uploaded, HttpStatusCode.Created, ImageError::toProblem) { it.toResponse() }
            }.describe(ImageApiDocs.upload)

            get {
                val page = call.pageRequest().mapError(ImageError::Invalid).flatMap { listImages(it) }
                call.respondOutcome(page, toProblem = ImageError::toProblem) { images -> images.toResponse { it.toResponse() } }
            }.describe(ImageApiDocs.list)

            route("/{id}") {
                get {
                    call.respondOutcome(call.imageId().flatMap { getImage(it) }, toProblem = ImageError::toProblem) { it.toResponse() }
                }.describe(ImageApiDocs.get)

                get("/content") {
                    when (val content = call.imageId().flatMap { downloadImage(it) }) {
                        is Outcome.Success -> {
                            val metadata = content.value.metadata
                            call.response.header(
                                HttpHeaders.ContentDisposition,
                                ContentDisposition.Inline.withParameter(ContentDisposition.Parameters.FileName, metadata.originalFilename).toString()
                            )
                            call.respondBytes(content.value.bytes, ContentType.parse(metadata.format.mediaType))
                        }

                        is Outcome.Failure -> call.respondProblem(content.error.toProblem())
                    }
                }.describe(ImageApiDocs.download)

                delete {
                    call.respondNoContent(call.imageId().flatMap { deleteImage(it) }, ImageError::toProblem)
                }.describe(ImageApiDocs.delete)
            }
        }
    }

    private fun ApplicationCall.imageId(): Outcome<ImageId, ImageError> = uuidParameter("id").map(::ImageId).mapError(ImageError::Invalid)
}
