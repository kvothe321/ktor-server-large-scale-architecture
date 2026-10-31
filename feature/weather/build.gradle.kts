plugins {
    alias(libs.plugins.template.ktor.feature)
}

dependencies {
    implementation(projects.feature.weather.domain)
    implementation(projects.feature.weather.data)
    implementation(projects.feature.weather.presentation)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.network)

    testImplementation(libs.ktor.client.mock)
}
