plugins {
    // Put third-party plugins on the build classpath once, so convention plugins can apply them by id.
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kotlinter) apply false

    alias(libs.plugins.template.git.hooks)
}
