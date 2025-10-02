
plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "2.2.0"
    id("io.kotest.multiplatform") version "5.9.1"
}

group = "xyz.atrius"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    val kotestVersion = "5.9.1"

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.20.0")

    testImplementation("io.kotest:kotest-framework-engine:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {

    plugins {
        // This is the main plugin. Unlike others, it needs to be applied under the settings.gradle file.
        // This plugin is responsible for reading the composites file if it exists, and attempting to parse
        // it into useful dependency/project relations. This will then substitute out the selected dependencies
        // for the local projects provided in the configuration.
        create("Compositor") {
            id = "$group.compositor"
            implementationClass = "$group.Compositor"
        }
        // This plugin handles generating the gradle tasks for creating/deleting or enabling/disabling the
        // composites file. It should not be applied manually, and should instead be auto-loaded in via
        // the settings plugin provided above.
        create("Compositor-Project-Auto") {
            id = "$group.compositor-project-auto"
            implementationClass = "$group.CompositorProjectAuto"
        }
    }
}