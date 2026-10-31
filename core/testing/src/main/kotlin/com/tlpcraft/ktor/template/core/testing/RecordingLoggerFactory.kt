package com.tlpcraft.ktor.template.core.testing

import com.tlpcraft.ktor.template.core.logger.LogField
import com.tlpcraft.ktor.template.core.logger.Logger
import com.tlpcraft.ktor.template.core.logger.LoggerFactory
import java.util.concurrent.ConcurrentLinkedQueue

/** A captured log event. */
public data class LogEvent(
    public val logger: String,
    public val level: Level,
    public val message: String,
    public val fields: Map<String, Any?>,
    public val throwable: Throwable?
) {
    public enum class Level { DEBUG, INFO, WARN, ERROR }
}

/** A [LoggerFactory] whose loggers record every event in [events] instead of printing it. */
public class RecordingLoggerFactory : LoggerFactory {
    private val recorded = ConcurrentLinkedQueue<LogEvent>()

    public val events: List<LogEvent> get() = recorded.toList()

    override fun logger(name: String): Logger = object : Logger {
        override fun debug(message: String, vararg fields: LogField) = record(LogEvent.Level.DEBUG, message, fields, throwable = null)

        override fun info(message: String, vararg fields: LogField) = record(LogEvent.Level.INFO, message, fields, throwable = null)

        override fun warn(message: String, vararg fields: LogField, throwable: Throwable?) = record(LogEvent.Level.WARN, message, fields, throwable)

        override fun error(message: String, vararg fields: LogField, throwable: Throwable?) = record(LogEvent.Level.ERROR, message, fields, throwable)

        private fun record(
            level: LogEvent.Level,
            message: String,
            fields: Array<out LogField>,
            throwable: Throwable?
        ) {
            recorded += LogEvent(name, level, message, fields.toMap(), throwable)
        }
    }
}
