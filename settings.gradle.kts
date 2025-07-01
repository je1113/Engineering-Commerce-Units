pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "Engineering-Commerce-Units"

include(
    ":ecu-core",
    ":ecu-commerce",
    ":ecu-engineering",
    ":examples:java8-example"
)
