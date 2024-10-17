plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment = project.ext.has("development")
    
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

allprojects {
    repositories {
        mavenCentral()
    }
}