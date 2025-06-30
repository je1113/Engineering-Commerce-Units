package io.ecu

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * 실제 커머스에서 활용 가능한 단위 변환 테스트
 * 
 * 실무에서 자주 발생하는 시나리오들을 기반으로 한 테스트입니다:
 * - 재고 관리 (개수 ↔ 박스 ↔ 팔레트)
 * - 주문 수량 최적화
 * - 배송 단위 계산
 * - 가격 계산을 위한 단위 변환
 */
class CommerceQuantityTest {

    @Test
    fun `inventory management - convert individual items to box units`() {
        // 시나리오: 1박스 = 12개 상품
        val individualItems = ECU.quantity("250 pieces")
        val boxes = individualItems.toBoxes(12)
        
        // 250개 = 20박스 + 10개
        assertEquals(20.833, boxes.value, 0.01)
        
        // 완전한 박스만 계산
        val completeBoxes = (individualItems.pieces / 12).toInt()
        val remainingItems = individualItems.pieces - (completeBoxes * 12)
        
        assertEquals(20, completeBoxes)
        assertEquals(10.0, remainingItems, 0.01)
    }

    @Test
    fun `order quantity optimization - dozen unit orders`() {
        // 시나리오: 베이커리에서 머핀 주문 (1다스 = 12개)
        val customerOrder = ECU.quantity("50 pieces")
        val dozenQuantity = customerOrder.to("dozen")
        
        assertEquals(4.167, dozenQuantity.value, 0.01)
        
        // 완전한 다스로 주문 권장
        val recommendedDozens = kotlin.math.ceil(dozenQuantity.value).toInt()
        val recommendedPieces = ECU.quantity("$recommendedDozens dozen")
        
        assertEquals(5, recommendedDozens)
        assertEquals(60.0, recommendedPieces.pieces, 0.01)
    }

    @Test
    fun `shipping unit calculation - pallet optimization`() {
        // 시나리오: 1팔레트 = 48박스, 1박스 = 24개
        val orderQuantity = ECU.quantity("2500 pieces")
        
        val boxSize = 24
        val palletCapacity = 48 // 박스 단위
        
        val totalBoxes = kotlin.math.ceil(orderQuantity.pieces / boxSize).toInt()
        val palletsNeeded = kotlin.math.ceil(totalBoxes.toDouble() / palletCapacity).toInt()
        val totalCapacity = palletsNeeded * palletCapacity * boxSize
        
        assertEquals(105, totalBoxes) // 2500 ÷ 24 = 104.17 → 105박스
        assertEquals(3, palletsNeeded) // 105 ÷ 48 = 2.19 → 3팔레트
        assertEquals(3456, totalCapacity) // 3 × 48 × 24 = 3456개
        
        val wastedSpace = totalCapacity - orderQuantity.pieces
        assertEquals(956.0, wastedSpace, 0.01)
    }

    @Test
    fun `price calculation - unit-based pricing`() {
        // 시나리오: 개별 가격 vs 박스 가격 vs 대량 할인
        val order = ECU.quantity("156 pieces")
        
        val unitPrice = 1.5 // 개당 1.5원
        val boxPrice = 15.0 // 박스당 15원 (12개, 17% 할인)
        val boxSize = 12
        
        // 개별 구매
        val individualCost = order.pieces * unitPrice
        
        // 박스 단위 구매
        val fullBoxes = (order.pieces / boxSize).toInt()
        val remainingItems = order.pieces - (fullBoxes * boxSize)
        val boxCost = (fullBoxes * boxPrice) + (remainingItems * unitPrice)
        
        assertEquals(234.0, individualCost, 0.01)
        // 156개 = 13박스 × 12개 = 156개 (나머지 0개)
        assertEquals(13, fullBoxes)
        assertEquals(0.0, remainingItems, 0.01)
        assertEquals(195.0, boxCost, 0.01) // 13박스 × 15원 = 195원
        
        val savings = individualCost - boxCost
        assertEquals(39.0, savings, 0.01) // 234 - 195 = 39원 절약
    }

    @Test
    fun `inventory transaction management - inbound and outbound`() {
        // 시나리오: 창고 재고 관리
        var currentStock = ECU.quantity("500 pieces")
        
        // 입고: 5박스 (1박스 = 20개)
        val incomingBoxes = 5
        val boxSize = 20
        val incomingPieces = ECU.quantity("${incomingBoxes * boxSize} pieces")
        currentStock = currentStock + incomingPieces
        
        assertEquals(600.0, currentStock.pieces, 0.01) // 500 + 100 = 600
        
        // 출고: 고객 주문 처리
        val order1 = ECU.quantity("150 pieces")
        val order2 = ECU.quantity("75 pieces")
        
        currentStock = currentStock - order1 - order2
        assertEquals(375.0, currentStock.pieces, 0.01) // 600 - 150 - 75 = 375
        
        // 남은 재고를 박스 단위로 표현
        val remainingBoxes = currentStock.toBoxes(boxSize)
        assertEquals(18.75, remainingBoxes.value, 0.01) // 375 ÷ 20 = 18.75
    }

    @Test
    fun `packaging calculation - various packaging options`() {
        // 시나리오: 이커머스 포장 최적화
        val orderItems = ECU.quantity("47 pieces")
        
        // 포장 옵션들
        val smallBox = 6   // 소형 박스
        val mediumBox = 12 // 중형 박스  
        val largeBox = 24  // 대형 박스
        
        // 각 포장 옵션별 필요 박스 수와 낭비 계산
        data class PackagingOption(
            val boxSize: Int,
            val boxesNeeded: Int,
            val totalCapacity: Int,
            val wastedSpace: Int
        )
        
        val options = listOf(smallBox, mediumBox, largeBox).map { size ->
            val needed = kotlin.math.ceil(orderItems.pieces / size).toInt()
            val capacity = needed * size
            val waste = capacity - orderItems.pieces.toInt()
            
            PackagingOption(size, needed, capacity, waste)
        }
        
        // 각 옵션 검증
        assertEquals(8, options[0].boxesNeeded) // 47 ÷ 6 = 7.83 → 8박스
        assertEquals(48, options[0].totalCapacity) // 8 × 6 = 48
        assertEquals(1, options[0].wastedSpace) // 48 - 47 = 1
        
        assertEquals(4, options[1].boxesNeeded) // 47 ÷ 12 = 3.92 → 4박스
        assertEquals(48, options[1].totalCapacity) // 4 × 12 = 48
        assertEquals(1, options[1].wastedSpace) // 48 - 47 = 1
        
        assertEquals(2, options[2].boxesNeeded) // 47 ÷ 24 = 1.96 → 2박스
        assertEquals(48, options[2].totalCapacity) // 2 × 24 = 48
        assertEquals(1, options[2].wastedSpace) // 48 - 47 = 1
        
        // 최소 낭비 옵션 찾기 (모두 같으므로 첫 번째)
        val optimal = options.minByOrNull { it.wastedSpace }
        assertNotNull(optimal)
        assertEquals(smallBox, optimal.boxSize) // 가장 작은 박스가 선택됨
    }

    @Test
    fun `B2B bulk orders - gross unit utilization`() {
        // 시나리오: B2B 대량 주문 (1 gross = 144개)
        val bulkOrder = ECU.quantity("10 gross")
        
        assertEquals(1440.0, bulkOrder.pieces, 0.01)
        
        // 배송 효율성을 위한 박스 변환 (1박스 = 36개)
        val shippingBoxes = bulkOrder.toBoxes(36)
        assertEquals(40.0, shippingBoxes.value, 0.01)
        
        // 고객사 재포장 단위 (1고객박스 = 12개)
        val customerBoxes = bulkOrder.toBoxes(12)
        assertEquals(120.0, customerBoxes.value, 0.01)
    }

    @Test
    fun `seasonal product management - ream units for paper products`() {
        // 시나리오: 사무용품 주문 (1 ream = 500장)
        val paperOrder = ECU.quantity("3.5 ream")
        
        assertEquals(1750.0, paperOrder.pieces, 0.01)
        
        // 고객 요청: 소량 패키지로 분할 (1패키지 = 100장)
        val smallPackages = paperOrder.toBoxes(100)
        assertEquals(17.5, smallPackages.value, 0.01)
        
        // 실제 배송은 완전한 패키지만
        val completePackages = smallPackages.value.toInt()
        val remainingSheets = paperOrder.pieces - (completePackages * 100)
        
        assertEquals(17, completePackages)
        assertEquals(50.0, remainingSheets, 0.01)
    }

    @Test
    fun `quantity formatting - actual business display`() {
        // 비즈니스에서 실제 사용하는 단순한 수량 표시
        val quantity = ECU.quantity("1234.56 pieces")
        
        val formatter = QuantityFormatter(
            locale = QuantityFormatter.Locale.US,
            options = QuantityFormatter.FormattingOptions(
                useThousandsSeparator = true,
                decimalPlaces = 2,
                useCompoundUnits = false, // 복합 단위 비활성화
                unitStyle = QuantityFormatter.UnitStyle.ABBREVIATED
            )
        )
        
        val formatted = formatter.format(quantity)
        
        // 간단한 형식이어야 함: "1,234.56 pcs"
        assertTrue(formatted.contains("1,234.56"))
        assertTrue(formatted.contains("pcs"))
        
        // EU 형식 테스트
        val euFormatter = QuantityFormatter(
            locale = QuantityFormatter.Locale.EU,
            options = QuantityFormatter.FormattingOptions(
                useThousandsSeparator = true,
                decimalPlaces = 2,
                useCompoundUnits = false,
                unitStyle = QuantityFormatter.UnitStyle.ABBREVIATED
            )
        )
        
        val euFormatted = euFormatter.format(quantity)
        assertTrue(euFormatted.contains("1.234,56"))
        assertTrue(euFormatted.contains("pcs"))
    }

    @Test
    fun `inventory turnover calculation`() {
        // 시나리오: 월별 재고 회전율 분석
        val monthlyData = listOf(
            "1500 pieces",  // 1월 재고
            "1200 pieces",  // 2월 재고
            "1800 pieces",  // 3월 재고
            "1100 pieces"   // 4월 재고
        ).map { ECU.quantity(it) }
        
        val averageStock = monthlyData.map { it.pieces }.average()
        assertEquals(1400.0, averageStock, 0.01)
        
        // 분기 판매량
        val quarterlySales = ECU.quantity("4500 pieces")
        val turnoverRate = quarterlySales.pieces / averageStock
        
        assertEquals(3.21, turnoverRate, 0.01)
    }

    @Test
    fun `global commerce - multi-country unit conversion`() {
        // 시나리오: 글로벌 이커머스 단위 표준화
        val productQuantity = ECU.quantity("100 pieces")
        
        // 미국: dozen 단위 선호
        val usFormat = productQuantity.to("dozen")
        assertEquals(8.33, usFormat.value, 0.01)
        
        // 유럽: 일반적으로 10개 단위 선호
        val europePackaging = productQuantity.toBoxes(10)
        assertEquals(10.0, europePackaging.value, 0.01)
        
        // 아시아: 다양한 포장 단위
        val asiaSmallPack = productQuantity.toBoxes(5)  // 소형
        val asiaMediumPack = productQuantity.toBoxes(20) // 중형
        
        assertEquals(20.0, asiaSmallPack.value, 0.01)
        assertEquals(5.0, asiaMediumPack.value, 0.01)
    }

    @Test
    fun `promotion quantity calculation`() {
        // 시나리오: "3개 사면 1개 무료" 프로모션
        val customerOrder = ECU.quantity("10 pieces")
        
        val promoRatio = 4 // 3+1
        val promoSets = (customerOrder.pieces / promoRatio).toInt()
        val regularItems = customerOrder.pieces - (promoSets * promoRatio)
        val freeItems = promoSets.toDouble()
        val totalItems = customerOrder.pieces + freeItems
        
        assertEquals(2, promoSets)      // 2세트 프로모션 적용
        assertEquals(2.0, regularItems, 0.01) // 일반 구매 2개
        assertEquals(2.0, freeItems, 0.01)     // 무료 2개
        assertEquals(12.0, totalItems, 0.01)   // 총 12개 제공
    }
}
