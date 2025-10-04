package io.github.atriusx.config

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
                history[path.formatQualifier()] = value
                path.removeLast(next.size)
            }
            // Recursively check next layer
            if (value is Map<*, *>) {
                flattenPaths(value as? Map<String, Any>, path, history)
                // Remove all recently added scopes
                path.removeLast(next.size)
            }
        }

        return history
    }

    private fun <T> MutableList<T>.removeLast(count: Int) = this.apply {
        repeat(count) { removeLast() }
    }

    private fun List<String>.formatQualifier() = when (size) {
        0, 1 -> firstOrNull() ?: ""
        else -> dropLast(1).joinToString(".") + ":" + last()
    }
}