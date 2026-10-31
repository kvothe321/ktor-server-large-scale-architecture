package com.tlpcraft.ktor.template.core.server

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.routing.Route
import org.koin.core.module.Module

/**
 * A vertical slice of the API. The application installs each feature by loading its Koin module and
 * mounting its routes under the API prefix; nothing else in `app` needs to know about it.
 */
public interface ServerFeature {
    /** Dependencies of this feature. Reads the feature's own section of [config]. */
    public fun koinModule(config: ApplicationConfig): Module

    /** Mounts this feature's endpoints on [route]. */
    public fun routes(route: Route)
}

/** Path prefix under which every feature's routes are mounted. */
public const val API_PREFIX: String = "/api/v1"
