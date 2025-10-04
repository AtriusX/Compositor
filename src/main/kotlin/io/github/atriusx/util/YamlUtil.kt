package io.github.atriusx.util

import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import org.gradle.internal.cc.base.logger
import java.io.File
import java.io.IOException
import kotlin.reflect.KClass

/**
 * @author Atrius
 */
object YamlUtil {

    private val yaml = YAMLMapper.builder()
        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        .build()

    fun <T : Any> parse(file: File, type: KClass<T>): T? = try {
        logger.debug("Attempting to parse ${file.name} content as YAML...")
        yaml.readValue(file, type.java)
    } catch (e: IOException) {
        logger.error("Failed to parse file '${file.path}', error: ${e.message}")
        null
    }

    fun write(file: File, value: Any) {
        logger.debug("Writing provided content to ${file.name}...")
        yaml.writeValue(file, value)
    }
}