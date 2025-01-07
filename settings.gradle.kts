rootProject.name = "actorsK"
pluginManagement {
    plugins {
        kotlin("jvm") version "2.1.0"
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
include("core")
include("examples")
