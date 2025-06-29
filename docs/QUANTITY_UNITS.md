# ECU Quantity Units System

커머스 도메인을 위한 강력한 수량 단위 변환 시스템

## 목차
- [개요](#개요)
- [주요 기능](#주요-기능)
- [빠른 시작](#빠른-시작)
- [상세 사용법](#상세-사용법)
- [API 레퍼런스](#api-레퍼런스)
- [실제 사용 예제](#실제-사용-예제)

## 개요

ECU Quantity Units System은 SAP와 같은 엔터프라이즈 ERP 시스템에서 영감을 받아 설계된 유연한 수량 단위 변환 시스템입니다. 제품별로 다른 포장 단위와 환산 비율을 지원하며, 실제 상거래에서 필요한 다양한 기능을 제공합니다.

### 왜 필요한가?

일반적인 단위 변환 라이브러리는 물리적 단위(미터, 킬로그램 등)에 초점을 맞추지만, 실제 커머스에서는:
- 제품마다 다른 포장 단위 (A제품: 1박스=10개, B제품: 1박스=6개)
- 최소 주문 수량 및 포장 단위 제약
- B2B/B2C에 따른 다른 판매 단위
- 재고 관리를 위한 다단계 포장 계층

이러한 요구사항을 해결하기 위해 설계되었습니다.

## 주요 기능

### 1. 기본 수량 단위 지원
- **pieces** (개, pcs, ea)
- **dozen** (12개)
- **gross** (144개)
- **ream** (500개)
- **score** (20개)

### 2. 제품별 커스텀 단위 설정
```kotlin
// 제품별로 다른 박스 크기 정의
val productA = ProductUnitConfiguration.builder("PROD-A", "piece")
    .addConversion("box", 1.0, 10.0)    // 1 box = 10 pieces
    .addConversion("case", 1.0, 120.0)  // 1 case = 12 boxes
    .build()
```

### 3. 유연한 포장 계층 구조
```kotlin
// Each → Pack → Box → Case → Pallet → Container
val hierarchy = PackagingHierarchy(
    productId = "PROD-001",
    levels = listOf(
        PackagingLevel("piece", "piece", 1.0),
        PackagingLevel("pack", "6-pack", 6.0),
        PackagingLevel("box", "box", 24.0),
        PackagingLevel("pallet", "pallet", 2880.0)
    )
)
```

### 4. 라운딩 프로파일
```kotlin
// 도매용 라운딩 (최소 12개, 12개 단위)
val wholesale = RoundingProfile(
    minimumOrderQuantity = 12.0,
    packagingUnit = 12.0,
    roundingMode = RoundingMode.UP
)
```

## 빠른 시작

### 설치
```kotlin
dependencies {
    implementation("io.github.je1113:engineering-commerce-units:1.0.0")
}
```

### 기본 사용법
```kotlin
import io.ecu.*

// 1. 간단한 수량 변환
val qty = ECU.quantity("25 dozens")
println(qty.pieces)  // 300.0
println(qty.to("gross"))  // 2.08 gross

// 2. 박스 단위 변환
val items = ECU.quantity("100 pieces")
println(items.toBoxes(12))  // 8.33 box(12)
println(items.toBoxes(24))  // 4.17 box(24)

// 3. 산술 연산
val qty1 = Quantity.pieces(100.0)
val qty2 = Quantity.dozens(2.0)  // 24 pieces
val total = qty1 + qty2  // 124 pieces
```

## 상세 사용법

### 1. 제품별 단위 설정

실제 커머스에서는 제품마다 포장 규격이 다릅니다:

```kotlin
// 음료수: 6개들이 팩
val beverageConfig = ProductUnitConfiguration.builder("BEV-001", "piece")
    .addConversion("pack", 1.0, 6.0)
    .addConversion("box", 1.0, 24.0)  // 4 packs
    .build()

// 계란: 30개들이 판
val eggConfig = ProductUnitConfiguration.builder("EGG-001", "piece")
    .addConversion("tray", 1.0, 30.0)
    .addConversion("box", 1.0, 360.0)  // 12 trays
    .build()

// 서비스에 등록
val service = QuantityConversionService()
service.registerProduct(beverageConfig)
service.registerProduct(eggConfig)

// 사용
val order = ECU.quantity("5 pack")
val pieces = service.convert("BEV-001", order, "piece")  // 30 pieces
```

### 2. 포장 최적화

대량 주문 시 최적의 포장 조합을 제안합니다:

```kotlin
// 포장 계층 정의
val hierarchy = PackagingHierarchy(
    productId = "PROD-001",
    levels = listOf(
        PackagingLevel("piece", "piece", 1.0, 1.0),
        PackagingLevel("inner", "inner pack", 6.0, 1.0),
        PackagingLevel("box", "box", 24.0, 1.0),
        PackagingLevel("case", "case", 144.0, 1.0),
        PackagingLevel("pallet", "pallet", 2880.0, 0.5)
    )
)

service.registerPackagingHierarchy("PROD-001", hierarchy)

// 최적 포장 제안
val order = ECU.quantity("1500 pieces")
val suggestion = service.suggestOptimalPackaging("PROD-001", order)

// 결과: 10 cases + 2 boxes + 2 inner packs
suggestion.optimal?.components?.forEach { comp ->
    println("${comp.count} x ${comp.level.displayName}")
}
```

### 3. 재고 관리

재고 가용성 확인 및 대안 제시:

```kotlin
val available = ECU.quantity("500 pieces")
val requested = ECU.quantity("50 box")  // 50 x 12 = 600 pieces

val result = service.checkAvailability("PROD-001", requested, available)

if (!result.canFulfill) {
    println("재고 부족: ${result.shortage}")
    result.alternativeOptions.forEach { option ->
        println("대안: ${option.description}")
    }
}
```

### 4. 라운딩 규칙 적용

B2B 거래에서 필수적인 최소 주문 수량과 포장 단위:

```kotlin
// 도매 프로파일: 최소 12개, 12개 단위
val wholesaleProfile = RoundingProfile(
    minimumOrderQuantity = 12.0,
    packagingUnit = 12.0,
    roundingMode = RoundingMode.UP,
    allowFractional = false
)

// 7개 주문 → 12개로 조정 (최소 주문 수량)
// 15개 주문 → 24개로 조정 (포장 단위)
val adjusted = wholesaleProfile.applyRounding(15.0)  // 24.0
```

## API 레퍼런스

### Quantity 클래스

```kotlin
// 생성
Quantity.pieces(100.0)
Quantity.dozens(5.0)
Quantity.parse("25 gross")

// 변환
qty.to("dozen")
qty.toBoxes(12)
qty.toPallets(240)

// 산술 연산
qty1 + qty2
qty * 2.0
qty / 3.0

// 포맷팅
qty.withPrecision(2)
qty.withRounding(RoundingMode.UP)
```

### ProductUnitConfiguration

```kotlin
ProductUnitConfiguration.builder(productId, baseUnit)
    .addConversion(unit, fromValue, toValue)
    .addRoundingProfile(unit, profile)
    .build()
```

### QuantityConversionService

```kotlin
service.registerProduct(config)
service.registerPackagingHierarchy(productId, hierarchy)
service.convert(productId, quantity, targetUnit)
service.suggestOptimalPackaging(productId, quantity)
service.checkAvailability(productId, requested, available)
```

## 실제 사용 예제

### 온라인 식료품점
```kotlin
// 과일: kg 단위 판매, 박스 단위 재고
val appleConfig = ProductUnitConfiguration.builder("APPLE-FUJI", "kg")
    .addConversion("piece", 5.0, 1.0)  // 5 pieces ≈ 1kg
    .addConversion("box", 1.0, 10.0)   // 1 box = 10kg
    .build()

// 고객 주문: 3kg
val order = ECU.quantity("3 kg")
val pieces = service.convert("APPLE-FUJI", order, "piece")  // ≈15 pieces
```

### B2B 전자부품 유통
```kotlin
// 전자부품: 릴/트레이/박스 단위
val chipConfig = ProductUnitConfiguration.builder("IC-74HC00", "piece")
    .addConversion("reel", 1.0, 3000.0)    // 1 reel = 3000 pieces
    .addConversion("tray", 1.0, 100.0)     // 1 tray = 100 pieces
    .addConversion("tube", 1.0, 25.0)      // 1 tube = 25 pieces
    .build()
```

### 의류 도매
```kotlin
// 의류: 사이즈별 SKU, 묶음 판매
val shirtConfig = ProductUnitConfiguration.builder("SHIRT-L-BLUE", "piece")
    .addConversion("pack", 1.0, 3.0)      // 3개 묶음
    .addConversion("box", 1.0, 36.0)      // 12 packs per box
    .addRoundingProfile("pack", RoundingProfile(
        minimumOrderQuantity = 2.0,        // 최소 2팩
        packagingUnit = 1.0
    ))
    .build()
```

## 모범 사례

1. **제품 ID 체계 확립**: SKU나 ERP 코드와 일치하도록 설정
2. **기본 단위 일관성**: 가능한 가장 작은 판매 단위를 기본으로
3. **라운딩 규칙 명확히**: B2B/B2C에 따라 다른 프로파일 적용
4. **포장 계층 표준화**: 산업별 표준 계층 구조 정의

## 마이그레이션 가이드

기존 시스템에서 마이그레이션하는 경우:

```kotlin
// 기존 시스템 데이터
data class LegacyProduct(
    val sku: String,
    val unitsPerBox: Int,
    val minOrderQty: Int
)

// ECU로 변환
fun migrateLegacyProduct(legacy: LegacyProduct): ProductUnitConfiguration {
    return ProductUnitConfiguration.builder(legacy.sku, "piece")
        .addConversion("box", 1.0, legacy.unitsPerBox.toDouble())
        .addRoundingProfile("piece", RoundingProfile(
            minimumOrderQuantity = legacy.minOrderQty.toDouble()
        ))
        .build()
}
```

## 성능 고려사항

- 제품 설정은 애플리케이션 시작 시 한 번만 로드
- 변환 계산은 O(1) 시간 복잡도
- 대량 변환 시 배치 처리 API 활용

## 향후 계획

- [ ] 국제 표준 포장 단위 (ISO)
- [ ] 산업별 프리셋 제공
- [ ] 변환 이력 추적
- [ ] 가격 단위 통합
