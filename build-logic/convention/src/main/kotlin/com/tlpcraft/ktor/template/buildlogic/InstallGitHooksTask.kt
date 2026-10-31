package com.tlpcraft.ktor.template.buildlogic

import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.process.ExecOperations

/**
 * Copies every script in [hooksSourceDir] into the Git hooks directory and marks it executable.
 *
 * The hooks directory is resolved with `git rev-parse --git-common-dir`, so installation also
 * works from a linked worktree, where `.git` is a file rather than a directory.
 */
@UntrackedTask(because = "Writes into the .git directory, which Gradle must not snapshot")
abstract class InstallGitHooksTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val hooksSourceDir: DirectoryProperty

    @get:Internal
    abstract val repositoryDir: DirectoryProperty

    @get:Inject
    protected abstract val execOperations: ExecOperations

    @get:Inject
    protected abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun install() {
        val hooksDir = resolveHooksDir()
        fileSystemOperations.copy {
            from(hooksSourceDir)
            into(hooksDir)
            filePermissions { unix("rwxr-xr-x") }
        }
        logger.lifecycle("Installed Git hooks into $hooksDir")
    }

    private fun resolveHooksDir(): File {
        val repoDir = repositoryDir.get().asFile
        val output = ByteArrayOutputStream()
        execOperations.exec {
            workingDir = repoDir
            commandLine("git", "rev-parse", "--git-common-dir")
            standardOutput = output
        }
        val gitCommonDir = File(output.toString().trim())
        val absoluteGitDir = if (gitCommonDir.isAbsolute) gitCommonDir else repoDir.resolve(gitCommonDir)
        return absoluteGitDir.resolve("hooks")
    }
}
