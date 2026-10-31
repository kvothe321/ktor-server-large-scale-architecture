package com.tlpcraft.ktor.template.core.database

import kotlin.time.Instant

private const val NANOS_PER_MICRO = 1_000

/**
 * Truncates to microseconds, the finest timestamp precision of PostgreSQL and of the H2 columns
 * used here. Apply it before persisting so the value returned from a write equals the value read
 * back later; otherwise the database silently rounds and responses disagree.
 */
public fun Instant.toDatabasePrecision(): Instant = Instant.fromEpochSeconds(epochSeconds, nanosecondsOfSecond / NANOS_PER_MICRO * NANOS_PER_MICRO)
