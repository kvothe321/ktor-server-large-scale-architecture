package com.tlpcraft.ktor.template.core.domain

import com.tlpcraft.ktor.template.core.model.PageRequest

/** Validating factory for [PageRequest], for use with untrusted input such as query parameters. */
public fun pageRequestOf(page: Int, size: Int): Outcome<PageRequest, ValidationError> = validated(
    rules = {
        check("page", page >= PageRequest.FIRST_PAGE) { "must be at least ${PageRequest.FIRST_PAGE}" }
        check("size", size in PageRequest.MIN_SIZE..PageRequest.MAX_SIZE) {
            "must be between ${PageRequest.MIN_SIZE} and ${PageRequest.MAX_SIZE}"
        }
    },
    create = { PageRequest(page, size) }
)
