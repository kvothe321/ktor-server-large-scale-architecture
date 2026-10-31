plugins {
    alias(libs.plugins.template.kotlin.library)
}

dependencies {
    api(projects.core.domain)
    api(projects.core.data)
    api(projects.core.logger)
    api(projects.core.database)
    api(projects.core.server)
    api(libs.ktor.server.test.host)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.client.mock)
    api(libs.kotlin.test)
    api(libs.kotlinx.coroutines.test)
}
