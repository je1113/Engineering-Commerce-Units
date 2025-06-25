package io.ecu

/**
 * Engineering Commerce Units의 메인 진입점
 * 
 * 유창한 API를 제공하여 직관적인 단위 변환을 가능하게 합니다.
 */
object ECU {
    
    /**
     * 길이 단위 변환을 위한 진입점
     */
    fun length(input: String): Length {
        return Length.parse(input)
    }
    
    /**
     * 무게 단위 변환을 위한 진입점
     */
    fun weight(input: String): Weight {
        return Weight.parse(input)
    }
    
    /**
     * 부피 단위 변환을 위한 진입점
     */
    fun volume(input: String): Volume {
        return Volume.parse(input)
    }
    
    /**
     * 온도 단위 변환을 위한 진입점
     */
    fun temperature(input: String): Temperature {
        return Temperature.parse(input)
    }
    
    /**
     * 면적 단위 변환을 위한 진입점
     */
    fun area(input: String): Area {
        return Area.parse(input)
    }
    
    /**
     * 압력 단위 변환을 위한 진입점
     */
    fun pressure(input: String): Pressure {
        return Pressure.parse(input)
    }
    
    /**
     * 속도 단위 변환을 위한 진입점
     */
    fun speed(input: String): Speed {
        return Speed.parse(input)
    }
    
    /**
     * 단위 변환 검증 및 제안 시스템
     */
    object Auto {
        /**
         * 더 적절한 단위 제안
         */
        fun suggest(input: String): UnitSuggestion {
            try {
                val length = length(input)
                return suggestBetterLengthUnit(length)
            } catch (e: Exception) {
                // 다른 단위 타입 시도
            }
            
            try {
                val weight = weight(input)
                return suggestBetterWeightUnit(weight)
            } catch (e: Exception) {
                // 다른 단위 타입 시도
            }
            
            try {
                val volume = volume(input)
                return suggestBetterVolumeUnit(volume)
            } catch (e: Exception) {
                // 다른 단위 타입 시도
            }
            
            try {
                val temperature = temperature(input)
                return suggestBetterTemperatureUnit(temperature)
            } catch (e: Exception) {
                // 다른 단위 타입 시도
            }
            
            try {
                val area = area(input)
                return suggestBetterAreaUnit(area)
            } catch (e: Exception) {
                // 다른 단위 타입 시도
            }
            
            try {
                val pressure = pressure(input)
                return suggestBetterPressureUnit(pressure)
            } catch (e: Exception) {
                // 다른 단위 타입 시도
            }
            
            try {
                val speed = speed(input)
                return suggestBetterSpeedUnit(speed)
            } catch (e: Exception) {
                // 모든 타입 실패
            }
            
            return UnitSuggestion(input, null, "No suggestion available")
        }
        
        private fun suggestBetterLengthUnit(length: Length): UnitSuggestion {
            val meters = length.meters
            
            return when {
                meters < 0.001 -> {
                    UnitSuggestion(
                        original = length.toString(),
                        suggested = Length.of(meters * 1_000_000, "μm").format(),
                        reason = "Consider using micrometers for very small lengths"
                    )
                }
                meters < 0.01 -> {
                    UnitSuggestion(
                        original = length.toString(),
                        suggested = Length.of(meters * 1000, "mm").format(),
                        reason = "Consider using millimeters for small lengths"
                    )
                }
                meters < 1.0 -> {
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
        
        private fun suggestBetterVolumeUnit(volume: Volume): UnitSuggestion {
            val liters = volume.liters
            
            return when {
                liters < 0.001 -> {
                    UnitSuggestion(
                        original = volume.toString(),
                        suggested = Volume.of(liters * 1000, "ml").format(),
                        reason = "Consider using milliliters for small volumes"
                    )
                }
                liters > 1000 -> {
                    UnitSuggestion(
                        original = volume.toString(),
                        suggested = Volume.of(liters / 1000, "m³").format(),
                        reason = "Consider using cubic meters for large volumes"
                    )
                }
                else -> UnitSuggestion(volume.toString(), null, "Current unit is appropriate")
            }
        }
        
        private fun suggestBetterTemperatureUnit(temperature: Temperature): UnitSuggestion {
            val celsius = temperature.celsius
            
            return when {
                celsius < -200 -> {
                    UnitSuggestion(
                        original = temperature.toString(),
                        suggested = Temperature.of(temperature.kelvin, "K").format(),
                        reason = "Consider using Kelvin for very low temperatures"
                    )
                }
                temperature.symbol == "K" && celsius > -100 -> {
                    UnitSuggestion(
                        original = temperature.toString(),
                        suggested = Temperature.of(celsius, "°C").format(),
                        reason = "Consider using Celsius for everyday temperatures"
                    )
                }
                else -> UnitSuggestion(temperature.toString(), null, "Current unit is appropriate")
            }
        }
        
        private fun suggestBetterAreaUnit(area: Area): UnitSuggestion {
            val sqm = area.squareMeters
            
            return when {
                sqm < 0.01 -> {
                    UnitSuggestion(
                        original = area.toString(),
                        suggested = Area.of(sqm * 10000, "cm²").format(),
                        reason = "Consider using square centimeters for small areas"
                    )
                }
                sqm > 1000000 -> {
                    UnitSuggestion(
                        original = area.toString(),
                        suggested = Area.of(sqm / 1000000, "km²").format(),
                        reason = "Consider using square kilometers for large areas"
                    )
                }
                sqm > 10000 -> {
                    UnitSuggestion(
                        original = area.toString(),
                        suggested = Area.of(sqm / 10000, "ha").format(),
                        reason = "Consider using hectares for large land areas"
                    )
                }
                else -> UnitSuggestion(area.toString(), null, "Current unit is appropriate")
            }
        }
        
        private fun suggestBetterPressureUnit(pressure: Pressure): UnitSuggestion {
            val pa = pressure.pascals
            
            return when {
                pa < 1000 -> {
                    UnitSuggestion(
                        original = pressure.toString(),
                        suggested = Pressure.of(pa, "Pa").format(),
                        reason = "Consider using Pascals for low pressures"
                    )
                }
                pa in 1000.0..100000.0 -> {
                    UnitSuggestion(
                        original = pressure.toString(),
                        suggested = Pressure.of(pa / 1000, "kPa").format(),
                        reason = "Consider using kilopascals for medium pressures"
                    )
                }
                pa > 1000000 -> {
                    UnitSuggestion(
                        original = pressure.toString(),
                        suggested = Pressure.of(pa / 100000, "bar").format(),
                        reason = "Consider using bars for high pressures"
                    )
                }
                else -> UnitSuggestion(pressure.toString(), null, "Current unit is appropriate")
            }
        }
        
        private fun suggestBetterSpeedUnit(speed: Speed): UnitSuggestion {
            val ms = speed.metersPerSecond
            
            return when {
                ms < 0.01 -> {
                    UnitSuggestion(
                        original = speed.toString(),
                        suggested = speed.to("mm/s").format(),
                        reason = "Consider using mm/s for extremely slow speeds"
                    )
                }
                ms < 0.1 -> {
                    UnitSuggestion(
                        original = speed.toString(),
                        suggested = speed.to("cm/s").format(),
                        reason = "Consider using cm/s for very slow speeds"
                    )
                }
                ms in 0.1..50.0 -> {
                    UnitSuggestion(
                        original = speed.toString(),
                        suggested = speed.to("km/h").format(),
                        reason = "Consider using km/h for moderate speeds"
                    )
                }
                ms > 100 -> {
                    val mach = speed.mach
                    if (mach > 0.8) {
                        UnitSuggestion(
                            original = speed.toString(),
                            suggested = speed.to("Ma").format(),
                            reason = "Consider using Mach number for high speeds"
                        )
                    } else {
                        UnitSuggestion(
                            original = speed.toString(),
                            suggested = speed.to("km/h").format(),
                            reason = "Consider using km/h for this speed range"
                        )
                    }
                }
                else -> UnitSuggestion(speed.toString(), null, "Current unit is appropriate")
            }
        }
    }
    
    /**
     * 배치 변환 시스템
     */
    object Batch {
        /**
         * 여러 길이를 동일한 단위로 변환
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
        
        /**
         * 여러 부피를 동일한 단위로 변환
         */
        fun convertVolumes(inputs: List<String>, targetUnit: String): List<Volume> {
            return inputs.map { input ->
                volume(input).to(targetUnit)
            }
        }
        
        /**
         * 여러 온도를 동일한 단위로 변환
         */
        fun convertTemperatures(inputs: List<String>, targetUnit: String): List<Temperature> {
            return inputs.map { input ->
                temperature(input).to(targetUnit)
            }
        }
        
        /**
         * 여러 면적을 동일한 단위로 변환
         */
        fun convertAreas(inputs: List<String>, targetUnit: String): List<Area> {
            return inputs.map { input ->
                area(input).to(targetUnit)
            }
        }
        
        /**
         * 여러 압력을 동일한 단위로 변환
         */
        fun convertPressures(inputs: List<String>, targetUnit: String): List<Pressure> {
            return inputs.map { input ->
                pressure(input).to(targetUnit)
            }
        }
        
        /**
         * 여러 속도를 동일한 단위로 변환
         */
        fun convertSpeeds(inputs: List<String>, targetUnit: String): List<Speed> {
            return inputs.map { input ->
                speed(input).to(targetUnit)
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
         * 지원되는 모든 부피 단위 조회
         */
        fun getSupportedVolumeUnits(): Set<String> {
            return UnitRegistry.getUnitsByCategory(UnitCategory.VOLUME)
        }
        
        /**
         * 지원되는 모든 온도 단위 조회
         */
        fun getSupportedTemperatureUnits(): Set<String> {
            return UnitRegistry.getUnitsByCategory(UnitCategory.TEMPERATURE)
        }
        
        /**
         * 지원되는 모든 면적 단위 조회
         */
        fun getSupportedAreaUnits(): Set<String> {
            return UnitRegistry.getUnitsByCategory(UnitCategory.AREA)
        }
        
        /**
         * 지원되는 모든 압력 단위 조회
         */
        fun getSupportedPressureUnits(): Set<String> {
            return UnitRegistry.getUnitsByCategory(UnitCategory.PRESSURE)
        }
        
        /**
         * 지원되는 모든 속도 단위 조회
         */
        fun getSupportedSpeedUnits(): Set<String> {
            return UnitRegistry.getUnitsByCategory(UnitCategory.SPEED)
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
