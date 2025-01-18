plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "io.wilski.actorsK"
version = libs.versions.project

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(libs.actorsK.core)
    implementation(libs.bundles.coroutines)
    implementation(libs.logback.classic)
}

kotlin {
    jvmToolchain(21)
}