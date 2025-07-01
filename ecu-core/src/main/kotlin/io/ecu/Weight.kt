@file:JvmName("WeightUnit")
package io.ecu

/**
 * 무게 단위를 나타내는 클래스
 * 
 * 기본 단위: 킬로그램(kg)
 * 
 * Java 사용 예:
 * ```java
 * Weight weight = ECU.weight("500 g");
 * Weight kilos = ECU.weight(2.5, "kg");
 * System.out.println(weight.to("kg")); // "0.5 kg"
 * ```
 */
public class Weight private constructor(
    baseValue: Double,
    symbol: String,
    displayName: String,
    precision: Int = -1,
    roundingMode: RoundingMode = RoundingMode.HALF_UP
) : BaseUnit<Weight>(baseValue, symbol, displayName, UnitCategory.WEIGHT, precision, roundingMode) {
    
    companion object {
        /**
         * 문자열에서 무게 객체 생성
         */
        fun parse(input: String): Weight {
            val trimmed = input.trim()
            val parts = parseValueAndUnit(trimmed)
            val value = parts.first
            val unit = parts.second
            
            val definition = UnitRegistry.getDefinition(unit)
                ?: throw IllegalArgumentException("Unknown weight unit: $unit")
            
            if (definition.category != UnitCategory.WEIGHT) {
                throw IllegalArgumentException("$unit is not a weight unit")
            }
            
            val baseValue = value * definition.baseRatio
            return Weight(baseValue, definition.symbol, definition.displayName)
        }
        
        /**
         * 값과 단위로 무게 객체 생성
         */
        fun of(value: Double, unit: String): Weight {
            return parse("$value$unit")
        }
        
        /**
         * 킬로그램 단위로 무게 객체 생성
         */
        fun kilograms(value: Double): Weight {
            return Weight(value, "kg", "kilogram")
        }
        
        /**
         * 그램 단위로 무게 객체 생성
         */
        fun grams(value: Double): Weight {
            return Weight(value * 0.001, "g", "gram")
        }
        
        /**
         * 파운드 단위로 무게 객체 생성
         */
        fun pounds(value: Double): Weight {
            return Weight(value * 0.453592, "lb", "pound")
        }
        
        /**
         * 온스 단위로 무게 객체 생성
         */
        fun ounces(value: Double): Weight {
            return Weight(value * 0.0283495, "oz", "ounce")
        }
        
        /**
         * 입력 문자열에서 값과 단위 파싱
         */
        private fun parseValueAndUnit(input: String): Pair<Double, String> {
            val regex = Regex("""^([+-]?\d*\.?\d+(?:[eE][+-]?\d+)?)\s*(.+)$""")
            val matchResult = regex.find(input)
                ?: throw IllegalArgumentException("Invalid format: $input. Expected format: '5.5kg' or '10 lb'")
            
            val value = matchResult.groupValues[1].toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid number: ${matchResult.groupValues[1]}")
            
            val unit = matchResult.groupValues[2].trim()
            
            return Pair(value, unit)
        }
    }
    
    /**
     * 다른 무게 단위로 변환
     */
    override fun to(targetSymbol: String): Weight {
        val targetDefinition = UnitRegistry.getDefinition(targetSymbol)
            ?: throw IllegalArgumentException("Unknown weight unit: $targetSymbol")
        
        if (targetDefinition.category != UnitCategory.WEIGHT) {
            throw IllegalArgumentException("$targetSymbol is not a weight unit")
        }
        
        return Weight(
            baseValue = baseValue,
            symbol = targetDefinition.symbol,
            displayName = targetDefinition.displayName,
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
    ): Weight {
        return Weight(baseValue, symbol, displayName, precision, roundingMode)
    }
    
    /**
     * 현재 단위에서의 값 계산
     */
    val value: Double
        get() {
            val definition = UnitRegistry.getDefinition(symbol)
                ?: throw IllegalStateException("Unknown unit: $symbol")
            return baseValue / definition.baseRatio
        }
    
    /**
     * 킬로그램 단위로 값 반환
     */
    val kilograms: Double
        get() = baseValue
    
    /**
     * 그램 단위로 값 반환
     */
    val grams: Double
        get() = baseValue * 1000
    
    /**
     * 파운드 단위로 값 반환
     */
    val pounds: Double
        get() = baseValue / 0.453592
    
    /**
     * 온스 단위로 값 반환
     */
    val ounces: Double
        get() = baseValue / 0.0283495
    
    /**
     * 무게 덧셈
     */
    operator fun plus(other: Weight): Weight {
        return Weight(
            baseValue = this.baseValue + other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 무게 뺄셈
     */
    operator fun minus(other: Weight): Weight {
        return Weight(
            baseValue = this.baseValue - other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 무게에 배수 적용
     */
    operator fun times(factor: Double): Weight {
        return Weight(
            baseValue = this.baseValue * factor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 무게를 나눔
     */
    operator fun div(divisor: Double): Weight {
        require(divisor != 0.0) { "Cannot divide by zero" }
        return Weight(
            baseValue = this.baseValue / divisor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 무게 비교
     */
    operator fun compareTo(other: Weight): Int {
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
        
        return "$formattedValue $symbol"
    }
}
