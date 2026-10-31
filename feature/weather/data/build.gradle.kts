plugins {
    alias(libs.plugins.template.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    // Exposed in the public factory signatures, so consumers need them too.
    api(projects.feature.weather.domain)
    api(projects.core.database)
    api(projects.core.logger)
    api(projects.core.network)

    implementation(projects.core.data)

    testImplementation(libs.ktor.client.mock)
}
