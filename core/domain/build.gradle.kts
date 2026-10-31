plugins {
    alias(libs.plugins.template.kotlin.library)
}

dependencies {
    api(projects.core.model)
    api(libs.kotlinx.coroutines.core)
}
