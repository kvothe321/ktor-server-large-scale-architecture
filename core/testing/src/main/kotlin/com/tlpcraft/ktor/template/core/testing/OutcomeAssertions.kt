package com.tlpcraft.ktor.template.core.testing

import com.tlpcraft.ktor.template.core.domain.DomainError
import com.tlpcraft.ktor.template.core.domain.Outcome
import kotlin.test.assertIs
import kotlin.test.fail

/** Asserts this is a success and returns its value. */
public fun <T> Outcome<T, DomainError>.assertSuccess(): T = when (this) {
    is Outcome.Success -> value
    is Outcome.Failure -> fail("Expected success but was failure: ${error.message}")
}

/** Asserts this is a failure of type [E] and returns the error. */
public inline fun <reified E : DomainError> Outcome<*, DomainError>.assertFailure(): E = when (this) {
    is Outcome.Success -> fail("Expected failure of type ${E::class.simpleName} but was success: $value")
    is Outcome.Failure -> assertIs<E>(error)
}
