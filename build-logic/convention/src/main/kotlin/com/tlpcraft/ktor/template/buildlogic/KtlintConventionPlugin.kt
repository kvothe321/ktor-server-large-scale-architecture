package com.tlpcraft.ktor.template.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jmailen.gradle.kotlinter.KotlinterExtension

/**
 * Applies ktlint through kotlinter. Rules and code style (`ktlint_official`) come from `.editorconfig`.
 *
 * Kotlinter names its tasks `lintKotlin`/`formatKotlin`; `ktlintCheck`/`ktlintFormat` are registered as
 * aliases so the familiar names work too.
 */
class KtlintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(libs.pluginId("kotlinter"))

        extensions.configure<KotlinterExtension> {
            ktlintVersion = libs.version("ktlint")
            ignoreFormatFailures = false
        }

        tasks.register("ktlintCheck") {
            group = "verification"
            description = "Runs ktlint over all Kotlin sources (alias for lintKotlin)."
            dependsOn("lintKotlin")
        }
        tasks.register("ktlintFormat") {
            group = "formatting"
            description = "Formats all Kotlin sources with ktlint (alias for formatKotlin)."
            dependsOn("formatKotlin")
        }
    }
}
