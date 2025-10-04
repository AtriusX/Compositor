package io.github.atriusx.util

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.intellij.lang.annotations.Language
import java.io.File

/**
 * @author Atrius
 */
object CompositesUtil {

    private const val COMPOSITES_LOCATION = "gradle/composites.yml"

    fun Settings.getCompositesFile(): File = settingsDir
        .resolve(COMPOSITES_LOCATION)

    fun Project.getCompositesFile(): File = projectDir
        .resolve(COMPOSITES_LOCATION)

    @Language("YML")
    fun defaultFileContent(): String = """
        |# Determines if this composite configuration is active
        |enabled: true
        |
        |# Composite projects are defined under this section in the following format:
        |#
        |# composites:
        |#   some.project.dependency: "other/project/path"
        |#
        |# Alternatively, if you have multiple dependencies which share a common package
        |# root, you can define your dependencies like so:
        |#
        |# composites:
        |#  some.common.root:
        |#    dependency-a: "project/a/path"
        |#    dependency-b: "project/b/path"
        |#
        |# These will resolve out to dependencies "some.common.root:dependency-a" and
        |# "some.common.root:dependency-b" respectively and replace them with their
        |# corresponding project mappings.
        |composites: {}
    """.trimMargin()
}