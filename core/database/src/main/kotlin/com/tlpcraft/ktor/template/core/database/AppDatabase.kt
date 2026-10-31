package com.tlpcraft.ktor.template.core.database

import com.tlpcraft.ktor.template.core.domain.DispatcherProvider
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

/**
 * The application's database: a pooled [HikariDataSource], migrated by Flyway on startup and
 * accessed through Exposed. Close it on shutdown to release the pool.
 */
public class AppDatabase private constructor(
    private val dataSource: HikariDataSource,
    private val dispatchers: DispatcherProvider
) : AutoCloseable {
    private val database: Database = Database.connect(dataSource)

    /** Runs [block] in a transaction on the I/O dispatcher, since JDBC calls block their thread. */
    public suspend fun <T> transaction(block: JdbcTransaction.() -> T): T = withContext(dispatchers.io) {
        suspendTransaction(db = database) { block() }
    }

    /** Whether a pooled connection can currently be obtained and validated. */
    public suspend fun isHealthy(): Boolean = withContext(dispatchers.io) {
        runCatching { dataSource.connection.use { it.isValid(HEALTH_CHECK_TIMEOUT_SECONDS) } }.getOrDefault(false)
    }

    override fun close() {
        dataSource.close()
    }

    public companion object {
        private const val HEALTH_CHECK_TIMEOUT_SECONDS = 2

        /** Opens the connection pool and applies pending migrations from `classpath:db/migration`. */
        public fun connect(config: DatabaseConfig, dispatchers: DispatcherProvider): AppDatabase {
            val dataSource = HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = config.jdbcUrl
                    username = config.username
                    password = config.password
                    maximumPoolSize = config.maximumPoolSize
                    isAutoCommit = false
                    transactionIsolation = "TRANSACTION_READ_COMMITTED"
                }
            )
            Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate()
            return AppDatabase(dataSource, dispatchers)
        }
    }
}
