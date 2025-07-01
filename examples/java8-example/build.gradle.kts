plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":ecu-core"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClass.set("io.ecu.examples.ShippingExample")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
