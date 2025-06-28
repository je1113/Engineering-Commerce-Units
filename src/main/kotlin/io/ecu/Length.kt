package io.ecu

/**
 * 길이 단위를 나타내는 클래스
 * 
 * 기본 단위: 미터(m)
 */
class Length private constructor(
    baseValue: Double,
    symbol: String,
    displayName: String,
    precision: Int = -1,
    roundingMode: RoundingMode = RoundingMode.HALF_UP
) : BaseUnit<Length>(baseValue, symbol, displayName, UnitCategory.LENGTH, precision, roundingMode) {
    
    companion object {
        /**
         * 문자열에서 길이 객체 생성
         * 
         * @param input "5m", "10.5ft", "100cm" 등의 형식
         * @return Length 객체
         */
        fun parse(input: String): Length {
            val trimmed = input.trim()
            val parts = parseValueAndUnit(trimmed)
            val value = parts.first
            val unit = parts.second
            
            val definition = UnitRegistry.getDefinition(unit)
                ?: throw IllegalArgumentException("Unknown length unit: $unit")
            
            if (definition.category != UnitCategory.LENGTH) {
                throw IllegalArgumentException("$unit is not a length unit")
            }
            
            val baseValue = value * definition.baseRatio
            return Length(baseValue, definition.symbol, definition.displayName)
        }
        
        /**
         * 값과 단위로 길이 객체 생성
         */
        fun of(value: Double, unit: String): Length {
            return parse("$value$unit")
        }
        
        /**
         * 미터 단위로 길이 객체 생성
         */
        fun meters(value: Double): Length {
            return Length(value, "m", "meter")
        }
        
        /**
         * 센티미터 단위로 길이 객체 생성
         */
        fun centimeters(value: Double): Length {
            return Length(value * 0.01, "cm", "centimeter")
        }
        
        /**
         * 인치 단위로 길이 객체 생성
         */
        fun inches(value: Double): Length {
            return Length(value * 0.0254, "in", "inch")
        }
        
        /**
         * 피트 단위로 길이 객체 생성
         */
        fun feet(value: Double): Length {
            return Length(value * 0.3048, "ft", "foot")
        }
        
        /**
         * 입력 문자열에서 값과 단위 파싱
         */
        private fun parseValueAndUnit(input: String): Pair<Double, String> {
            val regex = Regex("""^([+-]?\d*\.?\d+(?:[eE][+-]?\d+)?)\s*(.+)$""")
            val matchResult = regex.find(input)
                ?: throw IllegalArgumentException("Invalid format: $input. Expected format: '5.5m' or '10 ft'")
            
            val value = matchResult.groupValues[1].toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid number: ${matchResult.groupValues[1]}")
            
            val unit = matchResult.groupValues[2].trim()
            
            return Pair(value, unit)
        }
    }
    
    /**
     * 다른 길이 단위로 변환
     */
    override fun to(targetSymbol: String): Length {
        val targetDefinition = UnitRegistry.getDefinition(targetSymbol)
            ?: throw IllegalArgumentException("Unknown length unit: $targetSymbol")
        
        if (targetDefinition.category != UnitCategory.LENGTH) {
            throw IllegalArgumentException("$targetSymbol is not a length unit")
        }
        
        return Length(
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
    ): Length {
        return Length(baseValue, symbol, displayName, precision, roundingMode)
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
     * 미터 단위로 값 반환
     */
    val meters: Double
        get() = baseValue
    
    /**
     * 킬로미터 단위로 값 반환
     */
    val kilometers: Double
        get() = baseValue / 1000
    
    /**
     * 센티미터 단위로 값 반환
     */
    val centimeters: Double
        get() = baseValue * 100
    
    /**
     * 인치 단위로 값 반환
     */
    val inches: Double
        get() = baseValue / 0.0254
    
    /**
     * 피트 단위로 값 반환
     */
    val feet: Double
        get() = baseValue / 0.3048
    
    /**
     * 길이 덧셈
     */
    operator fun plus(other: Length): Length {
        return Length(
            baseValue = this.baseValue + other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 길이 뺄셈
     */
    operator fun minus(other: Length): Length {
        return Length(
            baseValue = this.baseValue - other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 길이에 배수 적용
     */
    operator fun times(factor: Double): Length {
        return Length(
            baseValue = this.baseValue * factor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 길이를 나눔
     */
    operator fun div(divisor: Double): Length {
        require(divisor != 0.0) { "Cannot divide by zero" }
        return Length(
            baseValue = this.baseValue / divisor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 길이 비교
     */
    operator fun compareTo(other: Length): Int {
        return this.baseValue.compareTo(other.baseValue)
    }
    
    /**
     * 정밀도가 적용된 포맷팅
     */
    override fun format(locale: String?): String {
        val formattedValue = if (precision >= 0) {
            String.format("%.${precision}f", value)
        } else {
            value.toString()
        }
        
        return "$formattedValue $symbol"
    }
}
