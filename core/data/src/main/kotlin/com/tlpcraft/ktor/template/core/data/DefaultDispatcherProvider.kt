package com.tlpcraft.ktor.template.core.data

import com.tlpcraft.ktor.template.core.domain.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** [DispatcherProvider] backed by the standard [Dispatchers]. */
public object DefaultDispatcherProvider : DispatcherProvider {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
