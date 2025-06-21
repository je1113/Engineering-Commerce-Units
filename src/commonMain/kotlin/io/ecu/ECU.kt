package io.ecu

/**
 * Engineering Commerce Units의 메인 진입점
 * 
 * 유창한 API를 제공하여 직관적인 단위 변환을 가능하게 합니다.
 */
object ECU {
    
    /**
     * 길이 단위 변환을 위한 진입점
     * 
     * @param input "5m", "10.5ft", "100cm" 등의 형식
     * @return Length 객체
     * 
     * @example
     * ```kotlin
     * val length = ECU.length("5m").to("ft")  // 16.40 ft
     * ```
     */
    fun length(input: String): Length {
        return Length.parse(input)
    }
    
    /**
     * 무게 단위 변환을 위한 진입점
     * 
     * @param input "5kg", "10.5lb", "100g" 등의 형식
     * @return Weight 객체
     * 
     * @example
     * ```kotlin
     * val weight = ECU.weight("5kg").to("lbs")  // 11.02 lbs
     * ```
     */
    fun weight(input: String): Weight {
        return Weight.parse(input)
    }
    
    /**
     * 단위 변환 검증 및 제안 시스템
     */
    object Auto {
        /**
         * 더 적절한 단위 제안
         * 
         * @param input 현재 단위
         * @return 제안 결과
         */
        fun suggest(input: String): UnitSuggestion {
            try {
                // 길이 단위 체크
                val length = length(input)
                return suggestBetterLengthUnit(length)
            } catch (e: Exception) {
                // 다른 단위 타입들도 체크할 예정
            }
            
            try {
                // 무게 단위 체크
                val weight = weight(input)
                return suggestBetterWeightUnit(weight)
            } catch (e: Exception) {
                // 무시
            }
            
            return UnitSuggestion(input, null, "No suggestion available")
        }
        
        private fun suggestBetterLengthUnit(length: Length): UnitSuggestion {
            val meters = length.meters
            
            return when {
                meters < 0.001 -> {
                    UnitSuggestion(
                        original = length.toString(),
                        suggested = Length.of(meters * 1_000_000, "mm").format(),
                        reason = "Consider using millimeters for very small lengths"
                    )
                }
                meters < 0.01 -> {
                    UnitSuggestion(
                        original = length.toString(),
                        suggested = Length.of(meters * 100, "cm").format(),
                        reason = "Consider using centimeters for small lengths"
                    )
                }
                meters > 1000 -> {
                    UnitSuggestion(
                        original = length.toString(),
                        suggested = Length.of(meters / 1000, "km").format(),
                        reason = "Consider using kilometers for large distances"
                    )
                }
                else -> UnitSuggestion(length.toString(), null, "Current unit is appropriate")
            }
        }
        
        private fun suggestBetterWeightUnit(weight: Weight): UnitSuggestion {
            val kg = weight.kilograms
            
            return when {
                kg < 0.001 -> {
                    UnitSuggestion(
                        original = weight.toString(),
                        suggested = Weight.of(kg * 1_000_000, "mg").format(),
                        reason = "Consider using milligrams for very light weights"
                    )
                }
                kg < 1.0 -> {
                    UnitSuggestion(
                        original = weight.toString(),
                        suggested = Weight.of(kg * 1000, "g").format(),
                        reason = "Consider using grams for light weights"
                    )
                }
                kg > 1000 -> {
                    UnitSuggestion(
                        original = weight.toString(),
                        suggested = Weight.of(kg / 1000, "t").format(),
                        reason = "Consider using metric tons for heavy weights"
                    )
                }
                else -> UnitSuggestion(weight.toString(), null, "Current unit is appropriate")
            }
        }
    }
    
    /**
     * 배치 변환 시스템
     */
    object Batch {
        /**
         * 여러 값을 동일한 단위로 변환
         * 
         * @param inputs 변환할 값들
         * @param targetUnit 목표 단위
         * @return 변환된 값들
         */
        fun convertLengths(inputs: List<String>, targetUnit: String): List<Length> {
            return inputs.map { input ->
                length(input).to(targetUnit)
            }
        }
        
        /**
         * 여러 무게를 동일한 단위로 변환
         */
        fun convertWeights(inputs: List<String>, targetUnit: String): List<Weight> {
            return inputs.map { input ->
                weight(input).to(targetUnit)
            }
        }
    }
    
    /**
     * 단위 정보 조회 시스템
     */
    object Info {
        /**
         * 지원되는 모든 길이 단위 조회
         */
        fun getSupportedLengthUnits(): Set<String> {
            return UnitRegistry.getUnitsByCategory(UnitCategory.LENGTH)
        }
        
        /**
         * 지원되는 모든 무게 단위 조회
         */
        fun getSupportedWeightUnits(): Set<String> {
            return UnitRegistry.getUnitsByCategory(UnitCategory.WEIGHT)
        }
        
        /**
         * 단위가 유효한지 확인
         */
        fun isValidUnit(unit: String): Boolean {
            return UnitRegistry.isValidUnit(unit)
        }
        
        /**
         * 단위 정보 조회
         */
        fun getUnitInfo(unit: String): UnitDefinition? {
            return UnitRegistry.getDefinition(unit)
        }
    }
}

/**
 * 단위 제안 결과를 나타내는 데이터 클래스
 */
data class UnitSuggestion(
    val original: String,
    val suggested: String?,
    val reason: String
) {
    /**
     * 제안이 있는지 확인
     */
    fun hasSuggestion(): Boolean = suggested != null
    
    /**
     * 사용자 친화적인 메시지 반환
     */
    fun getMessage(): String {
        return if (hasSuggestion()) {
            "Original: $original\nSuggested: $suggested\nReason: $reason"
        } else {
            reason
        }
    }
}

/**
 * 전역 ECU 인스턴스 (편의를 위한 별칭)
 */
val ecu = ECU
