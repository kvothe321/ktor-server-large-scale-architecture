plugins {
    alias(libs.plugins.template.ktor.feature)
}

dependencies {
    implementation(projects.feature.images.domain)
    implementation(projects.feature.images.data)
    implementation(projects.feature.images.presentation)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.storage)

    testImplementation(testFixtures(projects.feature.images.domain))
}
