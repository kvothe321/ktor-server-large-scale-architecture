plugins {
    alias(libs.plugins.template.ktor.feature)
}

dependencies {
    implementation(projects.feature.weather.domain)
}
