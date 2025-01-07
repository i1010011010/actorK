plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "io.wilski.actorsK"
version = "0.1.0"

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