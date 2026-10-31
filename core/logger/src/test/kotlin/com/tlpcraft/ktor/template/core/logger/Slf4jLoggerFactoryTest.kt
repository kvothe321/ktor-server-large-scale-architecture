package com.tlpcraft.ktor.template.core.logger

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.slf4j.LoggerFactory as Slf4jFactory

class Slf4jLoggerFactoryTest {
    private val name = "test.structured"
    private val appender = ListAppender<ILoggingEvent>().apply { start() }
    private val backend = (Slf4jFactory.getLogger(name) as ch.qos.logback.classic.Logger).apply {
        addAppender(appender)
        level = Level.INFO
    }

    @AfterTest
    fun detach() {
        backend.detachAppender(appender)
    }

    @Test
    fun `fields become key-value pairs and the message stays constant`() {
        Slf4jLoggerFactory.logger(name).info("Image uploaded", "imageId" to "abc", "sizeBytes" to 42)

        val event = appender.list.single()
        assertEquals("Image uploaded", event.formattedMessage)
        assertEquals(mapOf("imageId" to "abc", "sizeBytes" to 42), event.keyValuePairs.associate { it.key to it.value })
    }

    @Test
    fun `throwable is attached to the event`() {
        Slf4jLoggerFactory.logger(name).error("Query failed", "table" to "notes", throwable = IllegalStateException("boom"))

        assertEquals("boom", appender.list.single().throwableProxy.message)
    }

    @Test
    fun `disabled levels are skipped`() {
        Slf4jLoggerFactory.logger(name).debug("noise")

        assertTrue(appender.list.isEmpty())
    }
}
