plugins {
    alias(libs.plugins.template.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.feature.notes.domain)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.logger)
}
