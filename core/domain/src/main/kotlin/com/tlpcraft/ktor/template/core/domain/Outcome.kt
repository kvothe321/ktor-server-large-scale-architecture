package com.tlpcraft.ktor.template.core.domain

/**
 * The result of an operation that either succeeds with a [T] or fails with a typed [E].
 *
 * ```
 * suspend fun findNote(id: NoteId): Outcome<Note, NoteError>
 *
 * findNote(id)
 *     .map { it.title }
 *     .fold(onSuccess = { title -> ... }, onFailure = { error -> ... })
 * ```
 */
public sealed interface Outcome<out T, out E : DomainError> {
    public data class Success<out T>(public val value: T) : Outcome<T, Nothing>

    public data class Failure<out E : DomainError>(public val error: E) : Outcome<Nothing, E>
}

public fun <T> T.asSuccess(): Outcome<T, Nothing> = Outcome.Success(this)

public fun <E : DomainError> E.asFailure(): Outcome<Nothing, E> = Outcome.Failure(this)

public inline fun <T, E : DomainError, R> Outcome<T, E>.map(transform: (T) -> R): Outcome<R, E> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}

public inline fun <T, E : DomainError, F : DomainError> Outcome<T, E>.mapError(transform: (E) -> F): Outcome<T, F> = when (this) {
    is Outcome.Success -> this
    is Outcome.Failure -> Outcome.Failure(transform(error))
}

public inline fun <T, E : DomainError, R> Outcome<T, E>.flatMap(transform: (T) -> Outcome<R, E>): Outcome<R, E> = when (this) {
    is Outcome.Success -> transform(value)
    is Outcome.Failure -> this
}

public inline fun <T, E : DomainError, R> Outcome<T, E>.fold(onSuccess: (T) -> R, onFailure: (E) -> R): R = when (this) {
    is Outcome.Success -> onSuccess(value)
    is Outcome.Failure -> onFailure(error)
}

public inline fun <T, E : DomainError> Outcome<T, E>.recover(transform: (E) -> Outcome<T, E>): Outcome<T, E> = when (this) {
    is Outcome.Success -> this
    is Outcome.Failure -> transform(error)
}

public inline fun <T, E : DomainError> Outcome<T, E>.onSuccess(action: (T) -> Unit): Outcome<T, E> = also {
    if (it is Outcome.Success) action(it.value)
}

public inline fun <T, E : DomainError> Outcome<T, E>.onFailure(action: (E) -> Unit): Outcome<T, E> = also {
    if (it is Outcome.Failure) action(it.error)
}

public fun <T> Outcome<T, *>.getOrNull(): T? = (this as? Outcome.Success)?.value
