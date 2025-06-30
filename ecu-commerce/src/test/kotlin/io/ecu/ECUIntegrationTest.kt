package io.ecu

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * ECU 전체 시스템의 통합 테스트
 */
class ECUIntegrationTest {
    
    @Test
    fun `should perform complex unit conversions across categories`() {
        // 길이 → 면적 (정사각형) → 부피 (정육면체)
        val sideLength = ECU.length("5m")
        val area = Area.squareMeters(sideLength.meters * sideLength.meters)
        val volume = Volume.liters(sideLength.meters * sideLength.meters * sideLength.meters * 1000) // m³ to L: 1m³ = 1000L
        
        assertEquals(25.0, area.squareMeters, 0.001)
        assertEquals(125000.0, volume.liters, 0.001) // 5³ × 1000 = 125000 L
    }
    
    @Test
    fun `should handle multi-unit recipe conversion scenario`() {
        // 요리 레시피 변환 시나리오
        val ingredients = mapOf(
            "flour" to ECU.weight("500g"),
            "milk" to ECU.volume("250ml"),
            "butter" to ECU.weight("100g")
        )
        
        // 미국 단위로 변환
        val usRecipe = ingredients.mapValues { (key, value) ->
            when (value) {
                is Weight -> value.to("oz")
                is Volume -> value.to("fl oz")
                else -> value
            }
        }
        
        assertEquals(17.637, (usRecipe["flour"] as Weight).value, 0.01) // 500g ≈ 17.64 oz
        assertEquals(8.454, (usRecipe["milk"] as Volume).value, 0.01)   // 250ml ≈ 8.45 fl oz
        assertEquals(3.527, (usRecipe["butter"] as Weight).value, 0.01) // 100g ≈ 3.53 oz
    }
    
    @Test
    fun `should handle scientific measurement conversion`() {
        // 과학 실험 시나리오
        val labTemperature = ECU.temperature("23°C")
        val sampleVolume = ECU.volume("50ml")
        val sampleMass = ECU.weight("25.5g")
        
        // 표준 조건으로 변환
        val kelvinTemp = labTemperature.to("K")
        val cubicMeterVolume = sampleVolume.to("m³")
        val kilogramMass = sampleMass.to("kg")
        
        assertEquals(296.15, kelvinTemp.value, 0.01)
        assertEquals(0.00005, cubicMeterVolume.value, 0.0000001)
        assertEquals(0.0255, kilogramMass.value, 0.0001)
        
        // 밀도 계산 (kg/m³)
        val density = kilogramMass.value / cubicMeterVolume.value
        assertEquals(510.0, density, 1.0)
    }
    
    @Test
    fun `should handle real estate calculation scenario`() {
        // 부동산 면적 계산
        val apartmentArea = ECU.area("85m²")
        val gardenArea = ECU.area("200ft²")
        
        // 총 면적 계산 (같은 단위로 변환)
        val gardenInM2 = gardenArea.to("m²")
        val totalArea = apartmentArea + gardenInM2
        
        assertEquals(103.58, totalArea.squareMeters, 0.01) // 85 + 18.58
        assertEquals(AreaCategory.MEDIUM, totalArea.getAreaCategory())
        
        // 평수 계산 (한국 부동산)
        val pyeong = totalArea.squareMeters / 3.306 // 1평 = 3.306m²
        assertEquals(31.3, pyeong, 0.1)
    }
    
    @Test
    fun `should handle weather station data processing`() {
        // 기상대 데이터 처리
        val temperatures = listOf("15°C", "68°F", "291K")
        val rainfalls = listOf("25mm", "1.5in", "0.5cm")
        
        // 모든 온도를 섭씨로 변환
        val celsiusTemps = ECU.Batch.convertTemperatures(temperatures, "°C")
        val avgTemp = celsiusTemps.map { it.celsius }.average()
        
        assertEquals(15.0, celsiusTemps[0].celsius, 0.1)
        assertEquals(20.0, celsiusTemps[1].celsius, 0.1) // 68°F ≈ 20°C
        assertEquals(17.85, celsiusTemps[2].celsius, 0.1) // 291K ≈ 17.85°C
        assertEquals(17.6, avgTemp, 0.1)
        
        // 모든 강수량을 mm로 변환
        val mmRainfalls = ECU.Batch.convertLengths(rainfalls, "mm")
        val totalRainfall = mmRainfalls.map { it.value }.sum()
        
        assertEquals(68.1, totalRainfall, 0.1) // 25 + 38.1 + 5 = 68.1mm (1.5in = 38.1mm)
    }
    
    @Test
    fun `should handle quantity conversions for commerce`() {
        // 상거래 수량 변환 테스트
        val order1 = ECU.quantity("12 dozen")
        val order2 = ECU.quantity("5 gross")
        val order3 = ECU.quantity("2 ream")
        
        assertEquals(144.0, order1.pieces, 0.01)
        assertEquals(720.0, order2.pieces, 0.01)
        assertEquals(1000.0, order3.pieces, 0.01)
        
        // 박스 변환
        val items = ECU.quantity("100 pieces")
        val boxes12 = items.toBoxes(12)
        val boxes24 = items.toBoxes(24)
        
        assertEquals(8.33, boxes12.value, 0.01)
        assertEquals(4.17, boxes24.value, 0.01)
    }
    
    @Test
    fun `should handle precision and rounding consistently`() {
        // 정밀도와 반올림 일관성 테스트
        val length = ECU.length("1m").withPrecision(3).withRounding(RoundingMode.HALF_UP)
        val weight = ECU.weight("1kg").withPrecision(3).withRounding(RoundingMode.HALF_UP)
        val volume = ECU.volume("1l").withPrecision(3).withRounding(RoundingMode.HALF_UP)
        
        val convertedLength = length.to("ft")
        val convertedWeight = weight.to("lb")
        val convertedVolume = volume.to("gal")
        
        assertEquals("3.281 ft", convertedLength.format())
        assertEquals("2.205 lb", convertedWeight.format())
        assertEquals("0.264 gal", convertedVolume.format())
    }
    
    @Test
    fun `should validate all supported units are accessible`() {
        // 모든 지원 단위가 접근 가능한지 확인
        val lengthUnits = ECU.Info.getSupportedLengthUnits()
        val weightUnits = ECU.Info.getSupportedWeightUnits()
        val volumeUnits = ECU.Info.getSupportedVolumeUnits()
        val areaUnits = ECU.Info.getSupportedAreaUnits()
        
        assertTrue(lengthUnits.isNotEmpty())
        assertTrue(weightUnits.isNotEmpty())
        assertTrue(volumeUnits.isNotEmpty())
        assertTrue(areaUnits.isNotEmpty())
        
        // 기본 단위들이 포함되어 있는지 확인
        assertTrue("m" in lengthUnits)
        assertTrue("kg" in weightUnits)
        assertTrue("l" in volumeUnits)
        assertTrue("m²" in areaUnits)
    }
    
    @Test
    fun `should handle edge cases gracefully`() {
        // 극값 및 특수 케이스 처리
        val verySmall = ECU.length("1e-10m")
        val veryLarge = ECU.weight("1e10kg")
        val negative = ECU.temperature("-50°C")
        
        assertTrue(verySmall.isValid())
        assertTrue(veryLarge.isValid())
        assertTrue(negative.isValid())
        
        // 절대영도 테스트
        val absoluteZero = ECU.temperature("0K")
        assertTrue(absoluteZero.isAboveAbsoluteZero())
        
        val belowAbsoluteZero = ECU.temperature("-1K")
        assertTrue(!belowAbsoluteZero.isAboveAbsoluteZero())
    }
    
    @Test
    fun `should perform complex chaining operations`() {
        // 복잡한 체이닝 연산 테스트
        val result = ECU.length("100cm")
            .to("m")
            .withPrecision(1)
            .withRounding(RoundingMode.HALF_UP)
        
        assertEquals("1.0 m", result.format())
        
        // 연산 후 체이닝
        val calculated = (ECU.length("5m") + ECU.length("3m"))
            .to("ft")
            .withPrecision(0)
        
        assertEquals("26 ft", calculated.format())
    }
    
    @Test
    fun `should maintain type safety across operations`() {
        // 타입 안전성 검증
        val length = ECU.length("5m")
        val weight = ECU.weight("3kg")
        
        // 같은 타입끼리만 연산 가능
        val lengthSum = length + ECU.length("2m")
        val weightSum = weight + ECU.weight("1kg")
        
        assertEquals(7.0, lengthSum.meters, 0.001)
        assertEquals(4.0, weightSum.kilograms, 0.001)
        
        // 비교도 같은 타입끼리만 가능
        assertTrue(length > ECU.length("3m"))
        assertTrue(weight > ECU.weight("2kg"))
    }
    
    @Test
    fun `should handle commerce-specific scenarios`() {
        // 상거래 특화 시나리오
        val service = QuantityConversionService()
        
        // 제품 설정
        val productConfig = ProductUnitConfiguration.builder("PROD-001", "piece")
            .addConversion("box", 1.0, 12.0)
            .addConversion("case", 1.0, 144.0)
            .build()
        
        service.registerProduct(productConfig)
        
        // 포장 계층 등록
        service.registerPackagingHierarchy("PROD-001", PackagingHierarchy(
            productId = "PROD-001",
            levels = listOf(
                PackagingLevel("piece", "piece", 1.0, 1.0),
                PackagingLevel("box", "box", 12.0, 1.0),
                PackagingLevel("case", "case", 144.0, 1.0)
            )
        ))
        
        // 최적 포장 제안
        val order = ECU.quantity("250 pieces")
        val suggestion = service.suggestOptimalPackaging("PROD-001", order)
        
        assertNotNull(suggestion.optimal)
        assertTrue(suggestion.optimal!!.components.isNotEmpty())
        assertTrue(suggestion.optimal!!.efficiency > 0.5)
    }
    
    @Test
    fun `should handle quantity formatting options`() {
        // 수량 포맷팅 옵션 테스트
        val quantity = ECU.quantity("1234.56 pieces")
        val formatter = QuantityFormatter(
            locale = QuantityFormatter.Locale.US,
            options = QuantityFormatter.FormattingOptions(
                useThousandsSeparator = true,
                decimalPlaces = 2
            )
        )
        
        val formatted = formatter.format(quantity)
        assertTrue(formatted.contains("1,234.56"))
        
        // EU 형식
        val euFormatter = QuantityFormatter(
            locale = QuantityFormatter.Locale.EU,
            options = QuantityFormatter.FormattingOptions(
                useThousandsSeparator = true,
                decimalPlaces = 2
            )
        )
        
        val euFormatted = euFormatter.format(quantity)
        assertTrue(euFormatted.contains("1.234,56"))
    }
}
