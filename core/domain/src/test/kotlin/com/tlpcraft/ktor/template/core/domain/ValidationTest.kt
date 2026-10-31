package com.tlpcraft.ktor.template.core.domain

import com.tlpcraft.ktor.template.core.model.FieldViolation
import com.tlpcraft.ktor.template.core.model.PageRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidationTest {
    @Test
    fun `validated creates the value when every rule holds`() {
        val outcome = validated(rules = { check("name", condition = true) { "unused" } }, create = { "created" })

        assertEquals(Outcome.Success("created"), outcome)
    }

    @Test
    fun `validated reports every violated rule`() {
        val outcome = validated(
            rules = {
                check("a", condition = false) { "is wrong" }
                check("b", condition = true) { "unused" }
                check("c", condition = false) { "is also wrong" }
            },
            create = { "never" }
        )

        assertEquals(
            Outcome.Failure(ValidationError(listOf(FieldViolation("a", "is wrong"), FieldViolation("c", "is also wrong")))),
            outcome
        )
    }

    @Test
    fun `pageRequestOf accepts values in range`() {
        assertEquals(Outcome.Success(PageRequest(page = 2, size = 50)), pageRequestOf(page = 2, size = 50))
    }

    @Test
    fun `pageRequestOf rejects out-of-range values without throwing`() {
        val outcome = pageRequestOf(page = 0, size = 0)

        assertEquals(listOf("page", "size"), (outcome as Outcome.Failure).error.violations.map { it.field })
    }
}
