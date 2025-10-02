package xyz.atrius.config

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe

class CompositesConfigTest : DescribeSpec({

    val yaml = YAMLMapper.builder()
        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        .build()

    describe("CompositesConfig Tests") {

        describe("A successful config read") {
            val config = """
                |enabled: true
                |composites:
                |  foo:
                |    bar:
                |      baz: ../hello/world
                |    baz: ../goodbye
                |  baz.bar.biz: ../no/way
                |  foo.baz:bar: ../a/b/c
            """.trimMargin()
            val composites = yaml
                .readValue(config, CompositesConfig::class.java)
                .processQualifiers()

            it("Should resolve the correct composite count") {
                composites.size shouldBe 4
            }

            it("Should resolve the correct mappings") {
                composites.shouldContainAll(
                    mapOf(
                        "foo.bar:baz" to "../hello/world",
                        "foo:baz" to "../goodbye",
                        "baz.bar:biz" to "../no/way",
                        "foo.baz:bar" to "../a/b/c",
                    )
                )
            }
        }

        describe("When the config is empty") {
            it("Should return throw an error") {
                shouldThrow<JacksonException> {
                    yaml
                        .readValue("", CompositesConfig::class.java)
                        .processQualifiers()
                }
            }
        }

        describe("When the enabled field is provided, but no composites") {
            val content = """
                |enabled: true
            """.trimMargin()
            val composites = yaml
                .readValue(content, CompositesConfig::class.java)

            it("Should be enabled") {
                composites.enabled.shouldBeTrue()
            }

            it("Should have empty composites") {
                composites.composites.shouldBeEmpty()
            }
        }

        describe("When the composites field is provided, but no enabled field") {
            val content = """
                |composites:
                |  foo.bar.baz: ../test/hello
            """.trimMargin()
            val composites = yaml
                .readValue(content, CompositesConfig::class.java)

            it("Should be disabled") {
                composites.enabled.shouldBeFalse()
            }

            it("Should have single composite") {
                composites.processQualifiers().shouldContain(
                    "foo.bar:baz" to "../test/hello"
                )
            }
        }

        describe("When some composite groups have no children") {
            val content = """
                |composites:
                |  foo.bar:
                |    baz:
                |  baz.foo.biz: ../single/replacement
            """.trimMargin()
            val composites = yaml
                .readValue(content, CompositesConfig::class.java)
                .processQualifiers()

            it("Should resolve the correct composite count") {
                composites.size shouldBe 1
            }

            it("Should resolve the correct mappings") {
                composites.shouldContain(
                    "baz.foo:biz" to "../single/replacement"
                )
            }
        }

        describe("When some composite groups have an object child") {
            val content = """
                |composites:
                |  foo.bar:
                |    baz: {}
                |  baz.foo.biz: ../single/replacement
            """.trimMargin()
            val composites = yaml
                .readValue(content, CompositesConfig::class.java)
                .processQualifiers()

            it("Should resolve the correct composite count") {
                composites.size shouldBe 1
            }

            it("Should resolve the correct mappings") {
                composites.shouldContain(
                    "baz.foo:biz" to "../single/replacement"
                )
            }
        }
    }
})
