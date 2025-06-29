// root build.gradle.kts
plugins {
    kotlin("jvm") version "1.9.20" apply false
    id("org.jetbrains.dokka") version "1.9.10" apply false
}

allprojects {
    group = "io.github.je1113"
    version = "1.0.0"
    repositories { mavenCentral() }
}

subprojects {
    // ── 플러그인 적용 ──────────────────────────
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.dokka")

    // ── Kotlin 컴파일 옵션 ─────────────────────
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
                "-Xjvm-default=all"
            )
        }
    }

    // ── Java 1.8 호환성 ────────────────────────
    extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // ── Kotlin Toolchain(JDK 8 바이트코드) ─────
    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(8)
    }

    // ── 테스트 ────────────────────────────────
    tasks.withType<Test> { useJUnitPlatform() }
}
