package com.tlpcraft.ktor.template.core.storage

import com.tlpcraft.ktor.template.core.testing.TestDispatcherProvider
import com.tlpcraft.ktor.template.core.testing.assertFailure
import com.tlpcraft.ktor.template.core.testing.assertSuccess
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir

class LocalFileStorageTest {
    @TempDir
    lateinit var root: Path

    private val key = StorageKey("file-1")

    @Test
    fun `written bytes can be read back`() = runTest {
        val storage = LocalFileStorage(root, TestDispatcherProvider(StandardTestDispatcher(testScheduler)))

        storage.write(key, byteArrayOf(1, 2, 3)).assertSuccess()

        assertContentEquals(byteArrayOf(1, 2, 3), storage.read(key).assertSuccess())
        assertEquals(listOf(root.resolve("file-1")), root.listDirectoryEntries())
    }

    @Test
    fun `reading a missing key is a typed NotFound`() = runTest {
        val storage = LocalFileStorage(root, TestDispatcherProvider(StandardTestDispatcher(testScheduler)))

        assertEquals(key, storage.read(key).assertFailure<StorageError.NotFound>().key)
    }

    @Test
    fun `delete is idempotent`() = runTest {
        val storage = LocalFileStorage(root, TestDispatcherProvider(StandardTestDispatcher(testScheduler)))
        storage.write(key, byteArrayOf(1)).assertSuccess()

        storage.delete(key).assertSuccess()
        storage.delete(key).assertSuccess()

        storage.read(key).assertFailure<StorageError.NotFound>()
    }

    @Test
    fun `keys that could escape the root are rejected`() {
        assertFailsWith<IllegalArgumentException> { StorageKey("../etc/passwd") }
        assertFailsWith<IllegalArgumentException> { StorageKey("a/b") }
    }
}
