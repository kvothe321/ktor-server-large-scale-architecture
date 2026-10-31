package com.tlpcraft.ktor.template.core.server

import com.tlpcraft.ktor.template.core.model.Page
import kotlinx.serialization.Serializable

/** Wire format of a paginated collection. */
@Serializable
public data class PageResponse<T>(
    public val items: List<T>,
    public val page: Int,
    public val size: Int,
    public val totalItems: Long,
    public val totalPages: Long
)

public inline fun <T, R> Page<T>.toResponse(transform: (T) -> R): PageResponse<R> = PageResponse(
    items = items.map(transform),
    page = request.page,
    size = request.size,
    totalItems = totalItems,
    totalPages = totalPages
)
