package com.tlpcraft.ktor.template.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Base convention for every JVM module: Kotlin/JVM on the shared toolchain, warnings as errors,
 * and the shared quality gates (detekt, ktlint, testing).
 */
class KotlinJvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(libs.pluginId("kotlin-jvm"))
        apply<DetektConventionPlugin>()
        apply<KtlintConventionPlugin>()
        apply<TestingConventionPlugin>()

        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(libs.version("jvmToolchain").toInt())
            compilerOptions {
                allWarningsAsErrors.set(true)
                freeCompilerArgs.add("-Xconsistent-data-class-copy-visibility")
            }
        }
    }
}
