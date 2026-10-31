plugins {
    alias(libs.plugins.template.kotlin.library)
}

dependencies {
    api(projects.core.domain)
}
