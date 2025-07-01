package io.ecu.examples

import io.ecu.*

/**
 * ECU 라이브러리의 기본 사용법을 보여주는 예제
 */
object BasicUsageExample {
    
    /**
     * 기본 단위 변환 예제
     */
    fun basicConversions(): String {
        val results = mutableListOf<String>()
        
        // 길이 변환
        val length = ECU.length("5m").to("ft")
        results.add("길이: 5m → ${length.format()}")
        
        // 무게 변환
        val weight = ECU.weight("10kg").to("lb")
        results.add("무게: 10kg → ${weight.format()}")
        
        // 부피 변환
        val volume = ECU.volume("2l").to("gal")
        results.add("부피: 2l → ${volume.format()}")
        
        // 온도 변환
        val temp = ECU.temperature("25°C").to("°F")
        results.add("온도: 25°C → ${temp.format()}")
        
        // 면적 변환
        val area = ECU.area("100m²").to("ft²")
        results.add("면적: 100m² → ${area.format()}")
        
        return results.joinToString("\n")
    }
    
    /**
     * 정밀도 제어 예제
     */
    fun precisionControl(): String {
        val results = mutableListOf<String>()
        
        // 기본 정밀도
        val basic = ECU.length("1m").to("ft")
        results.add("기본: ${basic.format()}")
        
        // 2자리 정밀도
        val precise = ECU.length("1m").to("ft").withPrecision(2)
        results.add("2자리: ${precise.format()}")
        
        // 반올림 모드 적용
        val rounded = ECU.length("1m").to("ft")
            .withPrecision(1)
            .withRounding(RoundingMode.HALF_UP)
        results.add("반올림: ${rounded.format()}")
        
        return results.joinToString("\n")
    }
    
    /**
     * 수학 연산 예제
     */
    fun mathOperations(): String {
        val results = mutableListOf<String>()
        
        val length1 = ECU.length("5m")
        val length2 = ECU.length("3m")
        
        results.add("덧셈: ${(length1 + length2).format()}")
        results.add("뺄셈: ${(length1 - length2).format()}")
        results.add("곱셈: ${(length1 * 2.0).format()}")
        results.add("나눗셈: ${(length1 / 2.0).format()}")
        results.add("비교: 5m > 3m = ${length1 > length2}")
        
        return results.joinToString("\n")
    }
    
    /**
     * 배치 변환 예제
     */
    fun batchConversions(): String {
        val results = mutableListOf<String>()
        
        // 여러 길이를 미터로 변환
        val lengths = listOf("100cm", "3ft", "1yd")
        val converted = ECU.Batch.convertLengths(lengths, "m")
        
        results.add("배치 길이 변환 (→ m):")
        lengths.zip(converted).forEach { (original, result) ->
            results.add("  $original → ${result.format()}")
        }
        
        return results.joinToString("\n")
    }
    
    /**
     * 실용적인 예제 - 요리 레시피 변환
     */
    fun cookingRecipe(): String {
        val results = mutableListOf<String>()
        
        results.add("=== 한국식 → 미국식 레시피 변환 ===")
        
        val koreanIngredients = mapOf(
            "밀가루" to ECU.weight("500g"),
            "우유" to ECU.volume("250ml"),
            "설탕" to ECU.weight("100g"),
            "버터" to ECU.weight("50g")
        )
        
        koreanIngredients.forEach { (name, unit) ->
            val converted = when (unit) {
                is Weight -> unit.to("oz")
                is Volume -> unit.to("fl oz")
                else -> unit
            }
            results.add("$name: ${unit.format()} → ${converted.format()}")
        }
        
        return results.joinToString("\n")
    }
    
    /**
     * 특별 기능 예제
     */
    fun specialFeatures(): String {
        val results = mutableListOf<String>()
        
        // 온도 특별 기능
        val temp = ECU.temperature("25°C")
        results.add("온도 범주: ${temp.getTemperatureCategory()}")
        results.add("어는점인가: ${ECU.temperature("0°C").isFreezingPoint()}")
        
        // 면적 특별 기능
        val area = ECU.area("100m²")
        results.add("면적 범주: ${area.getAreaCategory()}")
        results.add("정사각형 한 변: ${area.toSquareSide().format()}")
        
        return results.joinToString("\n")
    }
    
    /**
     * 모든 예제 실행
     */
    fun runAllExamples(): String {
        val results = mutableListOf<String>()
        
        results.add("=== ECU 라이브러리 예제 ===\n")
        
        results.add("1. 기본 변환:")
        results.add(basicConversions())
        results.add("")
        
        results.add("2. 정밀도 제어:")
        results.add(precisionControl())
        results.add("")
        
        results.add("3. 수학 연산:")
        results.add(mathOperations())
        results.add("")
        
        results.add("4. 배치 변환:")
        results.add(batchConversions())
        results.add("")
        
        results.add("5. 요리 레시피 변환:")
        results.add(cookingRecipe())
        results.add("")
        
        results.add("6. 특별 기능:")
        results.add(specialFeatures())
        
        return results.joinToString("\n")
    }
}
