plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.github.klaidoshka.networks"
version = "1.0"

dependencies {
    implementation(project(":networks-domain"))
}