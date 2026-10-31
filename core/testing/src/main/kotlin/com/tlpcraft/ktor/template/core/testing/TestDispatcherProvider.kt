package com.tlpcraft.ktor.template.core.testing

import com.tlpcraft.ktor.template.core.domain.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher

/** Routes every dispatcher to [dispatcher], typically the test scheduler's. */
public class TestDispatcherProvider(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
}
