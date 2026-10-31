plugins {
    alias(libs.plugins.template.e2e)
}

// Black-box on purpose: no project dependencies. The suite knows the server only through its
// HTTP API, so it can run unchanged against any deployment (see E2E_BASE_URL in the README).
dependencies {
    e2eTestImplementation(libs.ktor.client.core)
    e2eTestImplementation(libs.ktor.client.cio)
    e2eTestImplementation(libs.ktor.client.content.negotiation)
    e2eTestImplementation(libs.ktor.serialization.kotlinx.json)
    e2eTestImplementation(libs.kotlinx.serialization.json)
}
