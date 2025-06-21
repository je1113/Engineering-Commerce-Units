plugins {
    kotlin("jvm") version "1.9.20"
    id("maven-publish")
}

group = "io.github.parkyoungmin"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // 테스트 의존성
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            
            from(components["java"])
            
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
