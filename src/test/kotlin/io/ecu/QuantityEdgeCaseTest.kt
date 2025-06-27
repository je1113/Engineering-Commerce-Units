package io.ecu

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows

/**
 * 엣지 케이스 및 오류 상황 테스트
 * 실제 비즈니스에서 발생할 수 있는 예외 상황들을 테스트합니다.
 */
class QuantityEdgeCaseTest {
    
    @Nested
    @DisplayName("잘못된 입력 처리")
    inner class InvalidInputTest {
        
        @Test
        @DisplayName("음수 수량 입력 시 오류")
        fun `negative quantity should throw exception`() {
            assertThrows<IllegalArgumentException> {
                ECU.quantity("-5 pieces")
            }
        }
        
        @Test
        @DisplayName("0 또는 음수 포장 크기")
        fun `zero or negative pack size should throw exception`() {
            val qty = Quantity.pieces(100.0)
            
            assertThrows<IllegalArgumentException> {
                qty.toBoxes(0)
            }
            
            assertThrows<IllegalArgumentException> {
                qty.toBoxes(-10)
            }
        }
        
        @Test
        @DisplayName("알 수 없는 단위 변환 시도")
        fun `unknown unit conversion should throw exception`() {
            val qty = Quantity.pieces(100.0)
            
            assertThrows<IllegalArgumentException> {
                qty.to("unknown_unit")
            }
        }
        
        @Test
        @DisplayName("잘못된 형식의 수량 문자열")
        fun `malformed quantity string should throw exception`() {
            assertThrows<IllegalArgumentException> {
                ECU.quantity("pieces 100")  // 숫자가 뒤에 있음
            }
            
            assertThrows<IllegalArgumentException> {
                ECU.quantity("abc pieces")  // 숫자가 아님
            }
            
            assertThrows<IllegalArgumentException> {
                ECU.quantity("")  // 빈 문자열
            }
        }
    }
    
    @Nested
    @DisplayName("극단적인 값 처리")
    inner class ExtremeValueTest {
        
        @Test
        @DisplayName("매우 큰 수량 처리")
        fun `handle very large quantities`() {
            val largeQty = Quantity.pieces(1e9)  // 10억 개
            
            // 변환이 정상적으로 작동하는지
            val millions = largeQty.pieces / 1_000_000
            assertEquals(1000.0, millions)
            
            // 큰 박스 사이즈로 변환
            val megaBoxes = largeQty.toBoxes(10_000)
            assertEquals(100_000.0, megaBoxes.pieces / 10_000)
        }
        
        @Test
        @DisplayName("매우 작은 수량 처리")
        fun `handle very small quantities`() {
            val smallQty = Quantity.pieces(0.001)  // 0.001개
            
            // dozens로 변환
            val dozens = smallQty.dozens
            assertTrue(dozens < 0.0001)
            
            // 정밀도 설정
            val formatted = smallQty.withPrecision(6).format()
            assertEquals("0.001000 pcs", formatted)
        }
        
        @Test
        @DisplayName("0 수량 처리")
        fun `handle zero quantity`() {
            val zero = Quantity.pieces(0.0)
            
            assertEquals(0.0, zero.pieces)
            assertEquals(0.0, zero.dozens)
            assertEquals(0.0, zero.gross)
            
            // 0으로 나누기 방지
            assertThrows<IllegalArgumentException> {
                zero / 0.0
            }
        }
    }
    
    @Nested
    @DisplayName("라운딩 엣지 케이스")
    inner class RoundingEdgeCaseTest {
        
        @Test
        @DisplayName("정확히 0.5일 때 라운딩 모드별 동작")
        fun `rounding behavior at exactly 0_5`() {
            val qty = Quantity.pieces(2.5)
            
            val halfUp = qty.withRounding(RoundingMode.HALF_UP)
                .withPrecision(0).format()
            assertEquals("3.0 pcs", halfUp)
            
            val halfDown = qty.withRounding(RoundingMode.HALF_DOWN)
                .withPrecision(0).format()
            assertEquals("2.0 pcs", halfDown)
            
            val halfEven = qty.withRounding(RoundingMode.HALF_EVEN)
                .withPrecision(0).format()
            assertEquals("2.0 pcs", halfEven)  // 2는 짝수
            
            val qty2 = Quantity.pieces(3.5)
            val halfEven2 = qty2.withRounding(RoundingMode.HALF_EVEN)
                .withPrecision(0).format()
            assertEquals("4.0 pcs", halfEven2)  // 4는 짝수
        }
        
        @Test
        @DisplayName("최소 주문 수량이 포장 단위보다 작을 때")
        fun `MOQ smaller than packaging unit`() {
            val profile = RoundingProfile(
                minimumOrderQuantity = 5.0,
                packagingUnit = 12.0,
                roundingMode = RoundingMode.UP
            )
            
            // 3개 주문 → MOQ 적용 → 5개 → 포장 단위 적용 → 12개
            val adjusted = profile.applyRounding(3.0)
            assertEquals(12.0, adjusted)
        }
        
        @Test
        @DisplayName("분수 허용/비허용 모드")
        fun `fractional vs non-fractional mode`() {
            val noFraction = RoundingProfile(allowFractional = false)
            val withFraction = RoundingProfile(allowFractional = true)
            
            assertEquals(4.0, noFraction.applyRounding(3.7))
            assertEquals(3.7, withFraction.applyRounding(3.7))
            
            assertEquals(3.0, noFraction.applyRounding(3.3))
            assertEquals(3.3, withFraction.applyRounding(3.3))
        }
    }
    
    @Nested
    @DisplayName("제품 설정 충돌 및 오류")
    inner class ConfigurationConflictTest {
        
        @Test
        @DisplayName("동일 제품 ID 중복 등록")
        fun `duplicate product registration should override`() {
            val service = QuantityConversionService()
            
            val config1 = ProductUnitConfiguration.builder("PROD-001", "piece")
                .addConversion("box", 1.0, 10.0)
                .build()
            
            val config2 = ProductUnitConfiguration.builder("PROD-001", "piece")
                .addConversion("box", 1.0, 12.0)  // 다른 환산 비율
                .build()
            
            service.registerProduct(config1)
            service.registerProduct(config2)  // 덮어쓰기
            
            val boxQty = ECU.quantity("1 box")
            val pieces = service.convert("PROD-001", boxQty, "piece")
            
            assertEquals(12.0, pieces.pieces)  // 두 번째 설정이 적용됨
        }
        
        @Test
        @DisplayName("존재하지 않는 제품 변환 시도")
        fun `convert non-existent product should throw exception`() {
            val service = QuantityConversionService()
            val qty = ECU.quantity("10 pieces")
            
            assertThrows<IllegalArgumentException> {
                service.convert("NON-EXISTENT", qty, "box")
            }
        }
        
        @Test
        @DisplayName("순환 참조 환산")
        fun `circular conversion reference`() {
            val config = ProductUnitConfiguration.builder("PROD-001", "piece")
                .addConversion("box", 1.0, 10.0)
                .addConversion("case", 1.0, 100.0)
                .build()
            
            val service = QuantityConversionService()
            service.registerProduct(config)
            
            // piece → box → case → piece 순환 변환
            val qty = ECU.quantity("1 case")
            val pieces = service.convert("PROD-001", qty, "piece")
            val boxes = service.convert("PROD-001", pieces, "box")
            val cases = service.convert("PROD-001", boxes, "case")
            
            assertEquals(1.0, cases.pieces, 0.001)  // 원래 값으로 돌아와야 함
        }
    }
    
    @Nested
    @DisplayName("재고 관리 특수 상황")
    inner class InventorySpecialCaseTest {
        
        @Test
        @DisplayName("정확히 재고량과 같은 주문")
        fun `order exactly matching available stock`() {
            val service = QuantityConversionService()
            
            val config = ProductUnitConfiguration.builder("PROD-001", "piece")
                .addConversion("box", 1.0, 12.0)
                .build()
            
            service.registerProduct(config)
            
            val available = ECU.quantity("144 pieces")
            val requested = ECU.quantity("12 box")  // 정확히 144개
            
            val result = service.checkAvailability("PROD-001", requested, available)
            
            assertTrue(result.canFulfill)
            assertNull(result.shortage)
            assertTrue(result.alternativeOptions.isEmpty())
        }
        
        @Test
        @DisplayName("재고가 0일 때 가용성 확인")
        fun `check availability with zero stock`() {
            val service = QuantityConversionService()
            
            val config = ProductUnitConfiguration.builder("PROD-001", "piece")
                .addConversion("box", 1.0, 12.0)
                .build()
            
            service.registerProduct(config)
            service.registerPackagingHierarchy("PROD-001", 
                PackagingHierarchy.STANDARD_RETAIL)
            
            val available = ECU.quantity("0 pieces")
            val requested = ECU.quantity("1 box")
            
            val result = service.checkAvailability("PROD-001", requested, available)
            
            assertFalse(result.canFulfill)
            assertEquals(12.0, result.shortage?.pieces)
            assertTrue(result.alternativeOptions.isEmpty())  // 대안도 없음
        }
    }
    
    @Nested
    @DisplayName("포장 최적화 특수 케이스")
    inner class PackagingOptimizationEdgeCaseTest {
        
        @Test
        @DisplayName("최소 단위 요구사항을 만족하지 못하는 경우")
        fun `quantity below minimum packaging requirements`() {
            val service = QuantityConversionService()
            
            val hierarchy = PackagingHierarchy(
                productId = "PROD-001",
                levels = listOf(
                    PackagingLevel("piece", "piece", 1.0, 100.0),  // 최소 100개
                    PackagingLevel("box", "box", 100.0, 5.0),      // 최소 5박스
                    PackagingLevel("pallet", "pallet", 2000.0, 1.0)
                )
            )
            
            service.registerPackagingHierarchy("PROD-001", hierarchy)
            
            // 50개 주문 - 최소 요구사항 미달
            val smallOrder = ECU.quantity("50 pieces")
            val suggestion = service.suggestOptimalPackaging("PROD-001", smallOrder)
            
            // 최소 단위 미달로 piece 단위로만 제안
            assertEquals(1, suggestion.optimal?.components?.size)
            assertEquals("piece", suggestion.optimal?.components?.first()?.level?.symbol)
        }
        
        @Test
        @DisplayName("정확히 포장 단위에 맞는 수량")
        fun `quantity exactly matching packaging units`() {
            val service = QuantityConversionService()
            
            val hierarchy = PackagingHierarchy(
                productId = "PROD-001",
                levels = listOf(
                    PackagingLevel("piece", "piece", 1.0, 1.0),
                    PackagingLevel("box", "box", 12.0, 1.0),
                    PackagingLevel("case", "case", 144.0, 1.0)
                )
            )
            
            service.registerPackagingHierarchy("PROD-001", hierarchy)
            
            // 정확히 12 케이스 = 1728개
            val exactOrder = ECU.quantity("1728 pieces")
            val suggestion = service.suggestOptimalPackaging("PROD-001", exactOrder)
            
            val caseCount = suggestion.optimal?.components
                ?.find { it.level.symbol == "case" }?.count
            
            assertEquals(12, caseCount)
            assertEquals(1.0, suggestion.optimal?.efficiency ?: 0.0, 0.1)  // 100% 효율
        }
    }
    
    @Nested
    @DisplayName("실수 연산 정밀도 문제")
    inner class FloatingPointPrecisionTest {
        
        @Test
        @DisplayName("부동소수점 연산 오차 처리")
        fun `handle floating point precision issues`() {
            // 0.1 + 0.2 = 0.3 문제
            val qty1 = Quantity.pieces(0.1)
            val qty2 = Quantity.pieces(0.2)
            val sum = qty1 + qty2
            
            // 정확히 0.3이 아닐 수 있음
            assertTrue(Math.abs(sum.pieces - 0.3) < 1e-10)
            
            // 포맷팅으로 해결
            val formatted = sum.withPrecision(1).format()
            assertEquals("0.3 pcs", formatted)
        }
        
        @Test
        @DisplayName("나누기 연산의 무한 소수")
        fun `handle infinite decimal in division`() {
            val qty = Quantity.pieces(10.0)
            val divided = qty / 3.0  // 3.333...
            
            // 기본 포맷
            val defaultFormat = divided.format()
            assertTrue(defaultFormat.startsWith("3.333"))
            
            // 정밀도 제한
            val limited = divided.withPrecision(2).format()
            assertEquals("3.33 pcs", limited)
        }
        
        @Test
        @DisplayName("매우 작은 차이 비교")
        fun `compare quantities with tiny differences`() {
            val qty1 = Quantity.pieces(1.0000000001)
            val qty2 = Quantity.pieces(1.0)
            
            // equals 메서드는 충분히 가까우면 같다고 판단
            assertEquals(qty1, qty2)  // 1e-10 이내 차이는 무시
        }
    }
    
    @Nested
    @DisplayName("성능 및 메모리 관련")
    inner class PerformanceEdgeCaseTest {
        
        @Test
        @DisplayName("대량 배치 변환 성능")
        fun `batch conversion performance`() {
            val service = QuantityConversionService()
            
            val config = ProductUnitConfiguration.builder("BATCH", "piece")
                .addConversion("box", 1.0, 100.0)
                .build()
            
            service.registerProduct(config)
            
            // 10,000개 주문 배치 처리
            val orders = List(10_000) { i ->
                ECU.quantity("${i + 1} box")
            }
            
            val startTime = System.currentTimeMillis()
            
            val results = orders.map { order ->
                service.convert("BATCH", order, "piece")
            }
            
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            // 성능 확인 (10,000건이 1초 이내)
            assertTrue(duration < 1000, "Batch conversion took ${duration}ms")
            assertEquals(10_000, results.size)
            assertEquals(100.0, results[0].pieces)  // 첫 번째: 1 box = 100 pieces
            assertEquals(1_000_000.0, results[9999].pieces)  // 마지막: 10000 box
        }
    }
}
