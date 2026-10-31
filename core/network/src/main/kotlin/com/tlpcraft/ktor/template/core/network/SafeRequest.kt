package com.tlpcraft.ktor.template.core.network

import com.tlpcraft.ktor.template.core.domain.Outcome
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerializationException

/**
 * Executes [request] and decodes a successful response body as [T]. Transport failures, non-2xx
 * statuses and undecodable bodies become a [NetworkError] instead of an exception.
 *
 * ```
 * client.safeRequest<ForecastDto> { get("https://api.example.com/forecast") }
 * ```
 */
public suspend inline fun <reified T> safeRequest(request: () -> HttpResponse): Outcome<T, NetworkError> = try {
    val response = request()
    if (response.status.isSuccess()) {
        Outcome.Success(response.body<T>())
    } else {
        Outcome.Failure(NetworkError.HttpStatus(response.status.value, response.bodyAsText()))
    }
} catch (timeout: HttpRequestTimeoutException) {
    Outcome.Failure(NetworkError.Timeout(timeout))
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (io: IOException) {
    Outcome.Failure(NetworkError.Unreachable(io))
} catch (serialization: SerializationException) {
    Outcome.Failure(NetworkError.InvalidResponse(serialization))
} catch (conversion: ContentConvertException) {
    Outcome.Failure(NetworkError.InvalidResponse(conversion))
} catch (noTransformation: NoTransformationFoundException) {
    Outcome.Failure(NetworkError.InvalidResponse(noTransformation))
}
