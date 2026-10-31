package com.tlpcraft.ktor.template.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PageTest {
    @Test
    fun `offset skips the items of all previous pages`() {
        assertEquals(40L, PageRequest(page = 3, size = 20).offset)
    }

    @Test
    fun `total pages rounds up a partial last page`() {
        assertEquals(3L, Page(items = emptyList<Int>(), request = PageRequest(page = 1, size = 10), totalItems = 21).totalPages)
    }

    @Test
    fun `total pages is zero for an empty collection`() {
        assertEquals(0L, Page(items = emptyList<Int>(), request = PageRequest(), totalItems = 0).totalPages)
    }

    @Test
    fun `constructor rejects a size above the maximum`() {
        assertFailsWith<IllegalArgumentException> { PageRequest(page = 1, size = PageRequest.MAX_SIZE + 1) }
    }
}
