import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.tlpcraft.ktor.template.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.detekt.gradle.plugin)
    compileOnly(libs.kotlinter.gradle.plugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("kotlinJvm") {
            id = libs.plugins.template.kotlin.jvm.get().pluginId
            implementationClass = "com.tlpcraft.ktor.template.buildlogic.KotlinJvmConventionPlugin"
        }
        register("kotlinLibrary") {
            id = libs.plugins.template.kotlin.library.get().pluginId
            implementationClass = "com.tlpcraft.ktor.template.buildlogic.KotlinLibraryConventionPlugin"
        }
        register("ktorFeature") {
            id = libs.plugins.template.ktor.feature.get().pluginId
            implementationClass = "com.tlpcraft.ktor.template.buildlogic.KtorFeatureConventionPlugin"
        }
        register("testing") {
            id = libs.plugins.template.testing.get().pluginId
            implementationClass = "com.tlpcraft.ktor.template.buildlogic.TestingConventionPlugin"
        }
        register("detekt") {
            id = libs.plugins.template.detekt.get().pluginId
            implementationClass = "com.tlpcraft.ktor.template.buildlogic.DetektConventionPlugin"
        }
        register("ktlint") {
            id = libs.plugins.template.ktlint.get().pluginId
            implementationClass = "com.tlpcraft.ktor.template.buildlogic.KtlintConventionPlugin"
        }
        register("gitHooks") {
            id = libs.plugins.template.git.hooks.get().pluginId
            implementationClass = "com.tlpcraft.ktor.template.buildlogic.GitHooksConventionPlugin"
        }
    }
}
