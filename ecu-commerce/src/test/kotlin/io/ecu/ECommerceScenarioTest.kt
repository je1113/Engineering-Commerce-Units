package io.ecu

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.math.round

/**
 * 실제 이커머스 비즈니스 시나리오 테스트
 * 
 * 온라인 쇼핑몰, 도매업체, 제조업체에서 실제로 발생하는
 * 복잡한 단위 변환 및 수량 계산 시나리오를 테스트합니다.
 */
class ECommerceScenarioTest {

    @Test
    fun `online shopping mall - minimum order quantity validation`() {
        // 시나리오: B2B 쇼핑몰에서 최소 주문 수량 체크
        val minimumOrder = ECU.quantity("2 dozen") // 최소 2다스
        val customerOrder = ECU.quantity("20 pieces")
        
        assertEquals(24.0, minimumOrder.pieces, 0.01)
        assertEquals(20.0, customerOrder.pieces, 0.01)
        
        val isOrderValid = customerOrder.pieces >= minimumOrder.pieces
        assertEquals(false, isOrderValid)
        
        // 최소 주문량 충족을 위한 추가 수량 계산
        val additionalNeeded = minimumOrder.pieces - customerOrder.pieces
        assertEquals(4.0, additionalNeeded, 0.01)
        
        val suggestedOrder = ECU.quantity("${minimumOrder.pieces} pieces")
        assertEquals(24.0, suggestedOrder.pieces, 0.01)
    }

    @Test
    fun `wholesale business - tiered discount application`() {
        // 시나리오: 수량별 계층 할인 시스템
        val orderQuantity = ECU.quantity("250 pieces")
        
        data class PriceTier(
            val minQuantity: Double,
            val maxQuantity: Double?,
            val pricePerUnit: Double,
            val description: String
        )
        
        val priceTiers = listOf(
            PriceTier(1.0, 99.0, 10.0, "소매"),
            PriceTier(100.0, 199.0, 8.5, "소량 도매"),
            PriceTier(200.0, 499.0, 7.0, "일반 도매"),
            PriceTier(500.0, null, 6.0, "대량 도매")
        )
        
        val applicableTier = priceTiers.find { tier ->
            orderQuantity.pieces >= tier.minQuantity && 
            (tier.maxQuantity == null || orderQuantity.pieces <= tier.maxQuantity)
        }
        
        assertNotNull(applicableTier)
        assertEquals("일반 도매", applicableTier.description)
        assertEquals(7.0, applicableTier.pricePerUnit, 0.01)
        
        val totalCost = orderQuantity.pieces * applicableTier.pricePerUnit
        assertEquals(1750.0, totalCost, 0.01)
    }

    @Test
    fun `manufacturing - production batch planning`() {
        // 시나리오: 주문량 기반 생산 배치 크기 결정
        val monthlyOrders = listOf(
            ECU.quantity("1200 pieces"),
            ECU.quantity("800 pieces"),
            ECU.quantity("1500 pieces"),
            ECU.quantity("600 pieces")
        )
        
        val totalDemand = monthlyOrders.reduce { acc, order -> acc + order }
        assertEquals(4100.0, totalDemand.pieces, 0.01)
        
        // 생산 배치 크기: 1000개 단위로 생산
        val batchSize = 1000
        val batchesNeeded = kotlin.math.ceil(totalDemand.pieces / batchSize).toInt()
        val totalProduction = batchesNeeded * batchSize
        val overproduction = totalProduction - totalDemand.pieces
        
        assertEquals(5, batchesNeeded)
        assertEquals(5000, totalProduction)
        assertEquals(900.0, overproduction, 0.01)
        
        // 과잉 생산률
        val overproductionRate = (overproduction / totalDemand.pieces) * 100
        assertEquals(21.95, overproductionRate, 0.01)
    }

    @Test
    fun `warehouse - picking optimization`() {
        // 시나리오: 창고에서 주문 피킹 효율성 계산
        data class PickingOrder(
            val orderId: String,
            val quantity: Quantity,
            val pickingBoxSize: Int
        )
        
        val pickingOrders = listOf(
            PickingOrder("ORD-001", ECU.quantity("45 pieces"), 12),
            PickingOrder("ORD-002", ECU.quantity("78 pieces"), 24),
            PickingOrder("ORD-003", ECU.quantity("156 pieces"), 36)
        )
        
        // 각 주문별 피킹 효율성 계산
        val pickingEfficiency = pickingOrders.map { order ->
            val fullBoxes = (order.quantity.pieces / order.pickingBoxSize).toInt()
            val remainingItems = order.quantity.pieces - (fullBoxes * order.pickingBoxSize)
            val pickingStops = fullBoxes + if (remainingItems > 0) 1 else 0
            
            Triple(order.orderId, pickingStops, remainingItems)
        }
        
        // ORD-001: 45개 = 3박스 + 9개 = 4스톱
        assertEquals(4, pickingEfficiency[0].second)
        assertEquals(9.0, pickingEfficiency[0].third, 0.01)
        
        // ORD-002: 78개 = 3박스 + 6개 = 4스톱  
        assertEquals(4, pickingEfficiency[1].second)
        assertEquals(6.0, pickingEfficiency[1].third, 0.01)
        
        // ORD-003: 156개 = 4박스 + 12개 = 5스톱
        assertEquals(5, pickingEfficiency[2].second)
        assertEquals(12.0, pickingEfficiency[2].third, 0.01)
    }

    @Test
    fun `subscription service - regular delivery quantity adjustment`() {
        // 시나리오: 월간 구독 박스 서비스
        val baseSubscription = ECU.quantity("30 pieces") // 월 기본량
        val usageHistory = listOf(0.8, 1.2, 0.9, 1.1, 1.05) // 사용률 히스토리
        
        // 평균 사용률 계산
        val averageUsage = usageHistory.average()
        assertEquals(1.01, averageUsage, 0.01)
        
        // 다음 달 추천 수량
        val recommendedQuantity = baseSubscription.pieces * averageUsage
        val adjustedSubscription = ECU.quantity("${recommendedQuantity} pieces")
        
        assertEquals(30.3, adjustedSubscription.pieces, 0.01)
        
        // 가장 가까운 포장 단위로 조정 (6개 단위)
        val packagingUnit = 6
        val finalQuantity = round(recommendedQuantity / packagingUnit) * packagingUnit
        assertEquals(30.0, finalQuantity, 0.01) // 30.3 → 30 (5팩)
    }

    @Test
    fun `cross docking - immediate shipment processing`() {
        // 시나리오: 물류센터에서 크로스 도킹 처리
        val incomingShipment = ECU.quantity("1000 pieces")
        
        val outgoingOrders = listOf(
            ECU.quantity("150 pieces"),
            ECU.quantity("300 pieces"),
            ECU.quantity("200 pieces"),
            ECU.quantity("250 pieces")
        )
        
        val totalOutgoing = outgoingOrders.reduce { acc, order -> acc + order }
        assertEquals(900.0, totalOutgoing.pieces, 0.01)
        
        val remainingInventory = incomingShipment - totalOutgoing
        assertEquals(100.0, remainingInventory.pieces, 0.01)
        
        // 크로스 도킹 효율성 (즉시 출고 비율)
        val crossDockingRate = (totalOutgoing.pieces / incomingShipment.pieces) * 100
        assertEquals(90.0, crossDockingRate, 0.01)
    }

    @Test
    fun `return processing - inventory recovery`() {
        // 시나리오: 고객 반품 처리 및 재고 복구
        val originalOrder = ECU.quantity("120 pieces")
        val returnedItems = ECU.quantity("8 pieces")
        val damagedItems = ECU.quantity("2 pieces")
        
        val restockableItems = returnedItems - damagedItems
        assertEquals(6.0, restockableItems.pieces, 0.01)
        
        // 재고 복구율
        val restockRate = (restockableItems.pieces / returnedItems.pieces) * 100
        assertEquals(75.0, restockRate, 0.01)
        
        // 손실률
        val lossRate = (damagedItems.pieces / originalOrder.pieces) * 100
        assertEquals(1.67, lossRate, 0.01)
    }

    @Test
    fun `seasonal inventory management - demand forecast based ordering`() {
        // 시나리오: 시즌 상품 수요 예측 및 발주 계획
        val historicalDemand = listOf(
            ECU.quantity("500 pieces"),  // 작년 동기
            ECU.quantity("650 pieces"),  // 전전월
            ECU.quantity("800 pieces")   // 전월
        )
        
        // 성장률 계산 (정확한 계산)
        val growthRate1 = historicalDemand[1].pieces / historicalDemand[0].pieces // 650/500 = 1.3
        val growthRate2 = historicalDemand[2].pieces / historicalDemand[1].pieces // 800/650 = 1.2307...
        
        val averageGrowthRate = (growthRate1 + growthRate2) / 2
        assertEquals(1.265, averageGrowthRate, 0.01) // (1.3 + 1.2307) / 2 = 1.265...
        
        // 다음 달 예상 수요
        val predictedDemand = historicalDemand.last().pieces * averageGrowthRate
        assertEquals(1012.31, predictedDemand, 0.01) // 800 * 1.265 = 1012.31...
        
        // 안전 재고 (20% 추가)
        val safetyStock = predictedDemand * 0.2
        val totalOrderQuantity = predictedDemand + safetyStock
        
        assertEquals(1214.77, totalOrderQuantity, 0.01) // 1012.31 * 1.2 = 1214.77...
        
        // 공급업체 최소 주문 단위 (100개)로 반올림
        val supplierMOQ = 100
        val finalOrderQuantity = kotlin.math.ceil(totalOrderQuantity / supplierMOQ) * supplierMOQ
        assertEquals(1300.0, finalOrderQuantity, 0.01)
    }

    @Test
    fun `B2B bulk orders - shipping cost optimization`() {
        // 시나리오: 배송비 절약을 위한 주문량 최적화
        val customerOrder = ECU.quantity("2800 pieces")
        
        data class ShippingTier(
            val minWeight: Double,
            val maxWeight: Double?,
            val cost: Double,
            val description: String
        )
        
        // 1개당 100g으로 가정
        val itemWeight = 0.1 // kg
        val totalWeight = customerOrder.pieces * itemWeight
        assertEquals(280.0, totalWeight, 0.01)
        
        val shippingTiers = listOf(
            ShippingTier(0.0, 100.0, 15000.0, "소형"),
            ShippingTier(100.0, 300.0, 25000.0, "중형"),
            ShippingTier(300.0, 500.0, 35000.0, "대형"),
            ShippingTier(500.0, null, 45000.0, "특대형")
        )
        
        val currentTier = shippingTiers.find { tier ->
            totalWeight >= tier.minWeight && 
            (tier.maxWeight == null || totalWeight <= tier.maxWeight)
        }
        
        assertNotNull(currentTier)
        assertEquals("중형", currentTier.description)
        assertEquals(25000.0, currentTier.cost, 0.01)
        
        // 다음 단계까지 추가 주문으로 배송비 효율성 확인
        val nextTierMinWeight = 300.0
        val additionalWeightNeeded = nextTierMinWeight - totalWeight
        val additionalItemsNeeded = kotlin.math.ceil(additionalWeightNeeded / itemWeight)
        
        assertEquals(20.0, additionalWeightNeeded, 0.01)
        assertEquals(200.0, additionalItemsNeeded, 0.01)
        
        // 배송비 효율성 비교
        val currentCostPerKg = currentTier.cost / totalWeight
        val nextTierCostPerKg = 35000.0 / nextTierMinWeight
        
        assertEquals(89.29, currentCostPerKg, 0.01)
        assertEquals(116.67, nextTierCostPerKg, 0.01)
        
        // 현재가 더 효율적
        assertTrue(currentCostPerKg < nextTierCostPerKg)
    }
}
