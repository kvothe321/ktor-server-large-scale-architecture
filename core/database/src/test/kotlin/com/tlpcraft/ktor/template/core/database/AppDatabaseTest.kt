package com.tlpcraft.ktor.template.core.database

import com.tlpcraft.ktor.template.core.testing.testDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class AppDatabaseTest {
    @Test
    fun `migrations run and the database reports healthy`() = runTest {
        testDatabase().use { database ->
            assertTrue(database.isHealthy())
            val tables = database.transaction {
                exec("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'") { rs ->
                    generateSequence { if (rs.next()) rs.getString(1) else null }.toSet()
                }
            }
            assertEquals(setOf("flyway_schema_history", "notes", "weather_cache", "images"), tables)
        }
    }

    @Test
    fun `a closed database reports unhealthy`() = runTest {
        val database = testDatabase()
        database.close()

        assertTrue(!database.isHealthy())
    }
}
