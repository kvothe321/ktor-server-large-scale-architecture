plugins {
    alias(libs.plugins.template.ktor.feature)
}

dependencies {
    // Route constructors take the domain's use cases.
    api(projects.feature.images.domain)
}
