package io.ecu.examples

import io.ecu.*

/**
 * ECU 수량 단위 시스템 사용 가이드
 * 
 * 이 예제는 커머스 도메인에서 수량 단위를 효과적으로 관리하는 방법을 보여줍니다.
 */
fun main() {
    println("=== ECU Quantity Units Usage Guide ===\n")
    
    // 실제 사용 시나리오들
    scenario1_SimpleEcommerce()
    scenario2_B2BCommerce()
    scenario3_InventoryManagement()
    scenario4_InternationalTrade()
}

/**
 * 시나리오 1: 간단한 이커머스 상점
 * - 제품별 포장 단위 관리
 * - 고객 주문 처리
 */
fun scenario1_SimpleEcommerce() {
    println("📦 시나리오 1: 온라인 상점 주문 처리")
    println("=" * 50)
    
    // 제품 등록: 음료수 (6개들이 팩)
    val beverageConfig = ProductUnitConfiguration.builder("BEV-001", "piece")
        .addConversion("pack", 1.0, 6.0)    // 1 pack = 6 bottles
        .addConversion("box", 1.0, 24.0)    // 1 box = 24 bottles (4 packs)
        .addRoundingProfile("pack", RoundingProfile(
            minimumOrderQuantity = 1.0,
            packagingUnit = 1.0,
            roundingMode = RoundingMode.UP,
            allowFractional = false
        ))
        .build()
    
    // 제품 등록: 계란 (30개들이 판)
    val eggConfig = ProductUnitConfiguration.builder("EGG-001", "piece")
        .addConversion("tray", 1.0, 30.0)   // 1 tray = 30 eggs
        .addConversion("box", 1.0, 360.0)   // 1 box = 12 trays
        .build()
    
    val service = QuantityConversionService()
    service.registerProduct(beverageConfig)
    service.registerProduct(eggConfig)
    
    // 고객 주문
    println("\n🛒 고객 주문:")
    
    // 주문 1: 음료수 3팩
    val beverageOrder = ECU.quantity("3 pack")
    val beveragePieces = service.convert("BEV-001", beverageOrder, "piece")
    println("- 음료수 3팩 = ${beveragePieces.format()}")
    
    // 주문 2: 계란 2판
    val eggOrder = ECU.quantity("2 tray")
    val eggPieces = service.convert("EGG-001", eggOrder, "piece")
    println("- 계란 2판 = ${eggPieces.format()}")
    
    // 총 아이템 수 (다른 단위지만 통계용으로 합산 가능)
    val totalItems = beveragePieces.pieces + eggPieces.pieces
    println("\n📊 총 아이템 수: ${totalItems.toInt()}개")
    
    println("\n")
}

/**
 * 시나리오 2: B2B 도매 거래
 * - 대량 주문 처리
 * - 최소 주문 수량 적용
 */
fun scenario2_B2BCommerce() {
    println("🏭 시나리오 2: B2B 도매 거래")
    println("=" * 50)
    
    // 도매 제품 설정: 산업용 나사
    val screwConfig = ProductUnitConfiguration.builder("SCR-M6", "piece")
        .addConversion("box", 1.0, 1000.0)      // 1 box = 1,000 screws
        .addConversion("carton", 1.0, 10000.0)  // 1 carton = 10 boxes
        .addConversion("pallet", 1.0, 100000.0) // 1 pallet = 10 cartons
        .addRoundingProfile("box", RoundingProfile.WHOLESALE.copy(
            minimumOrderQuantity = 5.0,  // 최소 5박스
            packagingUnit = 1.0
        ))
        .addRoundingProfile("carton", RoundingProfile.BULK.copy(
            minimumOrderQuantity = 1.0,
            packagingUnit = 1.0
        ))
        .build()
    
    val service = QuantityConversionService()
    service.registerProduct(screwConfig)
    
    // 포장 계층 설정
    val screwHierarchy = PackagingHierarchy(
        productId = "SCR-M6",
        levels = listOf(
            PackagingLevel("piece", "piece", 1.0, 1000.0),      // 최소 1000개
            PackagingLevel("box", "box", 1000.0, 5.0),          // 최소 5박스
            PackagingLevel("carton", "carton", 10000.0, 1.0),   // 최소 1카톤
            PackagingLevel("pallet", "pallet", 100000.0, 0.5)   // 최소 0.5팔레트
        )
    )
    service.registerPackagingHierarchy("SCR-M6", screwHierarchy)
    
    println("\n📋 도매 주문 처리:")
    
    // 다양한 주문량에 대한 최적 포장 제안
    val orders = listOf(
        ECU.quantity("3500 pieces"),   // 박스 단위로 올림 필요
        ECU.quantity("25000 pieces"),  // 카톤 단위 고려
        ECU.quantity("150000 pieces")  // 팔레트 단위 최적
    )
    
    orders.forEach { order ->
        println("\n주문: ${order.format()}")
        
        val suggestion = service.suggestOptimalPackaging("SCR-M6", order)
        println("최적 포장:")
        suggestion.optimal?.components?.forEach { comp ->
            println("  - ${comp.count} ${comp.level.displayName} " +
                    "(${comp.totalUnits.toInt()} pieces)")
        }
        
        // 실제 주문 가능 수량 (라운딩 적용)
        val boxOrder = service.convert("SCR-M6", order, "box")
        val roundedBoxes = screwConfig.getRoundingProfile("box")
            ?.applyRounding(boxOrder.pieces) ?: boxOrder.pieces
        
        if (roundedBoxes != boxOrder.pieces) {
            println("라운딩 적용: ${boxOrder.pieces.toInt()} → ${roundedBoxes.toInt()} boxes")
            val finalPieces = service.convert("SCR-M6", 
                Quantity.of(roundedBoxes, "box"), "piece")
            println("최종 주문량: ${finalPieces.format()}")
        }
    }
    
    println("\n")
}

/**
 * 시나리오 3: 재고 관리 시스템
 * - 재고 수준 확인
 * - 주문 가능 여부 판단
 */
fun scenario3_InventoryManagement() {
    println("📊 시나리오 3: 재고 관리")
    println("=" * 50)
    
    val service = QuantityConversionService()
    
    // 의류 제품 설정 (사이즈별 SKU)
    val shirtConfig = ProductUnitConfiguration.builder("SHIRT-M", "piece")
        .addConversion("pack", 1.0, 3.0)     // 3개 묶음
        .addConversion("box", 1.0, 36.0)     // 1 box = 12 packs
        .build()
    
    service.registerProduct(shirtConfig)
    service.registerPackagingHierarchy("SHIRT-M", 
        PackagingHierarchy.STANDARD_RETAIL)
    
    // 현재 재고
    val currentStock = ECU.quantity("250 pieces")
    println("\n📦 현재 재고: ${currentStock.format()}")
    println("  = ${service.convert("SHIRT-M", currentStock, "box").format()}")
    println("  = ${service.convert("SHIRT-M", currentStock, "pack").format()}")
    
    // 주문 검증
    println("\n🛍️ 주문 가능 여부 확인:")
    
    val orders = listOf(
        "20 pack" to "소매 주문",
        "5 box" to "도매 주문",
        "300 pieces" to "대량 주문"
    )
    
    orders.forEach { (orderStr, orderType) ->
        val order = ECU.quantity(orderStr)
        val result = service.checkAvailability("SHIRT-M", order, currentStock)
        
        println("\n[$orderType] ${order.format()}")
        println("  ✓ 주문 가능: ${if (result.canFulfill) "예" else "아니오"}")
        
        if (!result.canFulfill) {
            println("  ✗ 부족 수량: ${result.shortage?.format()}")
            println("  💡 대안:")
            result.alternativeOptions.forEach { alt ->
                println("     - ${alt.description}")
            }
        } else {
            val remaining = currentStock.pieces - 
                service.convert("SHIRT-M", order, "piece").pieces
            println("  ✓ 주문 후 재고: ${remaining.toInt()} pieces")
        }
    }
    
    println("\n")
}

/**
 * 시나리오 4: 국제 무역
 * - 다양한 포장 규격
 * - 컨테이너 적재 계산
 */
fun scenario4_InternationalTrade() {
    println("🌍 시나리오 4: 국제 무역 / 수출입")
    println("=" * 50)
    
    // 수출 제품: 전자부품
    val componentConfig = ProductUnitConfiguration.builder("COMP-X1", "piece")
        .addConversion("tube", 1.0, 50.0)         // 튜브 포장
        .addConversion("tray", 1.0, 500.0)        // 트레이 포장
        .addConversion("carton", 1.0, 5000.0)     // 수출용 카톤
        .addConversion("pallet", 1.0, 50000.0)    // 팔레트
        .build()
    
    val service = QuantityConversionService()
    service.registerProduct(componentConfig)
    
    // 컨테이너 적재 계층
    val exportHierarchy = PackagingHierarchy(
        productId = "COMP-X1",
        levels = listOf(
            PackagingLevel("tube", "tube", 50.0, 10.0),
            PackagingLevel("tray", "tray", 500.0, 1.0),
            PackagingLevel("carton", "carton", 5000.0, 1.0),
            PackagingLevel("pallet", "pallet", 50000.0, 1.0),
            PackagingLevel("container", "20ft container", 1000000.0, 1.0)  // 20 pallets
        )
    )
    service.registerPackagingHierarchy("COMP-X1", exportHierarchy)
    
    println("\n🚢 수출 주문 처리:")
    
    // 대량 수출 주문
    val exportOrder = ECU.quantity("2500000 pieces")  // 250만개
    
    println("주문량: ${exportOrder.format()}")
    
    val suggestion = service.suggestOptimalPackaging("COMP-X1", exportOrder)
    println("\n📦 최적 컨테이너 적재:")
    suggestion.optimal?.components?.forEach { comp ->
        when (comp.level.symbol) {
            "container" -> println("  🚢 ${comp.count} x 20ft 컨테이너")
            "pallet" -> println("  📦 ${comp.count} x 팔레트")
            "carton" -> println("  📦 ${comp.count} x 카톤")
            else -> println("  📦 ${comp.count} x ${comp.level.displayName}")
        }
    }
    
    // 컨테이너 효율성
    val containerCount = exportOrder.pieces / 1000000.0
    println("\n📊 적재 효율성:")
    println("  - 필요 컨테이너: ${kotlin.math.ceil(containerCount).toInt()}개")
    println("  - 적재율: ${(exportOrder.pieces % 1000000.0) / 10000.0}%")
    
    // 다양한 포장 옵션 비교
    println("\n💼 포장 옵션 비교:")
    val packagingOptions = listOf("tube", "tray", "carton", "pallet")
    packagingOptions.forEach { unit ->
        val converted = service.convert("COMP-X1", exportOrder, unit)
        println("  - ${unit}: ${converted.format()}")
    }
    
    println("\n")
}

// 확장 함수들
private operator fun String.times(count: Int) = repeat(count)
private fun Double.format(decimals: Int = 2) = "%.${decimals}f".format(this)
