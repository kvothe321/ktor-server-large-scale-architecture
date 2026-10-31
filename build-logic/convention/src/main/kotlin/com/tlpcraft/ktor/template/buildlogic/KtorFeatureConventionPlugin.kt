package com.tlpcraft.ktor.template.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Convention for HTTP-facing feature modules (`feature:<name>` and `feature:<name>:presentation`):
 * Ktor server, kotlinx.serialization, Koin and the shared server infrastructure from `core:server`.
 */
class KtorFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply<KotlinJvmConventionPlugin>()
        pluginManager.apply(libs.pluginId("kotlin-serialization"))

        extensions.configure<KotlinJvmProjectExtension> {
            // Route documentation (describe/hide) is still marked experimental in Ktor.
            compilerOptions.optIn.add("io.ktor.utils.io.ExperimentalKtorApi")
        }

        dependencies {
            add("implementation", project(":core:model"))
            add("implementation", project(":core:domain"))
            add("implementation", project(":core:logger"))
            add("implementation", project(":core:server"))
            libs.findBundle("ktor-server").ifPresent { add("implementation", it) }
            add("implementation", libs.library("kotlinx-serialization-json"))
            add("implementation", libs.library("koin-ktor"))

            add("testImplementation", libs.library("ktor-server-test-host"))
        }
    }
}
