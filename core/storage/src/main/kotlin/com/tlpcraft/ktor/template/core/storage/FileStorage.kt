package com.tlpcraft.ktor.template.core.storage

import com.tlpcraft.ktor.template.core.domain.DomainError
import com.tlpcraft.ktor.template.core.domain.Outcome

/**
 * Identifies a stored file. Restricted to a safe character set so a key can never escape the
 * storage root (no separators, no `..`).
 */
@JvmInline
public value class StorageKey(public val value: String) {
    init {
        require(value.matches(PATTERN)) { "Invalid storage key: '$value'" }
    }

    private companion object {
        val PATTERN = Regex("[A-Za-z0-9][A-Za-z0-9._-]{0,127}")
    }
}

/** Blob storage, independent of where the bytes live (local disk, S3, ...). */
public interface FileStorage {
    /** Stores [bytes] under [key], replacing any previous content. */
    public suspend fun write(key: StorageKey, bytes: ByteArray): Outcome<Unit, StorageError>

    public suspend fun read(key: StorageKey): Outcome<ByteArray, StorageError>

    /** Deletes [key]. Deleting a missing key succeeds, so retries are safe. */
    public suspend fun delete(key: StorageKey): Outcome<Unit, StorageError>
}

public sealed interface StorageError : DomainError {
    public data class NotFound(public val key: StorageKey) : StorageError {
        override val message: String get() = "No stored file for key '${key.value}'"
    }

    public data class Io(override val cause: Throwable) : StorageError {
        override val message: String get() = "Storage I/O failure: ${cause.message}"
    }
}
