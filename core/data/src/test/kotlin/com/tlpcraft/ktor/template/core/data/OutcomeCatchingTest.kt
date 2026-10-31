package com.tlpcraft.ktor.template.core.data

import com.tlpcraft.ktor.template.core.domain.DomainError
import com.tlpcraft.ktor.template.core.domain.Outcome
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class OutcomeCatchingTest {
    private data class TestError(override val cause: Throwable) : DomainError {
        override val message: String get() = "failed"
    }

    @Test
    fun `a returned value is a success`() {
        assertEquals(Outcome.Success(42), outcomeCatching(::TestError) { 42 })
    }

    @Test
    fun `an exception is translated into a typed failure`() {
        val io = IOException("disk")

        val outcome = outcomeCatching(::TestError) { throw io }

        assertSame(io, (outcome as Outcome.Failure).error.cause)
    }

    @Test
    fun `cancellation is rethrown, never swallowed`() {
        assertFailsWith<CancellationException> { outcomeCatching(::TestError) { throw CancellationException("cancelled") } }
    }
}
