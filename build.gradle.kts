import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.2.0"
    id("io.kotest.multiplatform") version "5.9.1"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "io.github.atriusx"

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

mavenPublishing {

    configure(
        GradlePlugin(
            javadocJar = JavadocJar.Javadoc(),
            sourcesJar = true,
        )
    )

    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(
        groupId = group as String,
        artifactId = rootProject.name.lowercase(),
        version = property("version") as String
    )

    pom {
        val projectWebsite: String by project
        val projectDescription: String by project

        name = rootProject.name
        description = projectDescription
        inceptionYear = "2025"
        url = projectWebsite

        licenses {
            val license: String by project
            val licenseLink: String by project

            license {
                name = license
                url = licenseLink
                distribution = licenseLink
            }
        }

        developers {
            val developerName: String by project
            val githubProfile: String by project

            developer {
                id = developerName
                name = developerName
                url = githubProfile
            }
        }

        scm {
            val scmUrl: String by project
            val scmConnection: String by project
            val scmDeveloperConnection: String by project

            url = scmUrl
            connection = scmConnection
            developerConnection = scmDeveloperConnection
        }
    }
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
    }
}
