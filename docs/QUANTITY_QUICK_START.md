# 🚀 ECU Quantity Units - Quick Start

커머스를 위한 수량 단위 변환 시스템을 5분 안에 시작하세요!

## 🎯 핵심 개념

ECU Quantity는 **제품별로 다른 포장 단위**를 관리합니다:
- 🥤 음료수 A: 1박스 = 6개
- 🥚 계란: 1판 = 30개  
- 🔩 나사: 1박스 = 1,000개

## 📦 설치

```kotlin
dependencies {
    implementation("io.github.parkyoungmin:engineering-commerce-units:1.0.0")
}
```

## 🏃 빠른 시작

### 1️⃣ 기본 사용법
```kotlin
import io.ecu.*

// 수량 생성
val qty = ECU.quantity("25 dozens")  // 300개
val boxes = qty.toBoxes(12)          // 25 박스 (12개들이)

// 산술 연산
val total = Quantity.pieces(100.0) + Quantity.dozens(2.0)  // 124개
```

### 2️⃣ 제품별 설정
```kotlin
// 제품 설정
val product = ProductUnitConfiguration.builder("PROD-001", "piece")
    .addConversion("box", 1.0, 10.0)    // 1박스 = 10개
    .addConversion("pallet", 1.0, 240.0) // 1팔레트 = 240개
    .build()

// 서비스 사용
val service = QuantityConversionService()
service.registerProduct(product)

val order = ECU.quantity("5 box")
val pieces = service.convert("PROD-001", order, "piece")  // 50개
```

### 3️⃣ 실전 예제: 온라인 상점
```kotlin
// 🍺 맥주 (6캔 팩)
val beerConfig = ProductUnitConfiguration.builder("BEER-001", "can")
    .addConversion("pack", 1.0, 6.0)
    .addConversion("box", 1.0, 24.0)  // 4팩
    .build()

// 🛒 주문 처리
val customerOrder = ECU.quantity("3 pack")  // 고객이 3팩 주문
val cans = service.convert("BEER-001", customerOrder, "can")  // 18캔

// 📦 재고 확인
val stock = ECU.quantity("10 box")  // 재고: 10박스
val available = service.checkAvailability("BEER-001", customerOrder, stock)
println("주문 가능: ${available.canFulfill}")  // true
```

## 💡 주요 기능

### ✅ 다양한 수량 단위
- `pieces`, `dozens` (12), `gross` (144), `ream` (500)
- 커스텀 단위: `box`, `pack`, `pallet`, `container`

### ✅ 스마트 변환
```kotlin
val qty = ECU.quantity("1250 pieces")
qty.toBoxes(12)     // 104.17 boxes
qty.toPallets(240)  // 5.21 pallets
```

### ✅ 최소 주문 수량
```kotlin
val wholesale = RoundingProfile(
    minimumOrderQuantity = 12.0,
    packagingUnit = 12.0,
    roundingMode = RoundingMode.UP
)
// 7개 주문 → 12개로 조정
```

### ✅ 포장 최적화
```kotlin
val qty = ECU.quantity("1500 pieces")
val optimal = service.suggestOptimalPackaging("PROD-001", qty)
// 결과: 6 boxes + 2 packs + 3 pieces
```

## 📖 다음 단계

- 📚 [전체 문서](QUANTITY_UNITS.md)
- 💼 [실전 예제](../src/main/kotlin/io/ecu/examples/QuantityUsageGuide.kt)
- 🧪 [테스트 코드](src/test/kotlin/io/ecu/QuantityTest.kt)

## 🤔 FAQ

**Q: 일반 단위 변환과 뭐가 다른가요?**  
A: 제품별로 다른 포장 단위를 지원합니다. A제품 1박스=10개, B제품 1박스=6개 처럼요.

**Q: 어떤 업종에 적합한가요?**  
A: 이커머스, 도매/소매, 제조업, 물류 등 수량 관리가 필요한 모든 분야

**Q: 기존 시스템과 통합 가능한가요?**  
A: 네! ProductUnitConfiguration으로 기존 제품 데이터를 쉽게 연결할 수 있습니다.

---
🎉 **시작할 준비가 되셨나요?** 위 예제를 복사해서 바로 사용해보세요!
