plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(project(":ecu-core"))
    
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "ECU Engineering Module",
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
            
            pom {
                name.set("ECU Engineering Module")
                description.set("Engineering-specific extensions for ECU Core library")
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
}
