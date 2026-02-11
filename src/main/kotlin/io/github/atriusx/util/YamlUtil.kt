package io.github.atriusx.util

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import org.slf4j.LoggerFactory
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
        logger.error("Failed to parse file '${file.path}', received error: ${e.message}")
        null
    }

    fun write(file: File, value: Any) = try {
        logger.debug("Writing provided content to ${file.name}...")
        yaml.writeValue(file, value)
    } catch (e: IOException) {
        logger.error("Failed to write content to file '${file.path}', received error: ${e.message}")
    }

    private val logger = LoggerFactory
        .getLogger(YamlUtil::class.java)
}