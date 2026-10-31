plugins {
    alias(libs.plugins.template.kotlin.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(projects.core.domain)
    api(libs.ktor.client.core)
    api(libs.kotlinx.serialization.json)

    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)

    testImplementation(libs.ktor.client.mock)
}
