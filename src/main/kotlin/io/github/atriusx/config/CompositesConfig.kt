package io.github.atriusx.config

import org.slf4j.LoggerFactory
import kotlin.collections.iterator

/**
 * @author Atrius
 */
data class CompositesConfig(
    var enabled: Boolean = false,
    val composites: Map<String, Any?> = hashMapOf(),
) {
    private val separator = "[.:]".toRegex()

    fun processQualifiers(): Map<String, String> =
        flattenPaths(composites)

    @Suppress("UNCHECKED_CAST")
    private fun flattenPaths(
        current: Map<String, Any?>?,
        path: MutableList<String> = arrayListOf(),
        history: MutableMap<String, String> = mutableMapOf(),
    ): Map<String, String> {
        if (current == null) {
            return history
        }
        // Iterator over children
        for ((name, value) in current) {
            // Scopes can include multiple package names, so we should split to simplify logic
            val next = name.split(separator)
            // Skip elements which have no value provided
            if (value == null) {
                continue
            }
            // Add current scope
            path.addAll(next)
            // Apply value
            if (value is String) {
                val qualifier = path.formatQualifier()
                history[qualifier] = value
                logger.debug("Found substituted dependency '$qualifier' with '$value'")
                path.removeLast(next.size)
                continue
            }
            val ymlPath = path.joinToString(".")
            // Recursively check next layer
            if (value is Map<*, *>) {
                logger.debug("Found nested scope: $ymlPath")
                flattenPaths(value as? Map<String, Any>, path, history)
                // Remove all recently added scopes
                path.removeLast(next.size)
                continue
            }

            logger.warn("Invalid value provided for '$ymlPath', expected path or nested scope, but found: $value")
        }

        logger.info("Finished processing composites configuration, found ${history.size} total substitutions.")

        for ((dependency, path) in history) {
            logger.debug("Mapped dependency '$dependency' to substitute at location: '$path'")
        }

        return history
    }

    // Kotlin doesn't have a built-in way to remove multiple elements from the end of a list in-place, so we can just
    // repeat this operation until we've removed the desired number of elements
    private fun <T> MutableList<T>.removeLast(count: Int) = this.apply {
        repeat(count) { removeLast() }
    }

    private fun List<String>.formatQualifier() = when (size) {
        0, 1 -> firstOrNull() ?: ""
        else -> dropLast(1).joinToString(".") + ":" + last()
    }

    companion object {

        private val logger = LoggerFactory
            .getLogger(CompositesConfig::class.java)
    }
}