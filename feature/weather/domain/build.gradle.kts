plugins {
    alias(libs.plugins.template.kotlin.jvm)
    `java-test-fixtures`
}

dependencies {
    api(projects.core.domain)

    testFixturesImplementation(projects.core.domain)
}
