package io.ecu.examples

import io.ecu.*

/**
 * 수량 단위 시스템 사용 예제
 */
fun main() {
    println("=== ECU Quantity Units System Example ===\n")
    
    // 1. 기본 수량 단위 변환
    basicQuantityConversion()
    
    // 2. 제품별 환산 비율 적용
    productSpecificConversion()
    
    // 3. 포장 계층 및 최적화
    packagingHierarchyExample()
    
    // 4. 재고 가용성 확인
    inventoryAvailabilityExample()
    
    // 5. 라운딩 프로파일 적용
    roundingProfileExample()
}

fun basicQuantityConversion() {
    println("1. Basic Quantity Conversion")
    println("-" * 40)
    
    // 다양한 수량 단위 생성
    val qty1 = ECU.quantity("25 dozens")
    println("Input: 25 dozens")
    println("  Pieces: ${qty1.pieces}")
    println("  Gross: ${qty1.gross}")
    println("  Reams: ${qty1.reams}")
    
    // Factory 메서드 사용
    val qty2 = Quantity.pieces(1250.0)
    println("\nInput: 1250 pieces")
    println("  Dozens: ${qty2.dozens}")
    println("  Gross: ${qty2.gross}")
    println("  To dozens: ${qty2.to("dozen")}")
    
    // 박스 단위 변환
    val qty3 = ECU.quantity("100 pieces")
    println("\nInput: 100 pieces")
    println("  To boxes (12): ${qty3.toBoxes(12).format()}")
    println("  To boxes (24): ${qty3.toBoxes(24).format()}")
    println("  To pallets (240): ${qty3.toPallets(240).format()}")
    
    println()
}

fun productSpecificConversion() {
    println("2. Product-Specific Conversion")
    println("-" * 40)
    
    // 제품 A: 1 박스 = 10개
    val productA = ProductUnitConfiguration.builder("PROD-A", "piece")
        .addConversion("box", 1.0, 10.0)
        .addConversion("case", 1.0, 120.0)  // 1 case = 12 boxes
        .addConversion("pallet", 1.0, 2400.0)  // 1 pallet = 20 cases
        .build()
    
    // 제품 B: 1 박스 = 6개
    val productB = ProductUnitConfiguration.builder("PROD-B", "piece")
        .addConversion("box", 1.0, 6.0)
        .addConversion("case", 1.0, 72.0)  // 1 case = 12 boxes
        .addConversion("pallet", 1.0, 1440.0)  // 1 pallet = 20 cases
        .build()
    
    val service = QuantityConversionService()
    service.registerProduct(productA)
    service.registerProduct(productB)
    
    // 같은 박스 수량이지만 제품별로 다른 개수
    val order = ECU.quantity("5 box")
    
    val resultA = service.convert("PROD-A", order, "piece")
    val resultB = service.convert("PROD-B", order, "piece")
    
    println("Order: 5 boxes")
    println("  Product A (10 pcs/box): ${resultA.format()}")
    println("  Product B (6 pcs/box): ${resultB.format()}")
    
    // 역변환
    val pieces = ECU.quantity("144 pieces")
    val boxesA = service.convert("PROD-A", pieces, "box")
    val boxesB = service.convert("PROD-B", pieces, "box")
    
    println("\nOrder: 144 pieces")
    println("  Product A: ${boxesA.format()}")
    println("  Product B: ${boxesB.format()}")
    
    println()
}

fun packagingHierarchyExample() {
    println("3. Packaging Hierarchy & Optimization")
    println("-" * 40)
    
    val service = QuantityConversionService()
    
    // 커스텀 포장 계층 정의
    val hierarchy = PackagingHierarchy(
        productId = "PROD-C",
        levels = listOf(
            PackagingLevel("piece", "piece", 1.0, 1.0),
            PackagingLevel("inner", "inner pack", 6.0, 1.0),
            PackagingLevel("box", "box", 24.0, 1.0),
            PackagingLevel("case", "case", 144.0, 1.0),
            PackagingLevel("pallet", "pallet", 2880.0, 0.5)  // 최소 0.5 팔레트
        )
    )
    
    service.registerPackagingHierarchy("PROD-C", hierarchy)
    
    // 다양한 수량에 대한 최적 포장 제안
    val quantities = listOf(
        ECU.quantity("17 pieces"),
        ECU.quantity("250 pieces"),
        ECU.quantity("1500 pieces"),
        ECU.quantity("5000 pieces")
    )
    
    quantities.forEach { qty ->
        val suggestion = service.suggestOptimalPackaging("PROD-C", qty)
        println("\nQuantity: ${qty.format()}")
        println("Optimal packaging:")
        suggestion.optimal?.components?.forEach { component ->
            println("  ${component.count} x ${component.level.displayName} = ${component.totalUnits} pieces")
        }
        println("Efficiency: ${(suggestion.optimal?.efficiency ?: 0.0) * 100}%")
        println("Reason: ${suggestion.reason}")
    }
    
    println()
}

fun inventoryAvailabilityExample() {
    println("4. Inventory Availability Check")
    println("-" * 40)
    
    val service = QuantityConversionService()
    
    // 제품 설정
    val product = ProductUnitConfiguration.builder("PROD-D", "piece")
        .addConversion("box", 1.0, 12.0)
        .addConversion("case", 1.0, 144.0)
        .build()
    
    service.registerProduct(product)
    service.registerPackagingHierarchy("PROD-D", PackagingHierarchy.STANDARD_RETAIL)
    
    // 재고 상황
    val available = ECU.quantity("500 pieces")
    
    // 다양한 주문 확인
    val orders = listOf(
        ECU.quantity("30 box"),    // 360 pieces
        ECU.quantity("50 box"),    // 600 pieces - 재고 부족
        ECU.quantity("3 case")     // 432 pieces
    )
    
    orders.forEach { requested ->
        val result = service.checkAvailability("PROD-D", requested, available)
        
        println("\nRequested: ${requested.format()}")
        println("Available: ${available.format()}")
        println("Can fulfill: ${result.canFulfill}")
        
        if (!result.canFulfill) {
            println("Shortage: ${result.shortage?.format()}")
            println("Alternative options:")
            result.alternativeOptions.forEach { option ->
                println("  - ${option.description} (${option.percentageOfRequest.format(1)}% of request)")
            }
        }
    }
    
    println()
}

fun roundingProfileExample() {
    println("5. Rounding Profile Application")
    println("-" * 40)
    
    // 다양한 라운딩 프로파일
    val profiles = mapOf(
        "Retail" to RoundingProfile.RETAIL,
        "Wholesale" to RoundingProfile.WHOLESALE,
        "Bulk" to RoundingProfile.BULK,
        "Custom" to RoundingProfile(
            minimumOrderQuantity = 6.0,
            packagingUnit = 6.0,
            roundingMode = RoundingMode.UP,
            allowFractional = false
        )
    )
    
    val quantities = listOf(3.2, 7.5, 15.3, 125.7)
    
    println("Original quantities: ${quantities.joinToString(", ")}")
    println()
    
    profiles.forEach { (name, profile) ->
        println("$name profile (MOQ: ${profile.minimumOrderQuantity}, " +
                "Unit: ${profile.packagingUnit}):")
        quantities.forEach { qty ->
            val rounded = profile.applyRounding(qty)
            println("  $qty → $rounded")
        }
        println()
    }
}

// 확장 함수: Double 포맷팅
fun Double.format(decimals: Int = 2): String {
    return "%.${decimals}f".format(this)
}

// 확장 함수: 반복 문자
operator fun String.times(count: Int): String {
    return this.repeat(count)
}
