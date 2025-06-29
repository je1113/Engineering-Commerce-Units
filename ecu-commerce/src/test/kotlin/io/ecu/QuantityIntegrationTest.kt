package io.ecu

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.BeforeEach

/**
 * 엔드투엔드 통합 테스트
 * 실제 비즈니스 프로세스를 처음부터 끝까지 테스트합니다.
 */
class QuantityIntegrationTest {
    
    private lateinit var service: QuantityConversionService
    
    @BeforeEach
    fun setup() {
        service = QuantityConversionService()
    }
    
    @Test
    @DisplayName("전체 주문 프로세스: 제품 등록 → 주문 → 재고 확인 → 출고")
    fun `complete order fulfillment process`() {
        // 1. 제품 등록
        val coffeeConfig = ProductUnitConfiguration.builder("COFFEE-BEANS-1KG", "bag")
            .addConversion("box", 1.0, 12.0)     // 1 box = 12 bags
            .addConversion("pallet", 1.0, 480.0)  // 1 pallet = 40 boxes
            .addRoundingProfile("box", RoundingProfile(
                minimumOrderQuantity = 2.0,
                packagingUnit = 1.0,
                roundingMode = RoundingMode.UP
            ))
            .build()
        
        service.registerProduct(coffeeConfig)
        service.registerPackagingHierarchy("COFFEE-BEANS-1KG", PackagingHierarchy(
            productId = "COFFEE-BEANS-1KG",
            levels = listOf(
                PackagingLevel("bag", "1kg bag", 1.0, 1.0),
                PackagingLevel("box", "box (12 bags)", 12.0, 2.0),
                PackagingLevel("pallet", "pallet", 480.0, 0.25)
            )
        ))
        
        // 2. 초기 재고 설정
        val initialStock = ECU.quantity("50 box")  // 600 bags
        println("초기 재고: ${initialStock.format()}")
        
        // 3. 고객 주문 접수
        val customerOrders = listOf(
            "CUST-001" to ECU.quantity("15 bag"),   // 개인 고객
            "CUST-002" to ECU.quantity("3 box"),    // 소매점
            "CUST-003" to ECU.quantity("100 bag")   // 대형 카페
        )
        
        var currentStock = service.convert("COFFEE-BEANS-1KG", initialStock, "bag")
        
        // 4. 각 주문 처리
        customerOrders.forEach { (customerId, orderQty) ->
            println("\n주문 처리: $customerId")
            println("  요청: ${orderQty.format()}")
            
            // 재고 확인
            val availability = service.checkAvailability(
                "COFFEE-BEANS-1KG", 
                orderQty, 
                currentStock
            )
            
            if (availability.canFulfill) {
                println("  ✓ 주문 가능")
                
                // 주문 수량을 bag 단위로 변환
                val orderInBags = service.convert("COFFEE-BEANS-1KG", orderQty, "bag")
                
                // 재고 차감
                currentStock = Quantity.pieces(currentStock.pieces - orderInBags.pieces)
                    .copy(symbol = "bag", displayName = "1kg bag")
                
                println("  출고 후 재고: ${currentStock.format()}")
                
                // 박스 단위로 재고 표시
                val stockInBoxes = service.convert("COFFEE-BEANS-1KG", currentStock, "box")
                println("  = ${stockInBoxes.format()}")
            } else {
                println("  ✗ 재고 부족!")
                println("  부족량: ${availability.shortage?.format()}")
                
                // 대안 제시
                if (availability.alternativeOptions.isNotEmpty()) {
                    println("  대안:")
                    availability.alternativeOptions.forEach { alt ->
                        println("    - ${alt.description}")
                    }
                }
            }
        }
        
        // 5. 최종 재고 확인
        val finalStockInBoxes = service.convert("COFFEE-BEANS-1KG", currentStock, "box")
        assertEquals(38.5, finalStockInBoxes.pieces, 0.1)  // 50 - 15/12 - 3 - 100/12 ≈ 38.5 boxes
        
        // 6. 재주문 필요 확인
        val reorderPoint = ECU.quantity("10 box")  // 재주문점
        val needsReorder = finalStockInBoxes.pieces <= reorderPoint.pieces
        assertFalse(needsReorder)  // 38.5 > 10, 아직 재주문 불필요
    }
    
    @Test
    @DisplayName("B2B 도매 프로세스: MOQ 적용 및 대량 할인")
    fun `B2B wholesale process with MOQ and bulk pricing`() {
        // 1. 도매 제품 설정
        val plasticBottleConfig = ProductUnitConfiguration.builder("BOTTLE-500ML", "piece")
            .addConversion("sleeve", 1.0, 50.0)      // 1 sleeve = 50 bottles
            .addConversion("carton", 1.0, 500.0)     // 1 carton = 10 sleeves
            .addConversion("pallet", 1.0, 10000.0)   // 1 pallet = 20 cartons
            .addRoundingProfile("carton", RoundingProfile(
                minimumOrderQuantity = 5.0,  // 최소 5 cartons
                packagingUnit = 1.0,
                roundingMode = RoundingMode.UP,
                allowFractional = false
            ))
            .build()
        
        service.registerProduct(plasticBottleConfig)
        
        // 2. 도매 주문 시나리오
        data class WholesaleOrder(
            val requested: String,
            val expectedAdjusted: String,
            val unitPrice: Double  // per carton
        )
        
        val wholesaleOrders = listOf(
            WholesaleOrder("3 carton", "5 carton", 250.0),      // MOQ 미달 → 조정
            WholesaleOrder("8 carton", "8 carton", 240.0),      // 정상
            WholesaleOrder("25 carton", "25 carton", 220.0),    // 대량 할인
            WholesaleOrder("2.5 pallet", "2.5 pallet", 200.0)   // 팔레트 단위
        )
        
        var totalRevenue = 0.0
        var totalPieces = 0.0
        
        wholesaleOrders.forEach { order ->
            println("\n도매 주문: ${order.requested}")
            
            val requestedQty = ECU.quantity(order.requested)
            val requestedInCartons = service.convert("BOTTLE-500ML", requestedQty, "carton")
            
            // MOQ 적용
            val profile = plasticBottleConfig.getRoundingProfile("carton")
            val adjustedCartons = profile?.applyRounding(requestedInCartons.pieces) 
                ?: requestedInCartons.pieces
            
            println("  요청: ${requestedInCartons.pieces} cartons")
            println("  조정: $adjustedCartons cartons")
            
            // 가격 계산
            val orderRevenue = adjustedCartons * order.unitPrice
            totalRevenue += orderRevenue
            
            // 총 개수 계산
            val piecesInOrder = adjustedCartons * 500  // 500 pieces per carton
            totalPieces += piecesInOrder
            
            println("  금액: $${"%.2f".format(orderRevenue)}")
            println("  수량: ${piecesInOrder.toInt()} pieces")
            
            // 예상값 검증
            val expectedQty = ECU.quantity(order.expectedAdjusted)
            val expectedInCartons = service.convert("BOTTLE-500ML", expectedQty, "carton")
            assertEquals(expectedInCartons.pieces, adjustedCartons)
        }
        
        println("\n총 매출: $${"%.2f".format(totalRevenue)}")
        println("총 판매 수량: ${totalPieces.toInt()} pieces")
        
        // 검증
        assertTrue(totalRevenue > 10000)  // 상당한 매출
        assertEquals(44000.0, totalPieces)  // (5 + 8 + 25 + 50) * 500
    }
    
    @Test
    @DisplayName("다국가 물류 센터 재고 이동")
    fun `multi-warehouse inventory transfer`() {
        // 1. 제품 설정 (전자제품)
        val laptopConfig = ProductUnitConfiguration.builder("LAPTOP-15INCH", "unit")
            .addConversion("box", 1.0, 5.0)        // 1 box = 5 laptops
            .addConversion("pallet", 1.0, 100.0)   // 1 pallet = 20 boxes
            .build()
        
        service.registerProduct(laptopConfig)
        
        // 2. 각 물류 센터 재고
        data class Warehouse(
            val location: String,
            var stock: Quantity,
            val safetyStock: Quantity
        )
        
        val warehouses = listOf(
            Warehouse("Seoul", ECU.quantity("50 box"), ECU.quantity("10 box")),
            Warehouse("Tokyo", ECU.quantity("30 box"), ECU.quantity("15 box")),
            Warehouse("Singapore", ECU.quantity("5 box"), ECU.quantity("20 box"))
        )
        
        println("초기 재고 현황:")
        warehouses.forEach { wh ->
            val units = service.convert("LAPTOP-15INCH", wh.stock, "unit")
            val safetyUnits = service.convert("LAPTOP-15INCH", wh.safetyStock, "unit")
            println("  ${wh.location}: ${wh.stock.format()} (${units.format()}) " +
                    "- 안전재고: ${wh.safetyStock.format()}")
            
            val isBelowSafety = units.pieces < safetyUnits.pieces
            if (isBelowSafety) {
                println("    ⚠️ 안전재고 미달!")
            }
        }
        
        // 3. 재고 이동 계획
        println("\n재고 이동 실행:")
        
        // Seoul → Singapore 이동 (15 boxes)
        val transferQty = ECU.quantity("15 box")
        val seoulWh = warehouses.find { it.location == "Seoul" }!!
        val singaporeWh = warehouses.find { it.location == "Singapore" }!!
        
        println("Seoul → Singapore: ${transferQty.format()}")
        
        // 이동 가능 여부 확인
        val canTransfer = service.checkAvailability(
            "LAPTOP-15INCH",
            transferQty,
            seoulWh.stock
        )
        
        assertTrue(canTransfer.canFulfill)
        
        // 재고 이동 실행
        seoulWh.stock = Quantity.of(
            seoulWh.stock.pieces - transferQty.pieces,
            "box"
        )
        
        singaporeWh.stock = Quantity.of(
            singaporeWh.stock.pieces + transferQty.pieces,
            "box"
        )
        
        // 4. 이동 후 재고 현황
        println("\n이동 후 재고:")
        warehouses.forEach { wh ->
            val units = service.convert("LAPTOP-15INCH", wh.stock, "unit")
            val safetyUnits = service.convert("LAPTOP-15INCH", wh.safetyStock, "unit")
            println("  ${wh.location}: ${wh.stock.format()} (${units.format()})")
            
            val stockLevel = units.pieces / safetyUnits.pieces * 100
            println("    재고 수준: ${"%.1f".format(stockLevel)}%")
            
            when {
                stockLevel < 50 -> println("    🔴 위험")
                stockLevel < 100 -> println("    🟡 주의")
                else -> println("    🟢 정상")
            }
        }
        
        // 검증
        assertEquals(35.0, seoulWh.stock.pieces)      // 50 - 15
        assertEquals(20.0, singaporeWh.stock.pieces)  // 5 + 15
        
        // Singapore가 이제 안전재고 수준 달성
        val singaporeFinal = service.convert("LAPTOP-15INCH", singaporeWh.stock, "unit")
        val singaporeSafety = service.convert("LAPTOP-15INCH", singaporeWh.safetyStock, "unit")
        assertEquals(singaporeFinal.pieces, singaporeSafety.pieces)
    }
    
    // 헬퍼 메서드
    private fun Quantity.copy(
        baseValue: Double = this.baseValue,
        symbol: String = this.symbol,
        displayName: String = this.displayName
    ): Quantity {
        return Quantity.of(baseValue, symbol)
    }
}
