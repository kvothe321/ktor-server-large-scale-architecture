plugins {
    alias(libs.plugins.template.kotlin.library)
}

dependencies {
    api(projects.core.domain)
    api(libs.exposed.core)
    api(libs.exposed.jdbc)
    api(libs.exposed.kotlin.datetime)

    implementation(libs.hikari)
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.database.postgresql)
    runtimeOnly(libs.h2)
    runtimeOnly(libs.postgresql)
}
