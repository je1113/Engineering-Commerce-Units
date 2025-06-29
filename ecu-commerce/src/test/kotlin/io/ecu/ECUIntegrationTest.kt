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
    fun `should provide intelligent unit suggestions across categories`() {
        // 다양한 카테고리에서 스마트 제안 테스트
        val suggestions = listOf(
            ECU.Auto.suggest("0.001kg"),    // → g
            ECU.Auto.suggest("0.0005l"),    // → ml
            ECU.Auto.suggest("250K"),       // → °C
            ECU.Auto.suggest("0.005m²"),    // → cm²
            ECU.Auto.suggest("0.1m"),       // → cm
            ECU.Auto.suggest("0.005m/s"),   // → mm/s (매우 느린 속도)
            ECU.Auto.suggest("400m/s")      // → Ma (초음속)
        )
        
        suggestions.forEach { suggestion ->
            assertTrue(suggestion.hasSuggestion(), "Should have suggestion for: ${suggestion.original}")
            assertNotNull(suggestion.suggested)
        }
    }
    
    @Test
    fun `should handle transportation planning scenario`() {
        // 교통 계획 시나리오
        val distances = listOf("150km", "20mi", "50000m")
        val speeds = listOf("60km/h", "35mph", "15m/s")
        
        // 모든 거리를 km로, 모든 속도를 km/h로 변환
        val kmDistances = ECU.Batch.convertLengths(distances, "km")
        val kmhSpeeds = ECU.Batch.convertSpeeds(speeds, "km/h")
        
        assertEquals(150.0, kmDistances[0].value, 0.01)
        assertEquals(32.19, kmDistances[1].value, 0.01) // 20mi ≈ 32.19km
        assertEquals(50.0, kmDistances[2].value, 0.01)  // 50000m = 50km
        
        assertEquals(60.0, kmhSpeeds[0].value, 0.01)
        assertEquals(56.33, kmhSpeeds[1].value, 0.01)   // 35mph ≈ 56.33km/h
        assertEquals(54.0, kmhSpeeds[2].value, 0.01)    // 15m/s = 54km/h
        
        // 여행 시간 계산 (첫 번째 구간)
        val travelTime = kmDistances[0].kilometers / kmhSpeeds[0].kilometersPerHour
        assertEquals(2.5, travelTime, 0.01) // 150km / 60km/h = 2.5시간
    }
    
    @Test
    fun `should handle aviation calculation scenario`() {
        // 항공 계산 시나리오
        val aircraftSpeed = ECU.speed("450kn")  // 일반적인 항공기 순항 속도
        val windSpeed = ECU.speed("25mph")       // 바람 속도
        val altitude = ECU.length("35000ft")     // 순항 고도
        
        // 속도를 같은 단위로 변환
        val windInKnots = windSpeed.to("kn")
        
        // 마하 수 계산
        val machNumber = aircraftSpeed.mach
        assertTrue(machNumber < 1.0) // 아음속
        // 450 knots = 231.499... m/s, 231.499.../343 = 0.6749...
        assertEquals(0.6749265306122448, machNumber, 0.01)
        
        // 고도를 미터로 변환
        val altitudeInM = altitude.to("m")
        assertEquals(10668.0, altitudeInM.value, 1.0)
        
        // 속도 카테고리 확인
        assertEquals(SpeedCategory.VERY_FAST, aircraftSpeed.getSpeedCategory())
        assertEquals(SpeedCategory.MODERATE, windSpeed.getSpeedCategory())
    }
    
    @Test
    fun `should handle automotive engineering scenario`() {
        // 자동차 공학 시나리오
        val carSpeed = ECU.speed("120km/h")
        val carMass = ECU.weight("1500kg")
        val brakingDistance = ECU.length("50m")
        
        // 운동 에너지 계산
        val kineticEnergy = carSpeed.kineticEnergy(carMass.kilograms)
        assertEquals( 833333.66, kineticEnergy, 1.0) // KE = 0.5 * 1500 * (33.33)²
        
        // 제동 시간 계산 (단순화된 모델)
        val brakingTime = carSpeed.timeForDistance(brakingDistance.meters)
        assertEquals(1.5, brakingTime, 0.01) // 50m / 33.33m/s = 1.5초
        
        // 다른 단위로 속도 표시
        val speedInMph = carSpeed.to("mph")
        assertEquals(74.56, speedInMph.value, 0.01)
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
        val speedUnits = ECU.Info.getSupportedSpeedUnits()
        
        assertTrue(lengthUnits.isNotEmpty())
        assertTrue(weightUnits.isNotEmpty())
        assertTrue(volumeUnits.isNotEmpty())
        assertTrue(areaUnits.isNotEmpty())
        assertTrue(speedUnits.isNotEmpty())
        
        // 기본 단위들이 포함되어 있는지 확인
        assertTrue("m" in lengthUnits)
        assertTrue("kg" in weightUnits)
        assertTrue("l" in volumeUnits)
        assertTrue("m²" in areaUnits)
        assertTrue("m/s" in speedUnits)
        
        // 속도 단위 추가 확인
        assertTrue("km/h" in speedUnits)
        assertTrue("mph" in speedUnits)
        assertTrue("kn" in speedUnits)
        assertTrue("ft/s" in speedUnits)
        assertTrue("Ma" in speedUnits)
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
    fun `should handle automotive torque scenario`() {
        // 자동차 토크 시나리오
        val engineTorque = ECU.torque("420 ft-lb") // 고성능 엔진 토크
        val wheelDiameter = ECU.length("0.7m") // 타이어 직경
        
        // SI 단위로 변환
        val torqueNm = engineTorque.to("Nm")
        assertEquals(569.44, torqueNm.value, 0.01)
        
        // 바퀴에서의 힘 계산 (토크 / 반지름)
        val wheelForce = engineTorque.forceAtDistance(wheelDiameter.meters / 2)
        assertEquals(1626.98, wheelForce, 0.02) // N
        
        // 5000 RPM에서의 출력
        val powerWatts = engineTorque.powerAtRPM(5000.0)
        val powerHP = powerWatts / 745.7 // Convert to horsepower
        assertEquals(400.0, powerHP, 1.0) // ~400 HP
        
        // 토크 스케일 확인
        assertEquals(TorqueScale.AUTOMOTIVE, engineTorque.getTorqueScale())
        
        // 자동차 형식으로 표시
        assertEquals("420.0 ft-lb", engineTorque.toAutomotiveFormat())
    }
    
    @Test
    fun `should handle industrial torque wrench calibration`() {
        // 산업용 토크 렌치 교정 시나리오
        val targetTorques = listOf("50 Nm", "100 ft-lb", "500 in-lb", "10 kgf⋅m")
        
        // 모든 토크를 Nm로 변환
        val nmTorques = ECU.Batch.convertTorques(targetTorques, "Nm")
        
        assertEquals(50.0, nmTorques[0].value, 0.01)
        assertEquals(135.58, nmTorques[1].value, 0.01)
        assertEquals(56.49, nmTorques[2].value, 0.01)
        assertEquals(98.07, nmTorques[3].value, 0.01)
        
        // 정밀도 설정
        val preciseTorque = nmTorques[1].withPrecision(1)
        assertEquals("135.6 Nm", preciseTorque.format())
        
        // 엔지니어링 형식 표시
        nmTorques.forEach { torque ->
            val formatted = torque.toEngineeringFormat()
            assertTrue(formatted.contains("N⋅m"))
        }
    }
    
    @Test
    fun `should handle electric motor torque and power calculations`() {
        // 전기 모터 토크 및 전력 계산
        val motorTorque = ECU.torque("2.5 Nm") // 서보 모터
        val motorSpeed = ECU.speed("3000 rpm") // RPM으로 표현된 속도
        
        // 3000 RPM에서의 전력 계산
        val powerWatts = motorTorque.powerAtRPM(3000.0)
        assertEquals(785.4, powerWatts, 0.1) // ~785W
        
        // 작은 토크는 다른 단위로 표시
        val ozfIn = motorTorque.to("ozf⋅in")
        assertEquals(354.02, ozfIn.value, 0.01)
        
        // 스마트 단위 제안
        val suggestion = ECU.Auto.suggest("2.5 Nm")
        assertTrue(suggestion.hasSuggestion())
    }
    
    @Test
    fun `should handle torque-based work calculations`() {
        // 토크 기반 일 계산
        val drillTorque = ECU.torque("50 Nm")
        val rotations = 10.0 // 10 회전
        val angleRadians = rotations * 2 * Math.PI
        
        // 수행된 일 계산
        val workJoules = drillTorque.workOverAngle(angleRadians)
        assertEquals(3141.59, workJoules, 0.01) // ~3.14 kJ
        
        // 에너지로 변환
        val energy = Energy.joules(workJoules)
        val kJ = energy.to("kJ")
        assertEquals(3.14, kJ.value, 0.01)
    }
    
    @Test
    fun `should handle precision torque applications`() {
        // 정밀 토크 응용 (시계, 전자기기 등)
        val watchSpring = ECU.torque("0.05 mNm")
        val electronicScrew = ECU.torque("0.5 ozf⋅in")
        
        // 매우 작은 토크 변환
        val watchInNm = watchSpring.to("Nm")
        assertEquals(0.00005, watchInNm.value, 0.000001)
        
        // 전자기기 나사 토크
        val screwInNm = electronicScrew.to("Nm")
        assertEquals(0.00353, screwInNm.value, 0.00001)
        
        // 토크 스케일 확인
        assertEquals(TorqueScale.MICRO, watchSpring.getTorqueScale())
        assertEquals(TorqueScale.MICRO, electronicScrew.getTorqueScale())
    }
    
    @Test
    fun `should validate all torque units are accessible`() {
        // 모든 토크 단위 접근 가능 확인
        val torqueUnits = ECU.Info.getSupportedTorqueUnits()
        
        assertTrue(torqueUnits.isNotEmpty())
        assertTrue("Nm" in torqueUnits)
        assertTrue("ft-lb" in torqueUnits)
        assertTrue("in-lb" in torqueUnits)
        assertTrue("kNm" in torqueUnits)
        assertTrue("mNm" in torqueUnits)
        assertTrue("kgf⋅m" in torqueUnits)
        assertTrue("ozf⋅in" in torqueUnits)
        assertTrue("dNm" in torqueUnits)
    }
    
    @Test
    fun `should handle energy unit validation`() {
        // 에너지 단위 접근 가능 확인
        val energyUnits = ECU.Info.getSupportedEnergyUnits()
        
        assertTrue(energyUnits.isNotEmpty())
        assertTrue("J" in energyUnits)
        assertTrue("kWh" in energyUnits)
        assertTrue("cal" in energyUnits)
        assertTrue("BTU" in energyUnits)
        assertTrue("eV" in energyUnits)
    }
    
    @Test
    fun `should handle energy consumption scenario`() {
        // 에너지 소비 시나리오
        val dailyUsage = ECU.energy("25 kWh") // 가정 일일 전력 사용량
        val gasHeating = ECU.energy("100000 BTU") // 가스 난방
        
        // 모두 kWh로 변환
        val gasInKwh = gasHeating.to("kWh")
        val totalKwh = dailyUsage.kilowattHours + gasInKwh.kilowattHours
        
        assertEquals(29.31, gasInKwh.value, 0.01)
        assertEquals(54.31, totalKwh, 0.01)
        
        // 월간 비용 계산 (kWh당 $0.12 가정)
        val monthlyCost = totalKwh * 30 * 0.12
        assertEquals(195.516, monthlyCost, 0.02)
    }
}
