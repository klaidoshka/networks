plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.github.klaidoshka.networks"
version = "1.0"

dependencies {
    implementationExposed(libs.apache.commons)
    implementationExposed(libs.guava)
    implementationExposed(libs.koin)
    implementationExposed(libs.kotlin.stdlib)
    implementationExposed(libs.ktor.server.core)
    implementationExposed(libs.ktor.server.netty)
    implementationExposed(libs.logback.classic)
    implementationExposed(libs.neo4j.driver)
    implementationExposed(libs.typesafe.config)
}

kotlin {
    jvmToolchain(21)
}

fun DependencyHandler.implementationExposed(any: Any) {
    api(any)
    implementation(any)
}