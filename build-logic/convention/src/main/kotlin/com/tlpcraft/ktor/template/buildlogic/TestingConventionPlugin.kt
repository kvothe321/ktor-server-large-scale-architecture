package com.tlpcraft.ktor.template.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.withType

/**
 * Wires JUnit Platform, kotlin.test, coroutine test utilities and the shared `core:testing`
 * fixtures into every module's test source set.
 */
class TestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.withPlugin(libs.pluginId("kotlin-jvm")) {
            dependencies {
                val junitBom = platform(libs.library("junit-bom"))
                add("testImplementation", junitBom)
                add("testImplementation", libs.library("kotlin-test-junit5"))
                add("testImplementation", libs.library("kotlinx-coroutines-test"))
                add("testRuntimeOnly", junitBom)
                add("testRuntimeOnly", libs.library("junit-platform-launcher"))
                if (path != TESTING_PROJECT_PATH) {
                    add("testImplementation", project(TESTING_PROJECT_PATH))
                }
            }
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            testLogging {
                events(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
                exceptionFormat = TestExceptionFormat.FULL
            }
        }
    }

    private companion object {
        const val TESTING_PROJECT_PATH = ":core:testing"
    }
}
