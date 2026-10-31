package com.tlpcraft.ktor.template.core.model

/**
 * A request for one page of a collection. Pages are 1-based.
 *
 * The constructor guards its invariants; build instances from untrusted input through the
 * validating factory in `core:domain` (`pageRequestOf`) so violations surface as typed errors.
 */
public data class PageRequest(
    public val page: Int = FIRST_PAGE,
    public val size: Int = DEFAULT_SIZE
) {
    init {
        require(page >= FIRST_PAGE) { "page must be >= $FIRST_PAGE, was $page" }
        require(size in MIN_SIZE..MAX_SIZE) { "size must be in $MIN_SIZE..$MAX_SIZE, was $size" }
    }

    /** Number of items to skip before this page starts. */
    public val offset: Long get() = (page - FIRST_PAGE).toLong() * size

    public companion object {
        public const val FIRST_PAGE: Int = 1
        public const val MIN_SIZE: Int = 1
        public const val DEFAULT_SIZE: Int = 20
        public const val MAX_SIZE: Int = 100
    }
}
