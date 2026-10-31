package com.tlpcraft.ktor.template.core.domain

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Coroutine dispatchers behind an interface, so production code uses the real pools while tests
 * inject a test dispatcher for deterministic execution.
 */
public interface DispatcherProvider {
    /** For blocking I/O: JDBC calls, file system access. */
    public val io: CoroutineDispatcher

    /** For CPU-bound work. */
    public val default: CoroutineDispatcher
}
