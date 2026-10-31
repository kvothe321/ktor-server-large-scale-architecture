package com.tlpcraft.ktor.template.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OutcomeTest {
    private data class TestError(override val message: String = "boom") : DomainError

    private val success: Outcome<Int, TestError> = 2.asSuccess()
    private val failure: Outcome<Int, TestError> = TestError().asFailure()

    @Test
    fun `map transforms only a success`() {
        assertEquals(Outcome.Success(4), success.map { it * 2 })
        assertEquals<Outcome<*, *>>(failure, failure.map { it * 2 })
    }

    @Test
    fun `flatMap short-circuits on failure`() {
        assertEquals(Outcome.Success("2"), success.flatMap { it.toString().asSuccess() })
        assertEquals<Outcome<*, *>>(failure, failure.flatMap { it.toString().asSuccess() })
    }

    @Test
    fun `mapError transforms only a failure`() {
        assertEquals(Outcome.Failure(TestError("BOOM")), failure.mapError { TestError(it.message.uppercase()) })
        assertEquals(success, success.mapError { TestError("unused") })
    }

    @Test
    fun `recover replaces a failure`() {
        assertEquals(Outcome.Success(0), failure.recover { 0.asSuccess() })
    }

    @Test
    fun `fold picks the matching branch`() {
        assertEquals("ok 2", success.fold(onSuccess = { "ok $it" }, onFailure = { it.message }))
        assertEquals("boom", failure.fold(onSuccess = { "ok $it" }, onFailure = { it.message }))
    }

    @Test
    fun `getOrNull returns null for a failure`() {
        assertEquals(2, success.getOrNull())
        assertNull(failure.getOrNull())
    }
}
