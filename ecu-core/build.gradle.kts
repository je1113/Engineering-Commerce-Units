import java.net.URL

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka")
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
    
    dokkaSourceSets {
        configureEach {
            jdkVersion.set(8)
            platform.set(org.jetbrains.dokka.Platform.jvm)
            
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(URL("https://github.com/je1113/Engineering-Commerce-Units/tree/main/ecu-core/src/main/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}

// Javadoc JAR 생성
val dokkaJavadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

// Sources JAR 생성
val sourcesJar by tasks.registering(Jar::class) {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

// Java 친화적인 코드 생성을 위한 설정
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
            "-Xjvm-default=all"
        )
    }
}

// JAR 매니페스트 설정
tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "ECU Core Library",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "Jeon Joo Eun",
            "Created-By" to "Gradle ${gradle.gradleVersion}",
            "Built-JDK" to System.getProperty("java.version"),
            "Target-JDK" to "1.8"
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.je1113"
            artifactId = "ecu-core"
            version = project.version.toString()

            from(components["java"])
            
            artifact(dokkaJavadocJar)
            artifact(sourcesJar)
            
            pom {
                name.set("ECU Core Library")
                description.set("A lightweight, Java 8 compatible unit conversion library for length, weight, volume, and temperature conversions")
                url.set("https://github.com/je1113/Engineering-Commerce-Units")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("je1113")
                        name.set("Jeon Joo Eun")
                        email.set("jje320594@gmail.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/je1113/Engineering-Commerce-Units.git")
                    developerConnection.set("scm:git:ssh://github.com/je1113/Engineering-Commerce-Units.git")
                    url.set("https://github.com/je1113/Engineering-Commerce-Units")
                }
            }
        }
    }
}

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

signing {
    val signingKey = System.getenv("GPG_SIGNING_KEY")
    val signingPassword = System.getenv("GPG_SIGNING_PASSWORD")
    
    if (!signingKey.isNullOrEmpty() && !signingPassword.isNullOrEmpty()) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["maven"])
    }
}
