package io.ecu

/**
 * 면적 단위를 나타내는 클래스
 * 
 * 기본 단위: 제곱미터(m²)
 */
class Area private constructor(
    baseValue: Double,
    symbol: String,
    displayName: String,
    precision: Int = -1,
    roundingMode: RoundingMode = RoundingMode.HALF_UP
) : BaseUnit<Area>(baseValue, symbol, displayName, UnitCategory.AREA, precision, roundingMode) {
    
    companion object {
        /**
         * 문자열에서 면적 객체 생성
         */
        fun parse(input: String): Area {
            val trimmed = input.trim()
            val parts = parseValueAndUnit(trimmed)
            val value = parts.first
            val unit = parts.second
            
            val definition = UnitRegistry.getDefinition(unit)
                ?: throw IllegalArgumentException("Unknown area unit: $unit")
            
            if (definition.category != UnitCategory.AREA) {
                throw IllegalArgumentException("$unit is not an area unit")
            }
            
            val baseValue = value * definition.baseRatio
            return Area(baseValue, definition.symbol, definition.displayName)
        }
        
        /**
         * 값과 단위로 면적 객체 생성
         */
        fun of(value: Double, unit: String): Area {
            return parse("$value$unit")
        }
        
        /**
         * 제곱미터 단위로 면적 객체 생성
         */
        fun squareMeters(value: Double): Area {
            return Area(value, "m²", "square meter")
        }
        
        /**
         * 제곱센티미터 단위로 면적 객체 생성
         */
        fun squareCentimeters(value: Double): Area {
            return Area(value * 0.0001, "cm²", "square centimeter")
        }
        
        /**
         * 제곱인치 단위로 면적 객체 생성
         */
        fun squareInches(value: Double): Area {
            return Area(value * 0.00064516, "in²", "square inch")
        }
        
        /**
         * 제곱피트 단위로 면적 객체 생성
         */
        fun squareFeet(value: Double): Area {
            return Area(value * 0.092903, "ft²", "square foot")
        }
        
        /**
         * 에이커 단위로 면적 객체 생성
         */
        fun acres(value: Double): Area {
            return Area(value * 4046.86, "acre", "acre")
        }
        
        /**
         * 헥타르 단위로 면적 객체 생성
         */
        fun hectares(value: Double): Area {
            return Area(value * 10000.0, "ha", "hectare")
        }
        
        /**
         * 입력 문자열에서 값과 단위 파싱
         */
        private fun parseValueAndUnit(input: String): Pair<Double, String> {
            val regex = Regex("""^([+-]?\d*\.?\d+(?:[eE][+-]?\d+)?)\s*(.+)$""")
            val matchResult = regex.find(input)
                ?: throw IllegalArgumentException("Invalid format: $input. Expected format: '5.5m²' or '10 ft²'")
            
            val value = matchResult.groupValues[1].toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid number: ${matchResult.groupValues[1]}")
            
            val unit = matchResult.groupValues[2].trim()
            
            return Pair(value, unit)
        }
    }
    
    /**
     * 다른 면적 단위로 변환
     */
    override fun to(targetSymbol: String): Area {
        val targetDefinition = UnitRegistry.getDefinition(targetSymbol)
            ?: throw IllegalArgumentException("Unknown area unit: $targetSymbol")
        
        if (targetDefinition.category != UnitCategory.AREA) {
            throw IllegalArgumentException("$targetSymbol is not an area unit")
        }
        
        return Area(
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
    ): Area {
        return Area(baseValue, symbol, displayName, precision, roundingMode)
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
     * 제곱미터 단위로 값 반환
     */
    val squareMeters: Double
        get() = baseValue
    
    /**
     * 제곱센티미터 단위로 값 반환
     */
    val squareCentimeters: Double
        get() = baseValue * 10000
    
    /**
     * 제곱킬로미터 단위로 값 반환
     */
    val squareKilometers: Double
        get() = baseValue / 1000000
    
    /**
     * 제곱인치 단위로 값 반환
     */
    val squareInches: Double
        get() = baseValue / 0.00064516
    
    /**
     * 제곱피트 단위로 값 반환
     */
    val squareFeet: Double
        get() = baseValue / 0.092903
    
    /**
     * 에이커 단위로 값 반환
     */
    val acres: Double
        get() = baseValue / 4046.86
    
    /**
     * 헥타르 단위로 값 반환
     */
    val hectares: Double
        get() = baseValue / 10000.0
    
    /**
     * 면적 덧셈
     */
    operator fun plus(other: Area): Area {
        return Area(
            baseValue = this.baseValue + other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 면적 뺄셈
     */
    operator fun minus(other: Area): Area {
        return Area(
            baseValue = this.baseValue - other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 면적에 배수 적용
     */
    operator fun times(factor: Double): Area {
        return Area(
            baseValue = this.baseValue * factor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 면적을 나눔
     */
    operator fun div(divisor: Double): Area {
        require(divisor != 0.0) { "Cannot divide by zero" }
        return Area(
            baseValue = this.baseValue / divisor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 면적 비교
     */
    operator fun compareTo(other: Area): Int {
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
    
    /**
     * 길이로부터 정사각형 면적 계산
     */
    fun toSquareSide(): Length {
        val sideLength = kotlin.math.sqrt(baseValue)
        return Length.meters(sideLength)
    }
    
    /**
     * 원의 반지름 계산 (이 면적을 가진 원)
     */
    fun toCircleRadius(): Length {
        val radius = kotlin.math.sqrt(baseValue / kotlin.math.PI)
        return Length.meters(radius)
    }
    
    /**
     * 면적 범주 판정 (부동산 기준)
     */
    fun getAreaCategory(): AreaCategory {
        val sqm = squareMeters
        return when {
            sqm < 10 -> AreaCategory.TINY
            sqm < 50 -> AreaCategory.SMALL
            sqm < 200 -> AreaCategory.MEDIUM
            sqm < 1000 -> AreaCategory.LARGE
            sqm < 10000 -> AreaCategory.VERY_LARGE
            else -> AreaCategory.MASSIVE
        }
    }
}

/**
 * 면적 범주 열거형
 */
enum class AreaCategory {
    TINY,       // < 10m²
    SMALL,      // 10-50m²
    MEDIUM,     // 50-200m²
    LARGE,      // 200-1000m²
    VERY_LARGE, // 1000-10000m²
    MASSIVE     // > 10000m²
}
