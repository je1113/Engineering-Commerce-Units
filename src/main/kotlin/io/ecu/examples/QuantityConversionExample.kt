package io.ecu.examples

import io.ecu.*

/**
 * 고급 수량 환산 및 라운딩 시스템 사용 예시
 */
fun main() {
    println("=== 수량 환산 및 라운딩 시스템 데모 ===\n")
    
    // 1. 라운딩 프로파일 적용
    demonstrateRoundingProfiles()
    
    // 2. 체인 환산
    demonstrateChainConversion()
    
    // 3. 최적 단위 제안
    demonstrateOptimalUnitSuggestion()
    
    // 4. 포맷팅 옵션
    demonstrateFormatting()
    
    // 5. 비즈니스 시나리오
    demonstrateBusinessScenarios()
}

fun demonstrateRoundingProfiles() {
    println("1. 라운딩 프로파일 적용")
    println("-".repeat(50))
    
    // 고객이 17개를 주문하려고 함
    val requested = ECU.quantity("17 pieces")
    println("고객 요청: $requested")
    
    // 소매 프로파일 (개별 판매)
    val retailRounded = requested.applyRounding(RoundingProfile.RETAIL)
    println("소매 판매: $retailRounded")
    
    // 도매 프로파일 (12개 박스 단위)
    val wholesaleRounded = requested.applyRounding(RoundingProfile.WHOLESALE)
    println("도매 판매: $wholesaleRounded (2 boxes)")
    
    // 음료 6팩 프로파일
    val beverageRounded = requested.applyRounding(RoundingProfile.BEVERAGE_6PACK)
    println("음료 6팩: $beverageRounded (3 packs)")
    
    // 커스텀 프로파일
    val customProfile = RoundingProfile(
        minimumOrderQuantity = 10.0,
        packagingUnit = 5.0,
        roundingMode = RoundingMode.UP,
        incrementUnit = 5.0
    )
    val customRounded = requested.applyRounding(customProfile)
    println("커스텀 (5개 단위): $customRounded")
    
    println()
}

fun demonstrateChainConversion() {
    println("2. 체인 환산 시스템")
    println("-".repeat(50))
    
    val conversionService = ConversionService()
    
    // 1250개의 상품을 다양한 방식으로 표현
    val quantity = ECU.quantity("1250 pieces")
    println("원본 수량: $quantity")
    
    // 분수 표현
    val fractionalBoxes = conversionService.toFractionalRepresentation(
        quantity, "box", "Standard Packaging"
    )
    println("박스 단위 (분수): ${fractionalBoxes.displayValue}")
    
    println()
}

fun demonstrateOptimalUnitSuggestion() {
    println("3. 최적 단위 제안")
    println("-".repeat(50))
    
    val conversionService = ConversionService()
    
    // 다양한 수량에 대한 최적 표현 제안
    val quantities = listOf(
        ECU.quantity("17 pieces"),
        ECU.quantity("144 pieces"),
        ECU.quantity("1250 pieces"),
        ECU.quantity("5000 pieces")
    )
    
    quantities.forEach { qty ->
        val optimal = conversionService.suggestOptimalUnit(qty)
        println("\n원본: $qty")
        println("추천: ${optimal.recommended?.format()}")
        
        // 모든 제안 표시
        optimal.suggestions.forEach { suggestion ->
            println("  - ${suggestion.chainName}: ${suggestion.format()}")
        }
    }
    
    println()
}

fun demonstrateFormatting() {
    println("4. 다양한 포맷팅 옵션")
    println("-".repeat(50))
    
    val quantity = ECU.quantity("1234.56 pieces")
    
    println("기본 포맷: ${quantity.format()}")
    println("컴팩트: ${quantity.toCompactString()}")
    println("상세: ${quantity.toVerboseString()}")
    println("회계용: ${quantity.toAccountingString()}")
    
    // 복합 단위 표현
    val largeQty = ECU.quantity("1250 pieces")
    println("\n복합 단위 표현:")
    println("기본: ${largeQty.formatCompound()}")
    println("상세: ${largeQty.formatCompound(QuantityFormatter.VERBOSE)}")
    
    // 지역별 포맷
    val euFormatter = QuantityFormatter.EU_STANDARD
    println("\n유럽 형식: ${quantity.format(euFormatter)}")
    
    println()
}

fun demonstrateBusinessScenarios() {
    println("5. 실제 비즈니스 시나리오")
    println("-".repeat(50))
    
    // 시나리오 1: B2B 주문 최적화
    println("\n[시나리오 1] B2B 대량 주문")
    val b2bOrder = ECU.quantity("847 pieces")
    val profileSelector = RoundingProfileSelector()
    
    val recommendation = profileSelector.findMostEconomical(b2bOrder.pieces)
    println(recommendation.format())
    
    // 시나리오 2: 재고 관리
    println("\n[시나리오 2] 재고 단위 변환")
    val inventory = ECU.quantity("15840 pieces")
    val conversionService = ConversionService()
    val optimalInventory = conversionService.suggestOptimalUnit(inventory)
    
    println("재고: ${inventory}")
    println("최적 표현: ${optimalInventory.recommended?.format()}")
    
    // 시나리오 3: 주문 검증
    println("\n[시나리오 3] 주문 수량 검증")
    val orderQuantity = 45.0
    val eggProfile = RoundingProfile.EGG_DOZEN
    
    val validation = eggProfile.isValidQuantity(orderQuantity)
    if (!validation.isValid) {
        println("주문 수량 $orderQuantity 는 유효하지 않습니다.")
        println("이유: ${validation.reason}")
        println("제안 수량: ${validation.suggestedQuantity}")
    }
    
    // 시나리오 4: 다단계 포장
    println("\n[시나리오 4] 다단계 포장 계산")
    val items = ECU.quantity("2500 pieces")
    
    // 커스텀 체인 등록
    val customChain = ConversionService.UnitChain(
        name = "Custom Packaging",
        units = listOf(
            ConversionService.UnitChain.ChainUnit("item", "item", 1.0),
            ConversionService.UnitChain.ChainUnit("inner-pack", "inner pack", 10.0),
            ConversionService.UnitChain.ChainUnit("master-carton", "master carton", 5.0),
            ConversionService.UnitChain.ChainUnit("pallet", "pallet", 20.0)
        )
    )
    
    val service = ConversionService()
    service.registerChain(customChain)
    
    val customOptimal = service.suggestOptimalUnit(items)
    println("원본: $items")
    println("커스텀 포장: ${customOptimal.suggestions.find { it.chainName == "Custom Packaging" }?.format()}")
}
