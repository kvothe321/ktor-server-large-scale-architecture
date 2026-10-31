package com.tlpcraft.ktor.template.app

import com.tlpcraft.ktor.template.core.database.DatabaseConfig
import com.tlpcraft.ktor.template.core.network.HttpClientConfig
import io.ktor.server.config.ApplicationConfig
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.time.Duration

/**
 * Typed view of the application-wide sections of `application.conf`. Feature sections are read by
 * the features themselves (see `ServerFeature.koinModule`).
 */
internal data class AppConfig(
    val database: DatabaseConfig,
    val storageRoot: Path,
    val httpClient: HttpClientConfig
) {
    companion object {
        fun from(config: ApplicationConfig): AppConfig = AppConfig(
            database = DatabaseConfig(
                jdbcUrl = config.string("database.jdbcUrl"),
                username = config.string("database.username"),
                password = config.string("database.password"),
                maximumPoolSize = config.string("database.maximumPoolSize").toInt()
            ),
            storageRoot = Path(config.string("storage.root")),
            httpClient = HttpClientConfig(
                requestTimeout = Duration.parse(config.string("httpClient.requestTimeout")),
                connectTimeout = Duration.parse(config.string("httpClient.connectTimeout"))
            )
        )

        private fun ApplicationConfig.string(path: String): String = property(path).getString()
    }
}
