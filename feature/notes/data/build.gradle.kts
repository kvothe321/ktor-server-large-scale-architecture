plugins {
    alias(libs.plugins.template.kotlin.jvm)
}

dependencies {
    // Exposed in the public factory signatures, so consumers need them too.
    api(projects.feature.notes.domain)
    api(projects.core.database)
    api(projects.core.logger)

    implementation(projects.core.data)
}
