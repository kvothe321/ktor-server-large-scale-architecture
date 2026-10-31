package com.tlpcraft.ktor.template.core.logger

/** A structured key-value pair attached to a log event, e.g. `"noteId" to id`. */
public typealias LogField = Pair<String, Any?>

/**
 * Structured logging API used across the codebase, independent of the logging backend.
 *
 * Messages stay constant and human-readable; variable data goes into [LogField]s, which a JSON
 * encoder emits as separate properties that log pipelines can index and query.
 *
 * ```
 * logger.info("Image uploaded", "imageId" to id, "sizeBytes" to size)
 * ```
 */
public interface Logger {
    public fun debug(message: String, vararg fields: LogField)

    public fun info(message: String, vararg fields: LogField)

    public fun warn(message: String, vararg fields: LogField, throwable: Throwable? = null)

    public fun error(message: String, vararg fields: LogField, throwable: Throwable? = null)
}

/** Creates [Logger]s. Inject it rather than reaching for a static factory, so tests can record logs. */
public fun interface LoggerFactory {
    public fun logger(name: String): Logger
}

/** A [Logger] named after [T], the usual way to obtain a class logger. */
public inline fun <reified T : Any> LoggerFactory.logger(): Logger = logger(T::class.qualifiedName ?: T::class.java.name)
