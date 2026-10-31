package com.tlpcraft.ktor.template.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerIfAbsent

/**
 * Black-box end-to-end suite. Tests live in the `e2eTest` source set and talk to the server over
 * HTTP only: the module has no project dependencies, so it cannot reach into server code.
 *
 * `e2eTest` is not part of `check`. By default it starts the packaged `app.jar` (in-memory H2, a
 * fake weather provider) through [E2eServerService]; with `E2E_BASE_URL` set it targets that
 * server instead, e.g. one started with docker-compose.
 */
class E2eConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        configureKotlinJvm()
        pluginManager.apply(libs.pluginId("kotlin-serialization"))

        val e2eSourceSet = extensions.getByType<SourceSetContainer>().create(SOURCE_SET)
        dependencies {
            val junitBom = platform(libs.library("junit-bom"))
            add("${SOURCE_SET}Implementation", junitBom)
            add("${SOURCE_SET}Implementation", libs.library("kotlin-test-junit5"))
            add("${SOURCE_SET}Implementation", libs.library("kotlinx-coroutines-test"))
            add("${SOURCE_SET}RuntimeOnly", junitBom)
            add("${SOURCE_SET}RuntimeOnly", libs.library("junit-platform-launcher"))
        }

        val server = gradle.sharedServices.registerIfAbsent("e2eServer", E2eServerService::class) {
            parameters.serverJar.set(rootProject.layout.projectDirectory.file("app/build/libs/app.jar"))
            parameters.workDir.set(layout.buildDirectory.dir("e2e"))
        }
        val externalBaseUrl = providers.environmentVariable(BASE_URL_ENV)

        tasks.register<Test>(SOURCE_SET) {
            group = "verification"
            description = "Runs the black-box end-to-end suite against app.jar, or against \$$BASE_URL_ENV when set."
            testClassesDirs = e2eSourceSet.output.classesDirs
            classpath = e2eSourceSet.runtimeClasspath
            useJUnitPlatform()
            testLogging {
                events(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
                exceptionFormat = TestExceptionFormat.FULL
            }
            outputs.upToDateWhen { false }
            if (!externalBaseUrl.isPresent) dependsOn(":app:buildFatJar")
            usesService(server)
            doFirst {
                // The server runs on the same toolchain JDK as the tests.
                val java = javaLauncher.get().executablePath.asFile
                systemProperty(BASE_URL_PROPERTY, externalBaseUrl.orNull ?: server.get().start(java))
            }
        }

        // The suite only runs on demand, but it must keep compiling with the rest of the build.
        tasks.named("check") { dependsOn("compile${SOURCE_SET.replaceFirstChar(Char::uppercase)}Kotlin") }
    }

    private companion object {
        const val SOURCE_SET = "e2eTest"
        const val BASE_URL_ENV = "E2E_BASE_URL"
        const val BASE_URL_PROPERTY = "e2e.baseUrl"
    }
}
