
plugins {
    `java-gradle-plugin`
    `maven-publish`
    signing
    kotlin("jvm") version "2.2.0"
    id("io.kotest.multiplatform") version "5.9.1"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

group = "xyz.atrius"

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
    // Since we provide two plugins together that we want deployed as a single plugin, we should disable this to avoid
    // publishing either of them separately when running through deployment pipelines.
    isAutomatedPublishing = false

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

publishing {
    // Maven Central repository configuration
    repositories {
        maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
            name = "MavenCentral"

            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
    // Both plugins should be deployed as a single artifact to ensure they can be setup automatically
    publications {

        val projectWebsite: String by project
        val license: String by project
        val licenseLink: String by project

        create<MavenPublication>("Compositor") {
            from(components["java"])

            groupId = group.toString()
            artifactId = rootProject.name
            version = property("version") as String

            pom {
                name = rootProject.name
                url = projectWebsite

                licenses {
                    license {
                        name = license
                        url = licenseLink
                    }
                }
            }
        }
    }
}
// Maven Central requires artifacts to be signed for upload
signing {
    val signingKey = System.getenv("SINGING_KEY")
    val signingPassword = System.getenv("SIGNING_PASSWORD")

    if (!signingKey.isNullOrEmpty()) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}
// Maven Central apparently requires staging repositories to be closed before a release can be published
// This should configure the plugin which will automate this process in the deployment pipeline
nexusPublishing {
    repositories {
        sonatype {
            username = System.getenv("SONATYPE_USERNAME")
            password = System.getenv("SONATYPE_PASSWORD")
        }
    }
}