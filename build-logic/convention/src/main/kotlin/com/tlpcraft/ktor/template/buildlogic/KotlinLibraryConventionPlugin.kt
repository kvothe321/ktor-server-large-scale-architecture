package com.tlpcraft.ktor.template.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Convention for shared `core:*` libraries: [KotlinJvmConventionPlugin] plus explicit API mode,
 * so every public declaration is a deliberate, visible contract.
 */
class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply<KotlinJvmConventionPlugin>()
        extensions.configure<KotlinJvmProjectExtension> {
            explicitApi()
        }
    }
}
