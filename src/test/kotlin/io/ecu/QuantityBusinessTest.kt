package io.ecu

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.math.abs

/**
 * 실제 비즈니스 시나리오 기반 테스트
 * 커머스 도메인에서 발생할 수 있는 실제 상황들을 테스트합니다.
 */
class QuantityBusinessTest {
    
    private lateinit var service: QuantityConversionService
    
    @BeforeEach
    fun setup() {
        service = QuantityConversionService()
    }
    
    @Nested
    @DisplayName("온라인 식료품점 시나리오")
    inner class OnlineGroceryStoreTest {
        
        @BeforeEach
        fun setupProducts() {
            // 계란: 30개들이 판
            val eggConfig = ProductUnitConfiguration.builder("EGG-LARGE", "piece")
                .addConversion("tray", 1.0, 30.0)
                .addConversion("box", 1.0, 360.0)  // 12 trays
                .build()
            
            // 우유: 1리터 팩, 12개들이 박스
            val milkConfig = ProductUnitConfiguration.builder("MILK-1L", "piece")
                .addConversion("pack", 1.0, 1.0)   // 1 pack = 1L
                .addConversion("box", 1.0, 12.0)   // 12 packs per box
                .build()
            
            // 바나나: kg 단위 판매, 박스는 13kg
            val bananaConfig = ProductUnitConfiguration.builder("BANANA", "kg")
                .addConversion("piece", 5.0, 1.0)  // 약 5개 = 1kg
                .addConversion("box", 1.0, 13.0)   // 1 box = 13kg
                .build()
            
            service.registerProduct(eggConfig)
            service.registerProduct(milkConfig)
            service.registerProduct(bananaConfig)
        }
        
        @Test
        @DisplayName("고객이 계란 2판 주문 시 정확한 개수 계산")
        fun `customer orders 2 egg trays`() {
            val order = ECU.quantity("2 tray")
            val pieces = service.convert("EGG-LARGE", order, "piece")
            
            assertEquals(60.0, pieces.pieces)
            assertEquals("60.0 piece", pieces.format())
        }
        
        @Test
        @DisplayName("재고 부족 시 대안 제시")
        fun `suggest alternatives when out of stock`() {
            val requested = ECU.quantity("5 tray")  // 150개 요청
            val available = ECU.quantity("100 piece")  // 100개만 재고
            
            val result = service.checkAvailability("EGG-LARGE", requested, available)
            
            assertFalse(result.canFulfill)
            assertEquals(50.0, result.shortage?.pieces)
            assertTrue(result.alternativeOptions.isNotEmpty())
            
            // 대안이 3판(90개)을 제시하는지 확인
            val bestAlternative = result.alternativeOptions.first()
            assertTrue(bestAlternative.description.contains("3"))
        }
        
        @Test
        @DisplayName("kg 단위 과일을 개수로 변환")
        fun `convert fruit from kg to pieces approximately`() {
            val order = ECU.quantity("2.5 kg")  // 2.5kg 바나나
            val pieces = service.convert("BANANA", order, "piece")
            
            // 2.5kg = 약 12.5개
            assertEquals(12.5, pieces.pieces, 0.1)
        }
        
        @Test
        @DisplayName("혼합 장바구니 총 아이템 수 계산")
        fun `calculate total items in mixed shopping cart`() {
            val cart = listOf(
                "EGG-LARGE" to ECU.quantity("1 tray"),     // 30개
                "MILK-1L" to ECU.quantity("6 pack"),       // 6개
                "BANANA" to ECU.quantity("2 kg")           // 약 10개
            )
            
            val totalItems = cart.sumOf { (productId, qty) ->
                service.convert(productId, qty, "piece").pieces
            }
            
            assertEquals(46.0, totalItems, 0.1)
        }
    }
    
    @Nested
    @DisplayName("B2B 도매 거래 시나리오")
    inner class B2BWholesaleTest {
        
        @BeforeEach
        fun setupProducts() {
            // 산업용 볼트: 최소 주문 단위 적용
            val boltConfig = ProductUnitConfiguration.builder("BOLT-M10", "piece")
                .addConversion("box", 1.0, 100.0)
                .addConversion("carton", 1.0, 1200.0)  // 12 boxes
                .addRoundingProfile("box", RoundingProfile(
                    minimumOrderQuantity = 5.0,  // 최소 5박스
                    packagingUnit = 1.0,
                    roundingMode = RoundingMode.UP
                ))
                .build()
            
            // A4 용지: 500매들이 ream, 10 ream = 1 box
            val paperConfig = ProductUnitConfiguration.builder("PAPER-A4", "sheet")
                .addConversion("ream", 1.0, 500.0)
                .addConversion("box", 1.0, 5000.0)  // 10 reams
                .addConversion("pallet", 1.0, 200000.0)  // 40 boxes
                .build()
            
            service.registerProduct(boltConfig)
            service.registerProduct(paperConfig)
            
            // 포장 계층 등록
            service.registerPackagingHierarchy("BOLT-M10", PackagingHierarchy(
                productId = "BOLT-M10",
                levels = listOf(
                    PackagingLevel("piece", "piece", 1.0, 100.0),
                    PackagingLevel("box", "box", 100.0, 5.0),
                    PackagingLevel("carton", "carton", 1200.0, 1.0)
                )
            ))
        }
        
        @Test
        @DisplayName("최소 주문 수량 미달 시 자동 조정")
        fun `adjust order to minimum quantity`() {
            val config = service.productConfigurations["BOLT-M10"]!!
            val profile = config.getRoundingProfile("box")!!
            
            // 3박스 주문 시도 → 5박스로 조정
            val requestedBoxes = 3.0
            val adjustedBoxes = profile.applyRounding(requestedBoxes)
            
            assertEquals(5.0, adjustedBoxes)
            
            // 개수로 환산
            val pieces = service.convert("BOLT-M10", 
                Quantity.of(adjustedBoxes, "box"), "piece")
            assertEquals(500.0, pieces.pieces)
        }
        
        @Test
        @DisplayName("대량 주문 시 최적 포장 단위 제안")
        fun `suggest optimal packaging for bulk orders`() {
            val order = ECU.quantity("5500 piece")  // 볼트 5,500개
            val suggestion = service.suggestOptimalPackaging("BOLT-M10", order)
            
            assertNotNull(suggestion.optimal)
            
            // 4 cartons + 3 boxes + 0 pieces 가 최적
            val components = suggestion.optimal!!.components
            
            val cartons = components.find { it.level.symbol == "carton" }?.count ?: 0
            val boxes = components.find { it.level.symbol == "box" }?.count ?: 0
            
            assertEquals(4, cartons)  // 4 x 1200 = 4800
            assertEquals(7, boxes)    // 7 x 100 = 700
            assertEquals(5500.0, suggestion.optimal!!.totalUnits)
        }
        
        @Test
        @DisplayName("용지 대량 주문 - 팔레트 단위 변환")
        fun `convert large paper order to pallets`() {
            val order = ECU.quantity("50 box")  // 50박스 = 250,000매
            
            val sheets = service.convert("PAPER-A4", order, "sheet")
            assertEquals(250000.0, sheets.pieces)
            
            val pallets = service.convert("PAPER-A4", order, "pallet")
            assertEquals(1.25, pallets.pieces)  // 1.25 팔레트
        }
        
        @Test
        @DisplayName("부분 팔레트 주문 시 박스 단위로 표현")
        fun `express partial pallet order in boxes`() {
            val order = ECU.quantity("0.3 pallet")  // 0.3 팔레트
            val boxes = service.convert("PAPER-A4", order, "box")
            
            assertEquals(12.0, boxes.pieces)  // 0.3 * 40 = 12 boxes
        }
    }
    
    @Nested
    @DisplayName("재고 관리 시나리오")
    inner class InventoryManagementTest {
        
        @BeforeEach
        fun setupProducts() {
            // 티셔츠: 사이즈별 SKU
            val shirtConfig = ProductUnitConfiguration.builder("SHIRT-L-BLACK", "piece")
                .addConversion("pack", 1.0, 3.0)     // 3개 묶음
                .addConversion("box", 1.0, 36.0)     // 12 packs
                .build()
            
            service.registerProduct(shirtConfig)
        }
        
        @Test
        @DisplayName("재고 회전율 계산을 위한 단위 변환")
        fun `convert units for inventory turnover calculation`() {
            val monthlyData = listOf(
                "초기 재고" to ECU.quantity("10 box"),      // 360개
                "입고" to ECU.quantity("5 box"),            // 180개  
                "판매" to ECU.quantity("150 piece"),        // 150개
                "반품" to ECU.quantity("2 pack")            // 6개
            )
            
            val initialStock = service.convert("SHIRT-L-BLACK", 
                monthlyData[0].second, "piece").pieces
            val received = service.convert("SHIRT-L-BLACK", 
                monthlyData[1].second, "piece").pieces
            val sold = monthlyData[2].second.pieces
            val returned = service.convert("SHIRT-L-BLACK", 
                monthlyData[3].second, "piece").pieces
            
            val finalStock = initialStock + received - sold + returned
            
            assertEquals(396.0, finalStock)  // 360 + 180 - 150 + 6
            
            // 박스 단위로 재고 표시
            val finalBoxes = service.convert("SHIRT-L-BLACK", 
                Quantity.pieces(finalStock), "box")
            assertEquals(11.0, finalBoxes.pieces)  // 396 / 36 = 11
        }
        
        @Test
        @DisplayName("안전 재고 수준 확인")
        fun `check safety stock levels`() {
            val safetyStock = ECU.quantity("3 box")     // 안전 재고: 3박스
            val currentStock = ECU.quantity("100 piece") // 현재 재고: 100개
            
            val safetyPieces = service.convert("SHIRT-L-BLACK", 
                safetyStock, "piece").pieces
            
            val isAboveSafety = currentStock.pieces >= safetyPieces
            assertFalse(isAboveSafety)  // 100 < 108 (3 boxes)
            
            val shortage = safetyPieces - currentStock.pieces
            assertEquals(8.0, shortage)
        }
    }
    
    @Nested
    @DisplayName("국제 무역 시나리오")
    inner class InternationalTradeTest {
        
        @BeforeEach
        fun setupProducts() {
            // 전자부품: 릴, 트레이, 튜브 포장
            val chipConfig = ProductUnitConfiguration.builder("IC-555", "piece")
                .addConversion("tube", 1.0, 25.0)
                .addConversion("tray", 1.0, 100.0)
                .addConversion("reel", 1.0, 3000.0)
                .addConversion("carton", 1.0, 30000.0)  // 10 reels
                .build()
            
            service.registerProduct(chipConfig)
            
            // 컨테이너 적재 계층
            service.registerPackagingHierarchy("IC-555", PackagingHierarchy(
                productId = "IC-555",
                levels = listOf(
                    PackagingLevel("tube", "tube", 25.0, 1.0),
                    PackagingLevel("tray", "tray", 100.0, 1.0),
                    PackagingLevel("reel", "reel", 3000.0, 1.0),
                    PackagingLevel("carton", "carton", 30000.0, 1.0),
                    PackagingLevel("pallet", "pallet", 600000.0, 1.0)  // 20 cartons
                )
            ))
        }
        
        @Test
        @DisplayName("컨테이너 적재 효율성 계산")
        fun `calculate container loading efficiency`() {
            val order = ECU.quantity("2500000 piece")  // 250만개
            val suggestion = service.suggestOptimalPackaging("IC-555", order)
            
            val pallets = suggestion.optimal?.components
                ?.find { it.level.symbol == "pallet" }?.count ?: 0
            
            // 20ft 컨테이너에 10 팔레트 적재 가능
            val containersNeeded = kotlin.math.ceil(pallets / 10.0).toInt()
            val utilizationRate = (pallets % 10) / 10.0 * 100
            
            assertTrue(containersNeeded > 0)
            assertTrue(utilizationRate >= 0)
        }
        
        @Test
        @DisplayName("다양한 포장 형태 혼합 주문")
        fun `mixed packaging format order`() {
            val mixedOrder = listOf(
                ECU.quantity("5 reel"),    // 15,000개
                ECU.quantity("20 tray"),   // 2,000개
                ECU.quantity("50 tube")    // 1,250개
            )
            
            val totalPieces = mixedOrder.sumOf { qty ->
                service.convert("IC-555", qty, "piece").pieces
            }
            
            assertEquals(18250.0, totalPieces)
            
            // 카톤 단위로 환산
            val totalCartons = totalPieces / 30000.0
            assertEquals(0.608, totalCartons, 0.001)
        }
        
        @Test
        @DisplayName("수출 문서용 정확한 수량 계산")
        fun `accurate quantity for export documentation`() {
            val shipment = ECU.quantity("5 carton")
            
            // 각 단위별로 정확한 수량 필요
            val pieces = service.convert("IC-555", shipment, "piece")
            val reels = service.convert("IC-555", shipment, "reel")
            val trays = service.convert("IC-555", shipment, "tray")
            
            assertEquals(150000.0, pieces.pieces)
            assertEquals(50.0, reels.pieces)
            assertEquals(1500.0, trays.pieces)
            
            // 무게 계산 (1000개당 1kg 가정)
            val weightKg = pieces.pieces / 1000.0
            assertEquals(150.0, weightKg)
        }
    }
    
    @Nested
    @DisplayName("가격 계산과 통합")
    inner class PricingIntegrationTest {
        
        @Test
        @DisplayName("단위별 다른 가격 적용")
        fun `apply different prices per unit`() {
            // 음료: 낱개, 6팩, 박스 판매
            val drinkConfig = ProductUnitConfiguration.builder("DRINK-330ML", "can")
                .addConversion("pack", 1.0, 6.0)
                .addConversion("box", 1.0, 24.0)
                .build()
            
            service.registerProduct(drinkConfig)
            
            // 가격 설정 (단위가 클수록 할인)
            val pricePerCan = 1.50
            val pricePerPack = 8.50   // 6 x 1.50 = 9.00 → 8.50 (5.6% 할인)
            val pricePerBox = 32.00    // 24 x 1.50 = 36.00 → 32.00 (11.1% 할인)
            
            // 주문별 가격 계산
            data class Order(val quantity: Quantity, val unitPrice: Double)
            
            val orders = listOf(
                Order(ECU.quantity("10 can"), pricePerCan),
                Order(ECU.quantity("3 pack"), pricePerPack),
                Order(ECU.quantity("2 box"), pricePerBox)
            )
            
            val totalPrice = orders.sumOf { order ->
                val units = when(order.quantity.symbol) {
                    "can" -> order.quantity.pieces
                    "pack" -> order.quantity.pieces / 6.0
                    "box" -> order.quantity.pieces / 24.0
                    else -> 0.0
                }
                units * order.unitPrice
            }
            
            assertEquals(100.50, totalPrice, 0.01)  // 15.00 + 25.50 + 64.00
        }
    }
    
    // 헬퍼 함수
    private fun assertCloseTo(expected: Double, actual: Double, tolerance: Double = 0.01) {
        assertTrue(abs(expected - actual) <= tolerance, 
            "Expected $expected but was $actual (tolerance: $tolerance)")
    }
}
