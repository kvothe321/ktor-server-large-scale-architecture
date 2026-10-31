package com.tlpcraft.ktor.template.core.model

/** One page of [items] taken from a collection of [totalItems] elements, as described by [request]. */
public data class Page<out T>(
    public val items: List<T>,
    public val request: PageRequest,
    public val totalItems: Long
) {
    public val totalPages: Long
        get() = (totalItems + request.size - 1) / request.size

    public inline fun <R> map(transform: (T) -> R): Page<R> = Page(items.map(transform), request, totalItems)
}
