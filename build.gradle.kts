// build.gradle.kts (루트) - JitPack용으로 단순화
plugins {
    kotlin("jvm") version "1.9.20" apply false
}

allprojects {
    group = "com.github.je1113" // JitPack용 그룹 ID
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}
