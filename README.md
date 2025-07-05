# Engineering Commerce Units (ECU)

🚀 **Java 8+ 호환 단위 변환 라이브러리**

[![Kotlin](https://img.shields.io/badge/kotlin-1.9.20-blue.svg)](http://kotlinlang.org)
[![Java](https://img.shields.io/badge/java-8+-orange.svg)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://jitpack.io/v/je1113/Engineering-Commerce-Units.svg)](https://jitpack.io/#je1113/Engineering-Commerce-Units)

ECU는 Java 8 이상을 지원하는 경량 단위 변환 라이브러리입니다. 레거시 Spring Boot 2.x 프로젝트부터 최신 환경까지 폭넓게 사용 가능합니다.

---

## 📦 설치 (via JitPack)

### 1. `settings.gradle.kts` 또는 `repositories {}`에 JitPack 추가

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
```

### 2. Gradle 의존성 설정

```kotlin
dependencies {
    implementation("com.github.je1113:Engineering-Commerce-Units:1.0.0")
}
```

> ❗️ `1.0.0`은 GitHub 릴리즈 태그 기준입니다. 최신 버전은 [여기에서 확인하세요](https://jitpack.io/#je1113/Engineering-Commerce-Units).

---

## 🏃 빠른 시작

<기존 예제들 유지 – Java & Kotlin 변환 예시, Batch, Commerce, Engineering 등>

---

## 🧱 프로젝트 구조

```
ecu/
├── ecu-core/          # 핵심 단위 변환 (Java 8+)
├── ecu-commerce/      # 상거래 특화 기능
├── ecu-engineering/   # 공학 단위 확장
├── examples/          # 사용 예제
├── docs/              # 기술 문서
└── benchmarks/        # 성능 테스트
```

---

## 📖 문서

- [JitPack 배포 페이지](https://jitpack.io/#je1113/Engineering-Commerce-Units)
- [마이그레이션 가이드](docs/migration-guide.md)
- [성능 분석](docs/performance.md)

---

## 🤝 기여하기

기여를 환영합니다!

### 기여 가이드라인

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Write tests for your changes
4. Ensure all tests pass (`./gradlew test`)
5. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
6. Push to the Branch (`git push origin feature/AmazingFeature`)
7. Open a Pull Request

### 코드 스타일
- Java: [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Kotlin: [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)

### 테스트 요구사항
- 단위 테스트 커버리지 80% 이상
- 통합 테스트 포함
- 성능 테스트 (선택사항)

---

## 📄 라이선스

MIT License — 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

**Made with ❤️ for Java developers**