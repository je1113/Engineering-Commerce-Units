# Engineering Commerce Units (ECU)

🚀 **커머스와 엔지니어링을 위한 강력한 단위 변환 라이브러리**

[![Kotlin](https://img.shields.io/badge/kotlin-1.9.20-blue.svg)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

ECU는 물리적 단위 변환과 커머스 특화 수량 단위 시스템을 제공하는 Kotlin 멀티플랫폼 라이브러리입니다.

## ✨ 주요 특징

- 🔄 **포괄적인 단위 변환**: 길이, 무게, 부피, 온도, 압력 등
- 📦 **커머스 특화 수량 단위**: 제품별 포장 단위, 최소 주문 수량
- 🌍 **멀티플랫폼 지원**: JVM, JS, Native
- 🎯 **타입 안전**: Kotlin의 강력한 타입 시스템 활용
- 🚀 **고성능**: 효율적인 변환 알고리즘

## 📥 설치

```kotlin
dependencies {
    implementation("io.github.parkyoungmin:engineering-commerce-units:1.0.0")
}
```

## 🏃 빠른 시작

### 물리적 단위 변환
```kotlin
import io.ecu.*

// 길이
val length = ECU.length("100 meters")
println(length.to("feet"))  // 328.08 ft

// 무게
val weight = ECU.weight("5 kg")
println(weight.to("lb"))    // 11.02 lb

// 온도
val temp = ECU.temperature("25 °C")
println(temp.to("°F"))      // 77.0 °F
```

### 🆕 커머스 수량 단위
```kotlin
// 기본 수량 변환
val qty = ECU.quantity("25 dozens")
println(qty.pieces)  // 300.0

// 제품별 포장 단위
val product = ProductUnitConfiguration.builder("PROD-001", "piece")
    .addConversion("box", 1.0, 12.0)    // 1박스 = 12개
    .addConversion("pallet", 1.0, 288.0) // 1팔레트 = 288개
    .build()

val service = QuantityConversionService()
service.registerProduct(product)

val order = ECU.quantity("5 box")
val pieces = service.convert("PROD-001", order, "piece")  // 60개
```

## 📚 지원 단위

### 물리적 단위
- **길이**: m, km, cm, mm, ft, in, yd, mi 등
- **무게**: kg, g, mg, lb, oz, t 등
- **부피**: L, mL, gal, fl oz, m³ 등
- **온도**: °C, °F, K
- **압력**: Pa, bar, psi, atm 등
- **속도**: m/s, km/h, mph, knot 등
- **에너지**: J, kJ, cal, kWh 등

### 수량 단위 (커머스 특화)
- **기본**: piece, dozen(12), gross(144), ream(500)
- **커스텀**: box, pack, case, pallet, container
- **제품별 설정**: 제품마다 다른 환산 비율
- **라운딩 규칙**: 최소 주문 수량, 포장 단위

## 🎯 사용 사례

### 이커머스 플랫폼
```kotlin
// 과일: kg 단위 판매, 박스 단위 재고
val appleConfig = ProductUnitConfiguration.builder("APPLE", "kg")
    .addConversion("box", 1.0, 10.0)  // 1박스 = 10kg
    .build()
```

### B2B 도매
```kotlin
// 최소 주문 수량 적용
val wholesale = RoundingProfile(
    minimumOrderQuantity = 100.0,
    packagingUnit = 12.0
)
```

### 국제 무역
```kotlin
// 미터법 ↔ 야드파운드법
val weight = ECU.weight("1000 kg")
val lbs = weight.to("lb")  // 2204.62 lb

// 컨테이너 적재 계산
val pallets = ECU.quantity("48 pallet")
val containers = pallets.toContainers(20)  // 20ft 컨테이너
```

## 🛠️ 고급 기능

### 정밀도 제어
```kotlin
val length = ECU.length("100.12345 m")
    .withPrecision(2)
    .to("ft")  // 328.48 ft
```

### 자동 단위 제안
```kotlin
val large = ECU.length("5000 m")
val suggestion = ECU.Auto.suggest("5000 m")
// "Consider using kilometers: 5 km"
```

### 배치 변환
```kotlin
val lengths = listOf("10 m", "20 ft", "5 km")
val results = ECU.Batch.convertLengths(lengths, "m")
```

## 📖 문서

- [수량 단위 가이드](docs/QUANTITY_UNITS.md)
- [빠른 시작 가이드](docs/QUANTITY_QUICK_START.md)
- [API 레퍼런스](docs/API.md)
- [예제 코드](src/main/kotlin/io/ecu/examples/)

## 🤝 기여하기

기여를 환영합니다! PR을 보내주시거나 이슈를 등록해주세요.

## 📄 라이선스

Apache License 2.0 - 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 🙏 감사의 말

이 프로젝트는 SAP와 같은 엔터프라이즈 ERP 시스템의 단위 관리 방식에서 영감을 받았습니다.

---

**Made with ❤️ for commerce and engineering**
