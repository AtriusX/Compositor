package xyz.atrius

import org.gradle.api.Plugin
import org.gradle.api.artifacts.DependencySubstitutions
import org.gradle.api.initialization.Settings
import org.gradle.internal.cc.base.logger
import xyz.atrius.config.CompositesConfig
import xyz.atrius.util.CompositesUtil.getCompositesFile
import xyz.atrius.util.YamlUtil

/**
 * @author Atrius
 *
 * The primary plugin. This primarily handles reading composition info from the `composites.yml` file, and swapping out
 * the requested dependencies for the provided local projects. This additionally injects an embedded plugin into the
 * project scope which facilitates the generation of Gradle tasks necessary for the function of this tool.
 */
@Suppress("unused")
class Compositor : Plugin<Settings> {

    override fun apply(target: Settings) {
        // Apply this so projects don't need to define the project plugin manually
        target.applyProjectPlugin()

        val file = target.getCompositesFile()

        if (!file.exists()) {
            logger.debug("No composites applied, ${file.name} does not exist.")
            return
        }

        val config = YamlUtil
            .parse(file, CompositesConfig::class)
            ?: return

        if (!config.enabled) {
            logger.debug("No composites applied, composition is disabled.")
            return
        }

        for ((dependency, path) in config.processQualifiers()) {
            val root = path.substringBefore(":")

            target.includeBuild(root) { build ->
                // Substitute the provided dependency for a local project
                build.dependencySubstitution {
                    it.swap(dependency, path)
                }

                logger.debug("Swapped out dependency '$dependency' for project: $path")
            }
        }
    }

    private fun DependencySubstitutions.swap(dependency: String, path: String) {
        val module = module(dependency)
        val subProject = path.substringAfter(":", "")
        val project = project(":$subProject")

        substitute(module).using(project)
    }

    private fun Settings.applyProjectPlugin() = gradle.beforeProject {
        it.rootProject.pluginManager.apply(CompositorProjectAuto::class.java)
    }
}