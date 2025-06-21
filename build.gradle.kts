plugins {
    kotlin("multiplatform") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
    id("maven-publish")
    id("signing")
}

group = "io.github.parkyoungmin"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
    
    js(IR) {
        moduleName = "ecu"
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        nodejs()
        binaries.executable()
    }
    
    // Native targets for future expansion
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.kotest:kotest-framework-engine:5.7.2")
                implementation("io.kotest:kotest-assertions-core:5.7.2")
                implementation("io.kotest:kotest-property:5.7.2")
            }
        }
        
        val jvmMain by getting {
            dependencies {
                implementation("org.slf4j:slf4j-api:2.0.9")
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5:5.7.2")
                implementation("org.slf4j:slf4j-simple:2.0.9")
            }
        }
        
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-nodejs:0.0.7")
            }
        }
        
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        
        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        
        val macosX64Main by getting {
            dependsOn(nativeMain)
        }
        
        val macosArm64Main by getting {
            dependsOn(nativeMain)
        }
        
        val mingwX64Main by getting {
            dependsOn(nativeMain)
        }
    }
}

publishing {
    publications {
        publications.withType<MavenPublication> {
            pom {
                name.set("Engineering Commerce Units")
                description.set("A comprehensive unit conversion library for commerce and engineering domains")
                url.set("https://github.com/parkyoungmin/engineering-commerce-units")
                
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                
                developers {
                    developer {
                        id.set("parkyoungmin")
                        name.set("Young Min Park")
                        email.set("parkyoungmin@example.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/parkyoungmin/engineering-commerce-units.git")
                    developerConnection.set("scm:git:ssh://github.com/parkyoungmin/engineering-commerce-units.git")
                    url.set("https://github.com/parkyoungmin/engineering-commerce-units/tree/main")
                }
            }
        }
    }
}

tasks.named("compileKotlinJvm") {
    dependsOn("compileJava")
}
