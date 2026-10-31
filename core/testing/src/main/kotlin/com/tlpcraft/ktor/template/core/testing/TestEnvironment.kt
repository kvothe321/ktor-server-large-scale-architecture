package com.tlpcraft.ktor.template.core.testing

import com.tlpcraft.ktor.template.core.database.AppDatabase
import com.tlpcraft.ktor.template.core.domain.DispatcherProvider
import com.tlpcraft.ktor.template.core.logger.LoggerFactory
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose

/** Collaborators shared by the test server's features. Override pieces per test as needed. */
public class TestEnvironment(
    public val clock: MutableClock = MutableClock(),
    public val loggerFactory: RecordingLoggerFactory = RecordingLoggerFactory(),
    public val dispatchers: DispatcherProvider = TestDispatcherProvider(Dispatchers.IO)
) {
    internal fun coreModule(): Module = module {
        single<Clock> { clock }
        single<LoggerFactory> { loggerFactory }
        single<DispatcherProvider> { dispatchers }
        single<AppDatabase> { testDatabase(dispatchers) } onClose { it?.close() }
    }
}
