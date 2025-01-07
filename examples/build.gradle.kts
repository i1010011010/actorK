plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "io.wilski.actorsK"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(libs.bundles.coroutines)
    implementation(libs.logback.classic)
}

kotlin {
    jvmToolchain(21)
}