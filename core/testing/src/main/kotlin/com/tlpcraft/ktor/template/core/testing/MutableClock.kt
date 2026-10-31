package com.tlpcraft.ktor.template.core.testing

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** A [Clock] that only moves when told to, for deterministic time-dependent tests. */
public class MutableClock(start: Instant = Instant.parse("2026-01-01T00:00:00Z")) : Clock {
    private var current: Instant = start

    override fun now(): Instant = current

    public fun advanceBy(duration: Duration) {
        current += duration
    }
}
