# Engineering Commerce Units (ECU)

🚀 **Java 8+ 호환 단위 변환 라이브러리**

[![Kotlin](https://img.shields.io/badge/kotlin-1.9.20-blue.svg)](http://kotlinlang.org)
[![Java](https://img.shields.io/badge/java-8+-orange.svg)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.je1113/ecu-core.svg)](https://search.maven.org/artifact/io.github.je1113/ecu-core)

ECU는 Java 8 이상을 지원하는 경량 단위 변환 라이브러리입니다. 레거시 Spring Boot 2.x 프로젝트부터 최신 환경까지 폭넓게 사용 가능합니다.

## ✨ 주요 특징

- ☕ **Java 8+ 완벽 호환**: 레거시 환경에서도 사용 가능
- 🔄 **간편한 단위 변환**: 길이, 무게, 부피, 온도
- 🎯 **타입 안전**: 컴파일 타임 단위 검증
- 🚀 **경량**: 외부 의존성 없음
- 📦 **모듈화**: 필요한 기능만 선택적 사용

## 📥 설치

### Maven
```xml
<dependency>
    <groupId>io.github.je1113</groupId>
    <artifactId>ecu-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```kotlin
dependencies {
    implementation("io.github.je1113:ecu-core:1.0.0")
}
```

## 🏃 빠른 시작

### Java 사용 예제
```java
import io.ecu.ECU;
import io.ecu.Length;
import io.ecu.Weight;

// 길이 변환
Length length = ECU.length("100 cm");
System.out.println(length.to("m"));    // "1.0 m"
System.out.println(length.to("in"));   // "39.37 in"

// 무게 변환
Weight weight = ECU.weight(2.5, "kg");
System.out.println(weight.to("lb"));   // "5.51 lb"

// getter 메소드 사용
double meters = length.getMeters();   // 1.0
double pounds = weight.getPounds();   // 5.51156
```

### Kotlin 사용 예제
```kotlin
import io.ecu.*

// 더 간결한 Kotlin 스타일
val length = ECU.length("100 cm")
println(length.to("m"))      // "1.0 m"
println(length.meters)       // 1.0 (프로퍼티 접근)

// 연산자 오버로딩
val total = length + ECU.length("50 cm")
val double = length * 2.0
```

## 📚 지원 단위

### 길이 (Length)
- **미터법**: m, km, cm, mm
- **야드파운드법**: ft, in, yd, mi

### 무게 (Weight)  
- **미터법**: kg, g, mg, t
- **야드파운드법**: lb, oz

### 부피 (Volume)
- **미터법**: l, ml, m³
- **야드파운드법**: gal, qt, pt, fl oz

### 온도 (Temperature)
- **섭씨**: °C
- **화씨**: °F
- **켈빈**: K

## 🎯 실제 사용 사례

### 배송 시스템
```java
// 박스 크기 계산
Length boxLength = ECU.length("60 cm");
Length boxWidth = ECU.length("40 cm");
Length boxHeight = ECU.length("30 cm");

// 국제 배송을 위한 인치 변환
System.out.println("Dimensions: " + 
    boxLength.to("in") + " x " + 
    boxWidth.to("in") + " x " + 
    boxHeight.to("in"));

// 무게 제한 확인
Weight packageWeight = ECU.weight("25 kg");
Weight limit = ECU.weight("50 lb");

if (packageWeight.getKilograms() > limit.getKilograms()) {
    System.out.println("Package exceeds weight limit!");
}
```

### 배치 변환
```java
List<String> weights = Arrays.asList("500 g", "2.5 kg", "750 g");
List<Weight> inKilograms = ECU.Batch.convertWeights(weights, "kg");

// 총 무게 계산
double total = inKilograms.stream()
    .mapToDouble(Weight::getKilograms)
    .sum();
```

## 🛠️ 고급 기능

### 정밀도 제어
```java
Length precise = ECU.length("100.12345 m")
    .withPrecision(2)
    .to("ft");  // "328.48 ft"
```

### 단위 정보 조회
```java
// 지원되는 단위 확인
Set<String> lengthUnits = ECU.Info.getSupportedLengthUnits();
// [m, km, cm, mm, ft, in, yd, mi]

// 단위 유효성 검증
boolean valid = ECU.Info.isValidUnit("kg");  // true
boolean invalid = ECU.Info.isValidUnit("xyz"); // false
```

## 🏗️ 프로젝트 구조

```
ecu-core/          # 핵심 단위 변환 (Java 8+)
ecu-commerce/      # 상거래 특화 기능 (개발 예정)
ecu-engineering/   # 공학 단위 확장 (개발 예정)
examples/          # 사용 예제
```

## 🔄 마이그레이션 가이드

### Spring Boot 2.x (Java 8)
```java
@Service
public class ShippingService {
    public ShippingCost calculateCost(String weight, String distance) {
        Weight w = ECU.weight(weight);
        Length d = ECU.length(distance);
        
        // 표준 단위로 변환 후 계산
        double kg = w.getKilograms();
        double km = d.getKilometers();
        
        return calculateRate(kg, km);
    }
}
```

## 📖 문서

- [API 문서](https://javadoc.io/doc/io.github.je1113/ecu-core)
- [예제 코드](examples/java8-example)
- [변경 로그](CHANGELOG.md)

## 🤝 기여하기

기여를 환영합니다! 

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

MIT License - 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 🙏 감사의 말

이 프로젝트는 다음 프로젝트들에서 영감을 받았습니다:
- [Units of Measurement (JSR 385)](https://github.com/unitsofmeasurement/unit-api)
- [Javax Measure](https://github.com/unitsofmeasurement/uom-se)

---

**Made with ❤️ for Java developers**
