package com.tlpcraft.ktor.template.core.testing

import com.tlpcraft.ktor.template.core.data.DefaultDispatcherProvider
import com.tlpcraft.ktor.template.core.database.AppDatabase
import com.tlpcraft.ktor.template.core.database.DatabaseConfig
import com.tlpcraft.ktor.template.core.domain.DispatcherProvider
import kotlin.uuid.Uuid

/**
 * A fresh, fully migrated in-memory H2 database (PostgreSQL mode). Each call gets its own
 * database, so tests never share state. Close it when done.
 */
public fun testDatabase(dispatchers: DispatcherProvider = DefaultDispatcherProvider): AppDatabase = AppDatabase.connect(
    config = DatabaseConfig(
        jdbcUrl = "jdbc:h2:mem:test-${Uuid.random()};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
        username = "sa",
        password = "",
        maximumPoolSize = 2
    ),
    dispatchers = dispatchers
)
