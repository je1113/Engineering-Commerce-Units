// root build.gradle.kts
plugins {
    kotlin("jvm") version "1.9.20" apply false
    id("org.jetbrains.dokka") version "1.9.10" apply false
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

allprojects {
    group = "io.github.je1113"
    version = "1.0.0"
    repositories { mavenCentral() }
}

// Nexus Publishing 설정 (Root 프로젝트에만)
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("MAVEN_USERNAME"))
            password.set(System.getenv("MAVEN_PASSWORD"))
        }
    }
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

    // ── Java 및 Kotlin 호환성 설정 ────────────
    extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // ── Kotlin Toolchain 설정 ─────────────────
    // CI 환경에서는 현재 Java 버전 사용, 로컬에서는 Java 8 사용
    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        val isCI = System.getenv("CI") == "true"
        val javaVersion = System.getProperty("java.version")
        
        if (isCI) {
            // CI에서는 설정된 Java 버전을 그대로 사용 (툴체인 없이)
            println("CI environment detected, using current Java version: $javaVersion")
        } else {
            // 로컬에서만 Java 8 툴체인 사용
            jvmToolchain(8)
        }
    }

    // ── 테스트 ────────────────────────────────
    tasks.withType<Test> { useJUnitPlatform() }
}
