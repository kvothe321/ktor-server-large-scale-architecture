plugins {
    alias(libs.plugins.template.ktor.feature)
}

dependencies {
    implementation(projects.feature.images.domain)
    implementation(projects.feature.images.data)
    implementation(projects.feature.images.presentation)

    testImplementation(projects.core.storage)

    testImplementation(testFixtures(projects.feature.images.domain))
}
