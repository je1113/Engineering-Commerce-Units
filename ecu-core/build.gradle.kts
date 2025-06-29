import java.net.URL
plugins {
    kotlin("jvm")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka")
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
            
            // 소스 링크 설정
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(URL("https://github.com/parkyoungmin/engineering-commerce-units/tree/main/ecu-core/src/main/kotlin"))
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
            // "-Xexplicit-api=strict" 제거 - 너무 엄격함
        )
    }
}

// JAR 매니페스트 설정
tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "ECU Core Library",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "Park Young Min",
            "Created-By" to "Gradle ${gradle.gradleVersion}",
            "Built-JDK" to System.getProperty("java.version"),
            "Target-JDK" to "1.8"
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            artifact(dokkaJavadocJar)
            artifact(sourcesJar)
            
            pom {
                name.set("ECU Core Library")
                description.set("A lightweight, Java 8 compatible unit conversion library")
                url.set("https://github.com/parkyoungmin/engineering-commerce-units")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("parkyoungmin")
                        name.set("Park Young Min")
                        email.set("your.email@example.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/parkyoungmin/engineering-commerce-units.git")
                    developerConnection.set("scm:git:ssh://github.com/parkyoungmin/engineering-commerce-units.git")
                    url.set("https://github.com/parkyoungmin/engineering-commerce-units")
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                password = project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["maven"])
    }
}
