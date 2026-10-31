package com.tlpcraft.ktor.template.core.data

import com.tlpcraft.ktor.template.core.domain.DomainError
import com.tlpcraft.ktor.template.core.domain.Outcome
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs [block] and wraps its result in [Outcome.Success]. Any exception is translated into a typed
 * error by [onError], which is the one place infrastructure exceptions become [DomainError]s.
 *
 * [CancellationException] is always rethrown so structured concurrency keeps working.
 */
public inline fun <T, E : DomainError> outcomeCatching(onError: (Exception) -> E, block: () -> T): Outcome<T, E> = try {
    Outcome.Success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (
    // The data layer's boundary: whatever the driver throws becomes a typed error here.
    @Suppress("TooGenericExceptionCaught") exception: Exception
) {
    Outcome.Failure(onError(exception))
}
