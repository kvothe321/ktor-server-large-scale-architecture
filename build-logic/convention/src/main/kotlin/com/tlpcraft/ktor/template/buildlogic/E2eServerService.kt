package com.tlpcraft.ktor.template.buildlogic

import com.sun.net.httpserver.HttpServer
import java.io.File
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.URI
import java.util.concurrent.TimeUnit
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

/**
 * Owns the processes the end-to-end suite runs against: a fake Open-Meteo endpoint (so the suite
 * needs no internet access and is deterministic) and the packaged server itself, configured purely
 * through environment variables, exactly as in production.
 *
 * Gradle closes the service when the build finishes, which stops both even if tests fail.
 */
abstract class E2eServerService : BuildService<E2eServerService.Params>, AutoCloseable {
    interface Params : BuildServiceParameters {
        val serverJar: RegularFileProperty
        val workDir: DirectoryProperty
    }

    private var weatherStub: HttpServer? = null
    private var server: Process? = null
    private var baseUrl: String? = null

    /** Starts the stub and the server once per build with [java] and returns the server's base URL. */
    @Synchronized
    fun start(java: File): String = baseUrl ?: launch(java).also { baseUrl = it }

    private fun launch(java: File): String {
        val workDir = parameters.workDir.get().asFile.apply {
            deleteRecursively()
            mkdirs()
        }
        val stub = startWeatherStub().also { weatherStub = it }
        val port = ServerSocket(0).use { it.localPort }
        val log = workDir.resolve("server.log")
        val process = ProcessBuilder(java.absolutePath, "-jar", parameters.serverJar.get().asFile.absolutePath)
            .directory(workDir)
            .redirectErrorStream(true)
            .redirectOutput(log)
            .apply {
                environment().putAll(
                    mapOf(
                        "PORT" to port.toString(),
                        "DATABASE_URL" to "jdbc:h2:mem:e2e;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
                        "DATABASE_USER" to "sa",
                        "DATABASE_PASSWORD" to "",
                        "STORAGE_ROOT" to workDir.resolve("uploads").absolutePath,
                        "WEATHER_BASE_URL" to "http://127.0.0.1:${stub.address.port}"
                    )
                )
            }
            .start()
        server = process
        val url = "http://127.0.0.1:$port"
        awaitHealthy(url, process, log)
        return url
    }

    private fun awaitHealthy(url: String, process: Process, log: File) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(STARTUP_TIMEOUT_SECONDS)
        while (System.nanoTime() < deadline) {
            check(process.isAlive) { "Server exited during startup. Log:\n${log.readText()}" }
            if (isHealthy(url)) return
            Thread.sleep(POLL_INTERVAL_MILLIS)
        }
        error("Server not healthy after $STARTUP_TIMEOUT_SECONDS s. Log:\n${log.readText()}")
    }

    private fun isHealthy(url: String): Boolean = runCatching {
        val connection = URI("$url/health").toURL().openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = POLL_INTERVAL_MILLIS.toInt()
            connection.responseCode == HttpURLConnection.HTTP_OK
        } finally {
            connection.disconnect()
        }
    }.getOrDefault(false)

    /** Answers every forecast request with fixed current conditions, timestamped now. */
    private fun startWeatherStub(): HttpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/v1/forecast") { exchange ->
            val epochSeconds = System.currentTimeMillis() / MILLIS_PER_SECOND
            val body = """{"current":{"time":$epochSeconds,"temperature_2m":18.5,"wind_speed_10m":7.2,"weather_code":2}}"""
                .toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        start()
    }

    override fun close() {
        server?.let { process ->
            process.destroy()
            if (!process.waitFor(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) process.destroyForcibly()
        }
        weatherStub?.stop(0)
    }

    private companion object {
        const val STARTUP_TIMEOUT_SECONDS = 90L
        const val SHUTDOWN_TIMEOUT_SECONDS = 15L
        const val POLL_INTERVAL_MILLIS = 500L
        const val MILLIS_PER_SECOND = 1000
    }
}
