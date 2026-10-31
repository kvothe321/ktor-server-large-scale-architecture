plugins {
    alias(libs.plugins.template.kotlin.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    compilerOptions {
        // Route documentation (describe/hide) is still marked experimental in Ktor.
        optIn.add("io.ktor.utils.io.ExperimentalKtorApi")
    }
}

dependencies {
    api(projects.core.domain)
    api(projects.core.logger)
    api(libs.bundles.ktor.server)
    api(libs.ktor.server.status.pages)
    api(libs.ktor.server.routing.openapi)
    api(libs.koin.ktor)
    api(libs.kotlinx.serialization.json)

    testImplementation(libs.ktor.server.test.host)
}
