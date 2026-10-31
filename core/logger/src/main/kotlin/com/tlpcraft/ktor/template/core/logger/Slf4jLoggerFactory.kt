package com.tlpcraft.ktor.template.core.logger

import org.slf4j.LoggerFactory as Slf4jFactory
import org.slf4j.event.Level

/**
 * [LoggerFactory] backed by SLF4J. Fields become SLF4J key-value pairs: logback renders them with
 * `%kvp` in plain text, and the logstash JSON encoder writes them as top-level properties.
 */
public object Slf4jLoggerFactory : LoggerFactory {
    override fun logger(name: String): Logger = Slf4jLogger(Slf4jFactory.getLogger(name))
}

private class Slf4jLogger(private val delegate: org.slf4j.Logger) : Logger {
    override fun debug(message: String, vararg fields: LogField) = log(Level.DEBUG, message, fields, throwable = null)

    override fun info(message: String, vararg fields: LogField) = log(Level.INFO, message, fields, throwable = null)

    override fun warn(message: String, vararg fields: LogField, throwable: Throwable?) = log(Level.WARN, message, fields, throwable)

    override fun error(message: String, vararg fields: LogField, throwable: Throwable?) = log(Level.ERROR, message, fields, throwable)

    private fun log(
        level: Level,
        message: String,
        fields: Array<out LogField>,
        throwable: Throwable?
    ) {
        if (!delegate.isEnabledForLevel(level)) return
        val event = delegate.atLevel(level).setMessage(message).setCause(throwable)
        fields.forEach { (key, value) -> event.addKeyValue(key, value) }
        event.log()
    }
}
