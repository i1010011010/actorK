plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-library")
    id("maven-publish")
}

group = "io.wilski.actorsK"
version = libs.versions.project

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.coroutines)
    implementation(libs.logback.classic)
}

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()
}

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
            "Implementation-Version" to project.version))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = "core"
            version = version.toString()
            from(components["kotlin"])
        }
    }
}