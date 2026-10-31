package com.tlpcraft.ktor.template.feature.images.presentation

import com.tlpcraft.ktor.template.core.server.PageResponse
import com.tlpcraft.ktor.template.core.server.problem
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.JsonSchema
import io.ktor.openapi.JsonType
import io.ktor.openapi.Operation
import io.ktor.openapi.ReferenceOr
import io.ktor.openapi.jsonSchema
import io.ktor.server.routing.openapi.RouteOperationFunction

/** OpenAPI descriptions of the images endpoints, kept apart from the handlers in [ImageRoutes]. */
internal object ImageApiDocs {
    private const val TAG = "Images"

    val upload: RouteOperationFunction = {
        tag(TAG)
        summary = "Upload an image"
        description = "multipart/form-data with a `file` part. PNG, JPEG, GIF or WEBP, detected from the file content."
        requestBody {
            ContentType.MultiPart.FormData {
                schema = JsonSchema(type = JsonType.OBJECT, properties = mapOf("file" to ReferenceOr.value(binarySchema())), required = listOf("file"))
            }
        }
        responses {
            HttpStatusCode.Created {
                description = "Metadata of the stored image; Location points at it"
                schema = jsonSchema<ImageResponse>()
            }
            problem(HttpStatusCode.BadRequest, "Missing file part, empty file or invalid filename")
            problem(HttpStatusCode.PayloadTooLarge, "File exceeds the size limit")
            problem(HttpStatusCode.UnsupportedMediaType, "Content is not a supported image")
        }
    }

    val list: RouteOperationFunction = {
        tag(TAG)
        summary = "List image metadata, newest first"
        parameters {
            query("page") { description = "1-based page number (default 1)" }
            query("size") { description = "Page size, 1 to 100 (default 20)" }
        }
        responses {
            HttpStatusCode.OK {
                description = "One page of image metadata"
                schema = jsonSchema<PageResponse<ImageResponse>>()
            }
            problem(HttpStatusCode.BadRequest, "Invalid paging parameters")
        }
    }

    val get: RouteOperationFunction = {
        tag(TAG)
        summary = "Get image metadata"
        idParameter()
        responses {
            HttpStatusCode.OK {
                description = "Image metadata"
                schema = jsonSchema<ImageResponse>()
            }
            problem(HttpStatusCode.NotFound, "No image with this id")
        }
    }

    val download: RouteOperationFunction = {
        tag(TAG)
        summary = "Download the image file"
        idParameter()
        responses {
            HttpStatusCode.OK {
                description = "The image bytes, with the image's content type"
                ContentType.Application.OctetStream { schema = binarySchema() }
            }
            problem(HttpStatusCode.NotFound, "No image with this id")
        }
    }

    val delete: RouteOperationFunction = {
        tag(TAG)
        summary = "Delete an image and its file"
        idParameter()
        responses {
            HttpStatusCode.NoContent { description = "Deleted" }
            problem(HttpStatusCode.NotFound, "No image with this id")
        }
    }

    private fun binarySchema() = JsonSchema(type = JsonType.STRING, format = "binary")

    private fun Operation.Builder.idParameter() = parameters {
        path("id") { description = "Image id (UUID)" }
    }
}
