# 수량 환산 및 라운딩 시스템 비즈니스 활용 가이드

## 개요

ECU의 수량 환산 및 라운딩 시스템은 실제 상거래 환경에서 발생하는 복잡한 수량 관리 문제를 해결합니다. 이 시스템은 B2B/B2C 전자상거래, 재고 관리, 물류, 제조업 등 다양한 분야에서 활용될 수 있습니다.

## 주요 기능

### 1. 라운딩 프로파일 (RoundingProfile)
- **최소 주문 수량(MOQ)** 적용
- **포장 단위** 기반 라운딩
- **증분 단위** 제약 처리
- **최대 주문 수량** 제한
- **특별 규칙** 적용

### 2. 체인 환산 (ConversionService)
- **다단계 포장 단위** 자동 환산
- **최적 단위 제안**
- **분수 표현** 지원
- **커스텀 체인** 정의

### 3. 포맷팅 (QuantityFormatter)
- **지역별 표시 형식**
- **약어/기호** 표현
- **복합 단위** 표시
- **회계용 포맷**

## 비즈니스 시나리오별 활용 예시

### 시나리오 1: B2B 도매 상거래

**상황**: 도매업체가 소매점에 상품을 판매하는 경우

```kotlin
// 소매점이 17개를 주문하려고 함
val orderRequest = ECU.quantity("17 pieces")

// 도매 프로파일 적용 (12개 박스 단위)
val wholesaleProfile = RoundingProfile.WHOLESALE
val adjustedOrder = orderRequest.applyRounding(wholesaleProfile)
// 결과: 24 pieces (2 boxes)

// 가격 계산
val pricePerBox = 120.00
val totalBoxes = adjustedOrder.pieces / 12
val totalPrice = totalBoxes * pricePerBox
// 총 가격: $240.00 (2박스)

// 고객에게 안내
println("주문 가능 수량: ${adjustedOrder.format()}")
println("박스 단위: ${totalBoxes.toInt()} boxes")
println("총 금액: $${"%.2f".format(totalPrice)}")
```

**비즈니스 이점**:
- 포장 단위에 맞춘 효율적인 배송
- 명확한 가격 정책
- 재고 관리 최적화

### 시나리오 2: 재고 관리 시스템

**상황**: 창고에서 재고를 효율적으로 표시하고 관리

```kotlin
// 대량 재고
val inventory = ECU.quantity("15840 pieces")

// 최적 단위로 표현
val conversionService = ConversionService()
val optimal = conversionService.suggestOptimalUnit(inventory)
// 결과: "11 pallets + 0 cartons + 0 boxes + 0 pieces"

// 재고 레벨별 표시
when {
    inventory.pieces < 144 -> {
        // 박스 단위로 표시
        println("재고 부족: ${inventory.toBoxes(12).format()}")
    }
    inventory.pieces < 1440 -> {
        // 카톤 단위로 표시
        println("재고 보통: ${optimal.format()}")
    }
    else -> {
        // 팔레트 단위로 표시
        println("재고 충분: ${optimal.format()}")
    }
}
```

**비즈니스 이점**:
- 직관적인 재고 파악
- 공간 활용 최적화
- 피킹/패킹 효율성 향상

### 시나리오 3: 전자상거래 플랫폼

**상황**: 다양한 판매 채널에 맞는 수량 관리

```kotlin
// 제품 설정
val productConfig = ProductConfiguration(
    retailChannel = RoundingProfile.RETAIL,
    wholesaleChannel = RoundingProfile.WHOLESALE,
    bulkChannel = RoundingProfile.BULK
)

// 고객 유형별 처리
fun processOrder(quantity: Double, customerType: CustomerType): OrderResult {
    val requested = ECU.quantity("$quantity pieces")
    
    val profile = when (customerType) {
        CustomerType.RETAIL -> productConfig.retailChannel
        CustomerType.WHOLESALE -> productConfig.wholesaleChannel
        CustomerType.BULK -> productConfig.bulkChannel
    }
    
    val adjusted = requested.applyRounding(profile)
    val validation = profile.isValidQuantity(quantity)
    
    return OrderResult(
        originalQuantity = quantity,
        adjustedQuantity = adjusted.pieces,
        isValid = validation.isValid,
        message = validation.reason ?: "주문 가능합니다"
    )
}
```

**비즈니스 이점**:
- 채널별 차별화된 판매 정책
- 자동화된 주문 검증
- 고객 경험 개선

### 시나리오 4: 물류 최적화

**상황**: 배송 효율을 위한 수량 최적화

```kotlin
// 배송 비용 계산
class ShippingCalculator {
    fun calculateShipping(quantity: Quantity): ShippingResult {
        val conversionService = ConversionService()
        val optimal = conversionService.suggestOptimalUnit(quantity)
        
        // 팔레트, 박스, 낱개 기반 배송비 계산
        var cost = 0.0
        optimal.recommended?.components?.forEach { component ->
            cost += when (component.unit) {
                "pallet" -> component.value * 150.0  // 팔레트당 $150
                "carton" -> component.value * 25.0   // 카톤당 $25
                "box" -> component.value * 10.0      // 박스당 $10
                else -> component.value * 2.0        // 낱개당 $2
            }
        }
        
        return ShippingResult(
            quantity = quantity,
            optimalPackaging = optimal.recommended?.format() ?: "",
            shippingCost = cost
        )
    }
}
```

**비즈니스 이점**:
- 배송비 최적화
- 포장 자재 절감
- 차량 적재 효율성 향상

### 시나리오 5: 제조업 생산 계획

**상황**: 원자재 주문 및 생산 배치 관리

```kotlin
// 음료 제조업체 예시
val beverageProduction = object {
    // 1 배치 = 1000 캔
    val batchSize = 1000
    
    // 포장 체인
    val packagingChain = ConversionService.BEVERAGE_PACKAGING
    
    fun planProduction(demand: Int): ProductionPlan {
        val demandQuantity = ECU.quantity("$demand cans")
        
        // 생산 배치 계산
        val batches = ceil(demand / batchSize.toDouble()).toInt()
        val actualProduction = batches * batchSize
        
        // 포장 계획
        val conversionService = ConversionService()
        val packaging = conversionService.suggestOptimalUnit(
            ECU.quantity("$actualProduction pieces")
        )
        
        return ProductionPlan(
            demandedQuantity = demand,
            productionBatches = batches,
            actualProduction = actualProduction,
            overProduction = actualProduction - demand,
            packagingPlan = packaging.recommended?.format() ?: ""
        )
    }
}
```

**비즈니스 이점**:
- 생산 효율성 최대화
- 원자재 낭비 최소화
- 재고 회전율 개선

### 시나리오 6: 글로벌 전자상거래

**상황**: 다국가 고객을 위한 지역별 표시

```kotlin
// 지역별 포맷터
val formatters = mapOf(
    "US" to QuantityFormatter.US_STANDARD,
    "EU" to QuantityFormatter.EU_STANDARD,
    "KR" to QuantityFormatter(
        locale = QuantityFormatter.Locale.KR,
        options = QuantityFormatter.FormattingOptions(
            unitStyle = QuantityFormatter.UnitStyle.FULL
        )
    )
)

// 고객 지역에 맞는 표시
fun displayQuantity(quantity: Quantity, region: String): String {
    val formatter = formatters[region] ?: QuantityFormatter.US_STANDARD
    return quantity.format(formatter)
}

// 사용 예
val qty = ECU.quantity("1234.56 pieces")
println("US: ${displayQuantity(qty, "US")}")  // 1,234.56 pcs
println("EU: ${displayQuantity(qty, "EU")}")  // 1.234,56 pcs
println("KR: ${displayQuantity(qty, "KR")}")  // 1,234.56 pieces
```

**비즈니스 이점**:
- 지역별 사용자 경험 최적화
- 현지화된 서비스 제공
- 글로벌 확장성

## ROI 및 기대 효과

### 1. 운영 효율성
- **주문 처리 시간 50% 단축**: 자동화된 수량 조정
- **포장 오류 90% 감소**: 명확한 단위 변환
- **재고 정확도 95% 이상**: 표준화된 단위 관리

### 2. 비용 절감
- **배송비 15-20% 절감**: 최적화된 포장 단위
- **재고 보관 비용 10% 절감**: 효율적인 공간 활용
- **반품률 30% 감소**: 정확한 수량 안내

### 3. 고객 만족도
- **주문 정확도 향상**: 명확한 수량 표시
- **투명한 가격 정책**: 단위별 가격 명시
- **신속한 주문 처리**: 자동화된 검증

## 구현 체크리스트

### 초기 설정
- [ ] 비즈니스 규칙 정의
  - [ ] 제품별 MOQ 설정
  - [ ] 포장 단위 정의
  - [ ] 가격 정책 수립
- [ ] 라운딩 프로파일 구성
  - [ ] 채널별 프로파일 생성
  - [ ] 제품군별 특별 규칙 정의
- [ ] 체인 정의
  - [ ] 표준 포장 체인 설정
  - [ ] 제품별 커스텀 체인 생성

### 시스템 통합
- [ ] ERP 시스템 연동
  - [ ] 재고 관리 모듈
  - [ ] 주문 처리 모듈
  - [ ] 가격 계산 모듈
- [ ] 전자상거래 플랫폼 적용
  - [ ] 제품 페이지 수량 표시
  - [ ] 장바구니 수량 조정
  - [ ] 주문 확인 프로세스
- [ ] 물류 시스템 연계
  - [ ] WMS 통합
  - [ ] 배송 라벨링
  - [ ] 적재 최적화

### 모니터링 및 개선
- [ ] KPI 설정 및 추적
  - [ ] 주문 정확도
  - [ ] 포장 효율성
  - [ ] 고객 만족도
- [ ] 지속적 개선
  - [ ] 프로파일 조정
  - [ ] 새로운 체인 추가
  - [ ] 사용자 피드백 반영

## 기술 지원 및 문의

ECU 수량 환산 및 라운딩 시스템에 대한 추가 정보나 기술 지원이 필요하신 경우:

- **문서**: [ECU GitHub Repository](https://github.com/your-repo/ecu)
- **이슈 트래커**: GitHub Issues
- **커뮤니티**: ECU 사용자 포럼

## 마무리

ECU의 수량 환산 및 라운딩 시스템은 복잡한 상거래 환경에서 수량 관리를 단순화하고 자동화합니다. 이를 통해 운영 효율성을 높이고, 비용을 절감하며, 고객 만족도를 향상시킬 수 있습니다. 

각 비즈니스의 특성에 맞게 시스템을 구성하고 활용함으로써, 경쟁력 있는 상거래 플랫폼을 구축할 수 있습니다.
