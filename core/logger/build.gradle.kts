plugins {
    alias(libs.plugins.template.kotlin.library)
}

dependencies {
    implementation(libs.slf4j.api)

    testImplementation(libs.logback.classic)
}
