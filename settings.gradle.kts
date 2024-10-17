plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("gradle/libraries.toml"))
        }
    }
}

rootProject.name = "networks"

include(
    "networks-domain",
    "networks-impl"
)