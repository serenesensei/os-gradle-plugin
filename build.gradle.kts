plugins {
    kotlin("jvm") version "1.4.31"
    `kotlin-dsl`
    signing
    id("com.gradle.plugin-publish") version "0.14.0"
    `java-gradle-plugin`
    `maven-publish`
}

description = "A Gradle plugin that detects the OS name and architecture, " +
        "providing a uniform classifier to be used in the names of " +
        "org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.native artifacts."
group = "org.mesleepy"
version = "1.0"

val isReleaseVersion by extra(!project.version.toString().endsWith("SNAPSHOT"))
val pluginId by extra("org.mesleepy.osdetector")

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation("io.kotest:kotest-runner-junit5:4.5.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
}

java {
    withSourcesJar()
    withJavadocJar()
}

// The Gradle plugin portal doesn't allow signature files.
if (gradle.startParameter.taskNames.intersect(listOf("publishPlugins")).isEmpty()) {
    signing {
        setRequired({
            isReleaseVersion
        })
        sign(configurations.archives.get())
    }
}

publishing {
    publications {
        create<MavenPublication>("osDetectorPlugin") {
            groupId = group.toString()
            artifactId = "osDetectorPlugin"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("${project.group}:${project.name}")
                description.set("${project.description}")
                url.set("https://github.com/me-sleepy/os-gradle-plugin")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    connection.set("scm:svn:https://github.com/me-sleepy/os-gradle-plugin.git")
                    developerConnection.set("scm:svn:git@github.com:me-sleepy/os-gradle-plugin.git")
                    url.set("https://github.com/me-sleepy/os-gradle-plugin")
                }
            }
        }
    }
    repositories {
        maven {
//            val releasesRepoUrl =
//                uri(layout.buildDirectory.dir("https://oss.sonatype.org/service/local/staging/deploy/maven2/"))
//            val snapshotsRepoUrl =
//                uri(layout.buildDirectory.dir("https://oss.sonatype.org/content/repositories/snapshots/"))
//            credentials(PasswordCredentials::class.java) {
//                username = rootProject.properties["ossrhusername"].toString()
//                password = rootProject.properties["ossrhpassword"].toString()
//            }
//            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

gradlePlugin {
    plugins {
        create("osDetectorPlugin") {
            id = pluginId
            implementationClass = "org.me-sleepy.gradle.osdetector.OsDetectorPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/me-sleepy/os-gradle-plugin"
    vcsUrl = "https://github.com/me-sleepy/os-gradle-plugin"
    description = "A Gradle plugin that detects the OS name and architecture, " +
            "providing a uniform classifier to be used in the names of native artifacts."

    plugins {
        getByName("osDetectorPlugin") {
            id = pluginId
            displayName = "OS name and architecture detector"
            tags = listOf("os", "osdetector", "arch", "classifier")
        }
    }
}
