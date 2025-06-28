package io.ecu

/**
 * 부피 단위를 나타내는 클래스
 * 
 * 기본 단위: 리터(l)
 */
class Volume private constructor(
    baseValue: Double,
    symbol: String,
    displayName: String,
    precision: Int = -1,
    roundingMode: RoundingMode = RoundingMode.HALF_UP
) : BaseUnit<Volume>(baseValue, symbol, displayName, UnitCategory.VOLUME, precision, roundingMode) {
    
    companion object {
        /**
         * 문자열에서 부피 객체 생성
         */
        fun parse(input: String): Volume {
            val trimmed = input.trim()
            val parts = parseValueAndUnit(trimmed)
            val value = parts.first
            val unit = parts.second
            
            val definition = UnitRegistry.getDefinition(unit)
                ?: throw IllegalArgumentException("Unknown volume unit: $unit")
            
            if (definition.category != UnitCategory.VOLUME) {
                throw IllegalArgumentException("$unit is not a volume unit")
            }
            
            val baseValue = value * definition.baseRatio
            return Volume(baseValue, definition.symbol, definition.displayName)
        }
        
        /**
         * 값과 단위로 부피 객체 생성
         */
        fun of(value: Double, unit: String): Volume {
            return parse("$value$unit")
        }
        
        /**
         * 리터 단위로 부피 객체 생성
         */
        fun liters(value: Double): Volume {
            return Volume(value, "l", "liter")
        }
        
        /**
         * 밀리리터 단위로 부피 객체 생성
         */
        fun milliliters(value: Double): Volume {
            return Volume(value * 0.001, "ml", "milliliter")
        }
        
        /**
         * 갤런 단위로 부피 객체 생성
         */
        fun gallons(value: Double): Volume {
            return Volume(value * 3.78541, "gal", "gallon")
        }
        
        /**
         * 입력 문자열에서 값과 단위 파싱
         */
        private fun parseValueAndUnit(input: String): Pair<Double, String> {
            val regex = Regex("""^([+-]?\d*\.?\d+(?:[eE][+-]?\d+)?)\s*(.+)$""")
            val matchResult = regex.find(input)
                ?: throw IllegalArgumentException("Invalid format: $input. Expected format: '5.5l' or '10 gal'")
            
            val value = matchResult.groupValues[1].toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid number: ${matchResult.groupValues[1]}")
            
            val unit = matchResult.groupValues[2].trim()
            
            return Pair(value, unit)
        }
    }
    
    /**
     * 다른 부피 단위로 변환
     */
    override fun to(targetSymbol: String): Volume {
        val targetDefinition = UnitRegistry.getDefinition(targetSymbol)
            ?: throw IllegalArgumentException("Unknown volume unit: $targetSymbol")
        
        if (targetDefinition.category != UnitCategory.VOLUME) {
            throw IllegalArgumentException("$targetSymbol is not a volume unit")
        }
        
        return Volume(
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
    ): Volume {
        return Volume(baseValue, symbol, displayName, precision, roundingMode)
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
     * 리터 단위로 값 반환
     */
    val liters: Double
        get() = baseValue
    
    /**
     * 밀리리터 단위로 값 반환
     */
    val milliliters: Double
        get() = baseValue * 1000
    
    /**
     * 갤런 단위로 값 반환
     */
    val gallons: Double
        get() = baseValue / 3.78541
    
    /**
     * 부피 덧셈
     */
    operator fun plus(other: Volume): Volume {
        return Volume(
            baseValue = this.baseValue + other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 부피 뺄셈
     */
    operator fun minus(other: Volume): Volume {
        return Volume(
            baseValue = this.baseValue - other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 부피에 배수 적용
     */
    operator fun times(factor: Double): Volume {
        return Volume(
            baseValue = this.baseValue * factor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 부피를 나눔
     */
    operator fun div(divisor: Double): Volume {
        require(divisor != 0.0) { "Cannot divide by zero" }
        return Volume(
            baseValue = this.baseValue / divisor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 부피 비교
     */
    operator fun compareTo(other: Volume): Int {
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
