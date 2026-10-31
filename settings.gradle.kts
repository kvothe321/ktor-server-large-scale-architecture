pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "ktor-server-large-scale-architecture"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":core:model",
    ":core:domain",
    ":core:logger",
    ":core:data",
    ":core:database",
    ":core:network",
    ":core:storage",
    ":core:server",
    ":core:testing"
)

include(features("weather", "notes", "images"))

/** Every feature is a wiring module plus its three clean-architecture layers. */
fun features(vararg names: String): List<String> = names.flatMap { name ->
    listOf(":feature:$name", ":feature:$name:domain", ":feature:$name:data", ":feature:$name:presentation")
}
