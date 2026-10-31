package com.tlpcraft.ktor.template.buildlogic

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

/**
 * Applies detekt on top of its default rule set, using the shared `config/detekt/detekt.yml`.
 * Formatting is intentionally left to ktlint (see [KtlintConventionPlugin]).
 */
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(libs.pluginId("detekt"))

        extensions.configure<DetektExtension> {
            toolVersion = libs.version("detekt")
            buildUponDefaultConfig = true
            parallel = true
            config.setFrom(rootProject.file("config/detekt/detekt.yml"))
            source.setFrom(
                "src/main/kotlin",
                "src/test/kotlin",
                "src/testFixtures/kotlin"
            )
        }

        tasks.withType<Detekt>().configureEach {
            jvmTarget = libs.version("jvmToolchain")
            reports {
                html.required.set(true)
                xml.required.set(false)
                txt.required.set(false)
                sarif.required.set(false)
                md.required.set(false)
            }
        }

        // detekt 1.23.x embeds the Kotlin compiler it was built with and aborts when the
        // Kotlin Gradle plugin bumps its classpath to a newer version.
        val detektKotlinVersion = libs.version("detektKotlin")
        configurations.matching { it.name == "detekt" }.configureEach {
            resolutionStrategy.eachDependency {
                if (requested.group == "org.jetbrains.kotlin") {
                    useVersion(detektKotlinVersion)
                }
            }
        }
    }
}
