package com.tlpcraft.ktor.template.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

/**
 * Root-only plugin exposing `installGitHooks`, which copies the versioned hooks from
 * `scripts/git-hooks` into the repository's hooks directory. Installation is opt-in.
 */
class GitHooksConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        check(this == rootProject) { "template.git.hooks must be applied to the root project only" }

        tasks.register<InstallGitHooksTask>("installGitHooks") {
            group = "git hooks"
            description = "Installs the pre-commit hook that runs ktlintCheck and detekt."
            hooksSourceDir.set(layout.projectDirectory.dir("scripts/git-hooks"))
            repositoryDir.set(layout.projectDirectory)
        }
    }
}
