plugins {
    alias(libs.plugins.template.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.feature.weather.domain)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.logger)
    implementation(projects.core.network)

    testImplementation(libs.ktor.client.mock)
}
