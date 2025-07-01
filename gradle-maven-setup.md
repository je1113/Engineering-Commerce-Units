# Maven Central 배포 설정 가이드

## 1. gradle.properties 설정

프로젝트 루트에 `gradle.properties` 파일 생성:

```properties
# Maven Central 배포 설정
signing.keyId=YOUR_GPG_KEY_ID_LAST_8_CHARS
signing.password=YOUR_GPG_KEY_PASSPHRASE
signing.secretKeyRingFile=/path/to/.gnupg/secring.gpg

# Sonatype 자격증명
ossrhUsername=YOUR_SONATYPE_USERNAME
ossrhPassword=YOUR_SONATYPE_PASSWORD

# 버전 설정
version=1.0.0
```

## 2. 루트 build.gradle.kts 수정

```kotlin
plugins {
    kotlin("jvm") version "1.9.20" apply false
    id("org.jetbrains.dokka") version "1.9.10" apply false
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

allprojects {
    group = "io.github.je1113"
    version = project.findProperty("version") as String? ?: "1.0.0-SNAPSHOT"
    repositories { 
        mavenCentral() 
    }
}

// Nexus 게시 설정
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(project.findProperty("ossrhUsername") as String? ?: "")
            password.set(project.findProperty("ossrhPassword") as String? ?: "")
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.dokka")

    // 기존 설정들...

    // ── Java/Kotlin 소스 및 문서 JAR 생성 ────────
    tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    tasks.register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from(tasks["dokkaJavadoc"])
    }

    // ── 게시 설정 ──────────────────────────────
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])
                
                pom {
                    name.set(project.name)
                    description.set("Engineering Commerce Units - ${project.name}")
                    url.set("https://github.com/je1113/engineering-commerce-units")
                    
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    
                    developers {
                        developer {
                            id.set("je1113")
                            name.set("Jeon Jooeun")
                            email.set("jje320594@gmail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:https://github.com/je1113/engineering-commerce-units.git")
                        developerConnection.set("scm:git:git@github.com:je1113/engineering-commerce-units.git")
                        url.set("https://github.com/je1113/engineering-commerce-units")
                    }
                }
            }
        }
    }

    // ── 서명 설정 ──────────────────────────────
    signing {
        sign(publishing.publications["maven"])
    }
    
    // 서명은 릴리즈 버전에서만 활성화
    tasks.withType<Sign>().configureEach {
        onlyIf { !version.toString().endsWith("SNAPSHOT") }
    }
}
```

## 3. 배포 명령어

### 스냅샷 배포 (개발 버전)
```bash
# 버전을 SNAPSHOT으로 설정
./gradlew publishToSonatype

# 또는 특정 모듈만
./gradlew :ecu-core:publishToSonatype
./gradlew :ecu-commerce:publishToSonatype
```

### 릴리즈 배포 (정식 버전)
```bash
# 1. 버전을 정식 버전으로 변경 (gradle.properties에서 version=1.0.0)
# 2. 빌드 및 테스트
./gradlew clean build test

# 3. 스테이징에 게시
./gradlew publishToSonatype

# 4. 스테이징 저장소 닫기 및 릴리즈
./gradlew closeSonatypeStagingRepository
./gradlew releaseSonatypeStagingRepository

# 또는 한 번에
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
```

## 4. 체크리스트

### 배포 전 확인사항
- [ ] 모든 테스트 통과
- [ ] README.md 업데이트
- [ ] CHANGELOG.md 작성
- [ ] 버전 번호 확정
- [ ] GPG 키 설정 완료
- [ ] Sonatype 계정 승인 완료

### 첫 배포 단계
1. [ ] JIRA 티켓 승인 대기 (1-2일)
2. [ ] 스냅샷 버전으로 테스트 배포
3. [ ] 정식 버전 배포
4. [ ] Maven Central 동기화 확인 (2-4시간)

## 5. 사용자 가이드 작성

배포 완료 후 README.md에 추가할 사용법:

```markdown
## Installation

### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("io.github.je1113:ecu-core:1.0.0")
    implementation("io.github.je1113:ecu-commerce:1.0.0")
}
```

### Gradle (Groovy)
```groovy
dependencies {
    implementation 'io.github.je1113:ecu-core:1.0.0'
    implementation 'io.github.je1113:ecu-commerce:1.0.0'
}
```

### Maven
```xml
<dependency>
    <groupId>io.github.je1113</groupId>
    <artifactId>ecu-core</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>io.github.je1113</groupId>
    <artifactId>ecu-commerce</artifactId>
    <version>1.0.0</version>
</dependency>
```
```

## 6. 배포 후 확인

- Maven Central 검색: https://search.maven.org/
- 라이브러리 페이지: https://central.sonatype.com/artifact/io.github.je1113/ecu-core
- 문서 사이트: GitHub Pages 또는 Dokka 호스팅 고려
