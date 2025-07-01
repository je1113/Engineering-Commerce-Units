rootProject.name = "Engineering-Commerce-Units"

include(
    ":ecu-core",
    ":ecu-commerce",
    ":ecu-engineering",
    ":examples:java8-example"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}