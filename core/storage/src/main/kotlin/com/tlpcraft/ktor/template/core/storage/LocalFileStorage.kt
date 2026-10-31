package com.tlpcraft.ktor.template.core.storage

import com.tlpcraft.ktor.template.core.domain.DispatcherProvider
import com.tlpcraft.ktor.template.core.domain.Outcome
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import kotlinx.coroutines.withContext

/**
 * [FileStorage] on the local file system, one file per key under [root]. Writes go to a temporary
 * file first and are moved into place atomically, so readers never see a partial file.
 */
public class LocalFileStorage(
    root: Path,
    private val dispatchers: DispatcherProvider
) : FileStorage {
    private val root: Path = root.toAbsolutePath().normalize()

    override suspend fun write(key: StorageKey, bytes: ByteArray): Outcome<Unit, StorageError> = io {
        root.createDirectories()
        val temporary = Files.createTempFile(root, ".upload-", ".tmp")
        try {
            temporary.writeBytes(bytes)
            Files.move(temporary, resolve(key), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally {
            temporary.deleteIfExists()
        }
    }

    override suspend fun read(key: StorageKey): Outcome<ByteArray, StorageError> = withContext(dispatchers.io) {
        try {
            Outcome.Success(resolve(key).readBytes())
        } catch (_: NoSuchFileException) {
            Outcome.Failure(StorageError.NotFound(key))
        } catch (exception: IOException) {
            Outcome.Failure(StorageError.Io(exception))
        }
    }

    override suspend fun delete(key: StorageKey): Outcome<Unit, StorageError> = io {
        resolve(key).deleteIfExists()
    }

    private fun resolve(key: StorageKey): Path = root.resolve(key.value)

    private suspend fun io(block: () -> Unit): Outcome<Unit, StorageError> = withContext(dispatchers.io) {
        try {
            Outcome.Success(block())
        } catch (exception: IOException) {
            Outcome.Failure(StorageError.Io(exception))
        }
    }
}
