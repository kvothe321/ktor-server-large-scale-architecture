plugins {
    alias(libs.plugins.template.kotlin.jvm)
}

dependencies {
    // Exposed in the public factory signatures, so consumers need them too.
    api(projects.feature.images.domain)
    api(projects.core.database)
    api(projects.core.logger)
    api(projects.core.storage)

    implementation(projects.core.data)
}
