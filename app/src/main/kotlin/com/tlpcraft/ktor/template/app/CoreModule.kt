package com.tlpcraft.ktor.template.app

import com.tlpcraft.ktor.template.core.data.DefaultDispatcherProvider
import com.tlpcraft.ktor.template.core.database.AppDatabase
import com.tlpcraft.ktor.template.core.domain.DispatcherProvider
import com.tlpcraft.ktor.template.core.logger.LoggerFactory
import com.tlpcraft.ktor.template.core.logger.Slf4jLoggerFactory
import com.tlpcraft.ktor.template.core.network.createHttpClient
import com.tlpcraft.ktor.template.core.storage.FileStorage
import com.tlpcraft.ktor.template.core.storage.LocalFileStorage
import io.ktor.client.HttpClient
import kotlin.time.Clock
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose

/** Infrastructure shared by all features. Resources are closed when the application stops. */
internal fun coreModule(config: AppConfig): Module = module {
    single<Clock> { Clock.System }
    single<DispatcherProvider> { DefaultDispatcherProvider }
    single<LoggerFactory> { Slf4jLoggerFactory }
    single(createdAtStart = true) { AppDatabase.connect(config.database, get()) } onClose { it?.close() }
    single<HttpClient> { createHttpClient(config.httpClient) } onClose { it?.close() }
    single<FileStorage> { LocalFileStorage(config.storageRoot, get()) }
}
