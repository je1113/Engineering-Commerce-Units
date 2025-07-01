@file:JvmName("TemperatureUnit")
package io.ecu

/**
 * 온도 단위를 나타내는 클래스
 * 
 * 기본 단위: 켈빈(K)
 * 
 * 온도는 다른 단위들과 달리 선형 변환이 아닌 오프셋이 필요합니다.
 * - Celsius = Kelvin - 273.15
 * - Fahrenheit = (Kelvin - 273.15) × 9/5 + 32
 * 
 * Java 사용 예:
 * ```java
 * Temperature temp = ECU.temperature("25 °C");
 * Temperature fahrenheit = temp.to("°F");
 * System.out.println(fahrenheit); // "77.0°F"
 * ```
 */
class Temperature private constructor(
    baseValue: Double, // Kelvin 값
    symbol: String,
    displayName: String,
    precision: Int = -1,
    roundingMode: RoundingMode = RoundingMode.HALF_UP
) : BaseUnit<Temperature>(baseValue, symbol, displayName, UnitCategory.TEMPERATURE, precision, roundingMode) {
    
    companion object {
        /**
         * 문자열에서 온도 객체 생성
         */
        fun parse(input: String): Temperature {
            val trimmed = input.trim()
            val parts = parseValueAndUnit(trimmed)
            val value = parts.first
            val unit = parts.second
            
            // 온도는 특별한 변환 로직이 필요하므로 직접 처리
            val kelvinValue = when (unit.lowercase()) {
                "k", "kelvin" -> value
                "°c", "c", "celsius" -> value + 273.15
                "°f", "f", "fahrenheit" -> (value - 32) * 5.0 / 9.0 + 273.15
                else -> throw IllegalArgumentException("Unknown temperature unit: $unit")
            }
            
            val normalizedSymbol = when (unit.lowercase()) {
                "k", "kelvin" -> "K"
                "°c", "c", "celsius" -> "°C"
                "°f", "f", "fahrenheit" -> "°F"
                else -> unit
            }
            
            val displayName = when (normalizedSymbol) {
                "K" -> "Kelvin"
                "°C" -> "Celsius"
                "°F" -> "Fahrenheit"
                else -> unit
            }
            
            return Temperature(kelvinValue, normalizedSymbol, displayName)
        }
        
        /**
         * 값과 단위로 온도 객체 생성
         */
        fun of(value: Double, unit: String): Temperature {
            return parse("$value$unit")
        }
        
        /**
         * 켈빈 단위로 온도 객체 생성
         */
        fun kelvin(value: Double): Temperature {
            return Temperature(value, "K", "Kelvin")
        }
        
        /**
         * 섭씨 단위로 온도 객체 생성
         */
        fun celsius(value: Double): Temperature {
            return Temperature(value + 273.15, "°C", "Celsius")
        }
        
        /**
         * 화씨 단위로 온도 객체 생성
         */
        fun fahrenheit(value: Double): Temperature {
            return Temperature((value - 32) * 5.0 / 9.0 + 273.15, "°F", "Fahrenheit")
        }
        
        /**
         * 입력 문자열에서 값과 단위 파싱
         */
        private fun parseValueAndUnit(input: String): Pair<Double, String> {
            val regex = Regex("""^([+-]?\d*\.?\d+(?:[eE][+-]?\d+)?)\s*(.+)$""")
            val matchResult = regex.find(input)
                ?: throw IllegalArgumentException("Invalid format: $input. Expected format: '25°C' or '77 F'")
            
            val value = matchResult.groupValues[1].toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid number: ${matchResult.groupValues[1]}")
            
            val unit = matchResult.groupValues[2].trim()
            
            return Pair(value, unit)
        }
    }
    
    /**
     * 다른 온도 단위로 변환
     */
    override fun to(targetSymbol: String): Temperature {
        val normalizedTarget = when (targetSymbol.lowercase()) {
            "k", "kelvin" -> "K"
            "°c", "c", "celsius" -> "°C"
            "°f", "f", "fahrenheit" -> "°F"
            else -> throw IllegalArgumentException("Unknown temperature unit: $targetSymbol")
        }
        
        val displayName = when (normalizedTarget) {
            "K" -> "Kelvin"
            "°C" -> "Celsius"
            "°F" -> "Fahrenheit"
            else -> targetSymbol
        }
        
        return Temperature(
            baseValue = baseValue, // 켈빈 값은 그대로 유지
            symbol = normalizedTarget,
            displayName = displayName,
            precision = precision,
            roundingMode = roundingMode
        )
    }
    
    override fun createInstance(
        baseValue: Double,
        symbol: String,
        displayName: String,
        precision: Int,
        roundingMode: RoundingMode
    ): Temperature {
        return Temperature(baseValue, symbol, displayName, precision, roundingMode)
    }
    
    /**
     * 현재 단위에서의 값 계산
     */
    val value: Double
        get() = when (symbol) {
            "K" -> baseValue
            "°C" -> baseValue - 273.15
            "°F" -> (baseValue - 273.15) * 9.0 / 5.0 + 32
            else -> baseValue
        }
    
    /**
     * 켈빈 단위로 값 반환
     */
    val kelvin: Double
        get() = baseValue
    
    /**
     * 섭씨 단위로 값 반환
     */
    val celsius: Double
        get() = baseValue - 273.15
    
    /**
     * 화씨 단위로 값 반환
     */
    val fahrenheit: Double
        get() = (baseValue - 273.15) * 9.0 / 5.0 + 32
    
    /**
     * 온도 차이 계산 (켈빈 기준)
     */
    operator fun minus(other: Temperature): Double {
        return this.baseValue - other.baseValue
    }
    
    /**
     * 온도 비교
     */
    operator fun compareTo(other: Temperature): Int {
        return this.baseValue.compareTo(other.baseValue)
    }
    
    /**
     * 정밀도가 적용된 포맷팅
     */
    override fun format(locale: String?): String {
        val formattedValue = if (precision >= 0) {
            "%.${precision}f".format(value)
        } else {
            value.toString()
        }
        
        return "$formattedValue$symbol"
    }
    
    /**
     * 절대영도 확인
     */
    fun isAboveAbsoluteZero(): Boolean {
        return baseValue >= 0.0
    }
    
    /**
     * 물의 어는점/끓는점 확인 (표준 대기압 기준)
     */
    fun isFreezingPoint(): Boolean {
        return kotlin.math.abs(celsius - 0.0) < 0.1
    }
    
    fun isBoilingPoint(): Boolean {
        return kotlin.math.abs(celsius - 100.0) < 0.1
    }
    
    /**
     * 온도 범주 판정
     */
    fun getTemperatureCategory(): TemperatureCategory {
        val c = celsius
        return when {
            c < -40 -> TemperatureCategory.EXTREME_COLD
            c < 0 -> TemperatureCategory.FREEZING
            c < 10 -> TemperatureCategory.COLD
            c < 20 -> TemperatureCategory.COOL
            c < 30 -> TemperatureCategory.WARM
            c < 40 -> TemperatureCategory.HOT
            else -> TemperatureCategory.EXTREME_HOT
        }
    }
}

/**
 * 온도 범주 열거형
 */
enum class TemperatureCategory {
    EXTREME_COLD,
    FREEZING,
    COLD,
    COOL,
    WARM,
    HOT,
    EXTREME_HOT
}
