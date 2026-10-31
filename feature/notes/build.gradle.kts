plugins {
    alias(libs.plugins.template.ktor.feature)
}

dependencies {
    implementation(projects.feature.notes.domain)
    implementation(projects.feature.notes.data)
    implementation(projects.feature.notes.presentation)
    implementation(projects.core.data)
    implementation(projects.core.database)
}
