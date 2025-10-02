package xyz.atrius

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.cc.base.logger
import xyz.atrius.config.CompositesConfig
import xyz.atrius.util.CompositesUtil
import xyz.atrius.util.CompositesUtil.getCompositesFile
import xyz.atrius.util.YamlUtil
import java.io.File

/**
 * @author Atrius
 *
 * A project-scope plugin which is meant to be automatically applied by the [Compositor] plugin. This should not be
 * applied manually. The primary purpose of this plugin is simply to setup a collection of Gradle tasks which can be
 * used to generate, delete, or enable/disable the composites config.
 */
@Suppress("unused")
class CompositorProjectAuto : Plugin<Project> {

    override fun apply(target: Project) {
        val file = target.getCompositesFile()

        target.compositorTask("createCompositesConfig") {
            if (!file.exists()) {
                logger.debug("Creating ${file.path}...")
                file.createNewFile()
            }
            logger.debug("Writing default content to file...")
            file.writeText(CompositesUtil.defaultFileContent(), Charsets.UTF_8)
            logger.debug("Done!")
        }

        target.compositorTask("deleteCompositesConfig") {
            if (file.exists()) {
                logger.debug("Deleting ${file.path}...")
                file.delete()
                logger.debug("Finished deleting!")
            }
        }

        target.compositorTask("enableComposites") {
            setEnabled(file, true)
            logger.debug("Enabled ${file.name} configuration.")
        }

        target.compositorTask("disableComposites") {
            setEnabled(file, false)
            logger.debug("Disabled ${file.name} configuration.")
        }
    }

    private fun Project.compositorTask(taskName: String, action: () -> Unit) {
        tasks.register(taskName) {
            // Task Execution
            it.doLast { action() }
            // Task Configuration
            it.group = "composition"
            it.outputs.upToDateWhen { false }
        }
    }

    private fun setEnabled(file: File, enabled: Boolean) {
        if (!file.exists()) {
            logger.error("Cannot configure ${file.path}, file does not exist!")
            return
        }

        val composites = YamlUtil
            .parse(file, CompositesConfig::class)
            ?.apply { this.enabled = enabled }
            ?: return
        // Save content back to file
        YamlUtil.write(file, composites)
    }
}