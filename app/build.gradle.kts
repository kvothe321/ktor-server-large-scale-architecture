plugins {
    alias(libs.plugins.template.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
}

group = "com.tlpcraft.ktor.template"
version = "0.1.0"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    compilerOptions {
        // Route documentation (describe/hide) is still marked experimental in Ktor.
        optIn.add("io.ktor.utils.io.ExperimentalKtorApi")
    }
}

tasks.named<JavaExec>("run") {
    // Resolve relative paths in application.conf (./data/...) against the repository root.
    workingDir = rootDir
}

ktor {
    fatJar {
        archiveFileName = "app.jar"
    }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.logger)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.network)
    implementation(projects.core.storage)
    implementation(projects.core.server)

    implementation(projects.feature.weather)
    implementation(projects.feature.notes)
    implementation(projects.feature.images)

    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.routing.openapi)
    implementation(libs.ktor.server.swagger)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logstash.logback.encoder)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.koin.test)
}
