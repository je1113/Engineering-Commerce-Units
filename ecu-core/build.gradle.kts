import java.net.URL

plugins {
    kotlin("jvm")
    java
}

group = "com.github.je1113" // GitHub 사용자명
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8)) // ✅ JDK 8 타겟으로 변경
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
