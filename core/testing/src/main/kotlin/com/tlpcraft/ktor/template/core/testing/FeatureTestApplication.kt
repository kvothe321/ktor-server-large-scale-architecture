package com.tlpcraft.ktor.template.core.testing

import com.tlpcraft.ktor.template.core.server.API_PREFIX
import com.tlpcraft.ktor.template.core.server.AppJson
import com.tlpcraft.ktor.template.core.server.ServerFeature
import com.tlpcraft.ktor.template.core.server.configureErrorHandling
import com.tlpcraft.ktor.template.core.server.configureSerialization
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.koin.core.module.Module
import org.koin.ktor.plugin.KoinIsolated

/**
 * Starts [feature] in a test server wired like production (serialization, error handling, routes
 * under [API_PREFIX]) on a fresh H2 database, then runs [block] with a JSON-aware client.
 *
 * @param config the feature's configuration keys, as they would appear in `application.conf`.
 * @param extraModules extra Koin modules, e.g. a mock-engine `HttpClient` for features that call out.
 */
public fun testFeature(
    feature: ServerFeature,
    testEnvironment: TestEnvironment = TestEnvironment(),
    config: Map<String, String> = emptyMap(),
    extraModules: List<Module> = emptyList(),
    block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit
): Unit = testApplication {
    environment {
        this.config = MapApplicationConfig(config.toList())
    }
    application {
        val featureModule = feature.koinModule(environment.config)
        install(KoinIsolated) {
            modules(listOf(testEnvironment.coreModule(), featureModule) + extraModules)
        }
        configureSerialization()
        configureErrorHandling(testEnvironment.loggerFactory)
        routing {
            route(API_PREFIX) { feature.routes(this) }
        }
    }
    val client = createClient {
        install(ContentNegotiation) { json(AppJson) }
    }
    block(client)
}
