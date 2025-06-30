package io.ecu.engineering

import io.ecu.BaseUnit
import io.ecu.RoundingMode
import io.ecu.UnitCategory
import io.ecu.UnitDefinition
import io.ecu.UnitRegistry

/**
 * 압력 단위를 나타내는 클래스
 * 
 * 기본 단위: 파스칼(Pa)
 */
class Pressure private constructor(
    baseValue: Double,
    symbol: String,
    displayName: String,
    precision: Int = -1,
    roundingMode: RoundingMode = RoundingMode.HALF_UP
) : BaseUnit<Pressure>(baseValue, symbol, displayName, UnitCategory.PRESSURE, precision, roundingMode) {
    
    companion object {
        init {
            // 압력 단위 등록
            UnitRegistry.register(UnitDefinition("Pa", "pascal", UnitCategory.PRESSURE, 1.0, true, setOf("pascal", "pascals")))
            UnitRegistry.register(UnitDefinition("kPa", "kilopascal", UnitCategory.PRESSURE, 1000.0, aliases = setOf("kilopascal", "kilopascals")))
            UnitRegistry.register(UnitDefinition("MPa", "megapascal", UnitCategory.PRESSURE, 1_000_000.0, aliases = setOf("megapascal", "megapascals")))
            UnitRegistry.register(UnitDefinition("bar", "bar", UnitCategory.PRESSURE, 100_000.0, aliases = setOf("bars")))
            UnitRegistry.register(UnitDefinition("mbar", "millibar", UnitCategory.PRESSURE, 100.0, aliases = setOf("millibar", "millibars")))
            UnitRegistry.register(UnitDefinition("psi", "pounds per square inch", UnitCategory.PRESSURE, 6894.76, aliases = setOf("PSI")))
            UnitRegistry.register(UnitDefinition("atm", "atmosphere", UnitCategory.PRESSURE, 101325.0, aliases = setOf("atmosphere", "atmospheres")))
            UnitRegistry.register(UnitDefinition("mmHg", "millimeter of mercury", UnitCategory.PRESSURE, 133.322, aliases = setOf("mm Hg", "torr")))
            UnitRegistry.register(UnitDefinition("inHg", "inch of mercury", UnitCategory.PRESSURE, 3386.39, aliases = setOf("in Hg")))
        }
        
        /**
         * 문자열에서 압력 객체 생성
         * 
         * @param input "100Pa", "1bar", "14.5psi" 등의 형식
         * @return Pressure 객체
         */
        fun parse(input: String): Pressure {
            val trimmed = input.trim()
            val parts = parseValueAndUnit(trimmed)
            val value = parts.first
            val unit = parts.second
            
            val definition = UnitRegistry.getDefinition(unit)
                ?: throw IllegalArgumentException("Unknown pressure unit: $unit")
            
            if (definition.category != UnitCategory.PRESSURE) {
                throw IllegalArgumentException("$unit is not a pressure unit")
            }
            
            val baseValue = value * definition.baseRatio
            return Pressure(baseValue, definition.symbol, definition.displayName)
        }
        
        /**
         * 값과 단위로 압력 객체 생성
         */
        fun of(value: Double, unit: String): Pressure {
            return parse("$value$unit")
        }
        
        /**
         * 파스칼 단위로 압력 객체 생성
         */
        fun pascals(value: Double): Pressure {
            return Pressure(value, "Pa", "Pascal")
        }
        
        /**
         * 킬로파스칼 단위로 압력 객체 생성
         */
        fun kilopascals(value: Double): Pressure {
            return Pressure(value * 1000, "kPa", "kilopascal")
        }
        
        /**
         * 바 단위로 압력 객체 생성
         */
        fun bars(value: Double): Pressure {
            return Pressure(value * 100000, "bar", "bar")
        }
        
        /**
         * PSI 단위로 압력 객체 생성
         */
        fun psi(value: Double): Pressure {
            return Pressure(value * 6894.76, "psi", "pounds per square inch")
        }
        
        /**
         * 대기압 단위로 압력 객체 생성
         */
        fun atmospheres(value: Double): Pressure {
            return Pressure(value * 101325, "atm", "atmosphere")
        }
        
        /**
         * 입력 문자열에서 값과 단위 파싱
         */
        private fun parseValueAndUnit(input: String): Pair<Double, String> {
            val regex = Regex("""^([+-]?\d*\.?\d+(?:[eE][+-]?\d+)?)\s*(.+)$""")
            val matchResult = regex.find(input)
                ?: throw IllegalArgumentException("Invalid format: $input. Expected format: '100Pa' or '1 bar'")
            
            val value = matchResult.groupValues[1].toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid number: ${matchResult.groupValues[1]}")
            
            val unit = matchResult.groupValues[2].trim()
            
            return Pair(value, unit)
        }
    }
    
    /**
     * 다른 압력 단위로 변환
     */
    override fun to(targetSymbol: String): Pressure {
        val targetDefinition = UnitRegistry.getDefinition(targetSymbol)
            ?: throw IllegalArgumentException("Unknown pressure unit: $targetSymbol")
        
        if (targetDefinition.category != UnitCategory.PRESSURE) {
            throw IllegalArgumentException("$targetSymbol is not a pressure unit")
        }
        
        return Pressure(
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
    ): Pressure {
        return Pressure(baseValue, symbol, displayName, precision, roundingMode)
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
     * 파스칼 단위로 값 반환
     */
    val pascals: Double
        get() = baseValue
    
    /**
     * 킬로파스칼 단위로 값 반환
     */
    val kilopascals: Double
        get() = baseValue / 1000
    
    /**
     * 바 단위로 값 반환
     */
    val bars: Double
        get() = baseValue / 100000
    
    /**
     * PSI 단위로 값 반환
     */
    val psi: Double
        get() = baseValue / 6894.76
    
    /**
     * 대기압 단위로 값 반환
     */
    val atmospheres: Double
        get() = baseValue / 101325
    
    /**
     * 압력 덧셈
     */
    operator fun plus(other: Pressure): Pressure {
        return Pressure(
            baseValue = this.baseValue + other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 압력 뺄셈
     */
    operator fun minus(other: Pressure): Pressure {
        return Pressure(
            baseValue = this.baseValue - other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 압력에 배수 적용
     */
    operator fun times(factor: Double): Pressure {
        return Pressure(
            baseValue = this.baseValue * factor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 압력을 나눔
     */
    operator fun div(divisor: Double): Pressure {
        require(divisor != 0.0) { "Cannot divide by zero" }
        return Pressure(
            baseValue = this.baseValue / divisor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 압력 비교
     */
    operator fun compareTo(other: Pressure): Int {
        return this.baseValue.compareTo(other.baseValue)
    }
    
    /**
     * 표준 대기압과의 비교
     */
    fun isAboveAtmospheric(): Boolean {
        return baseValue > 101325
    }
    
    /**
     * 진공 상태인지 확인
     */
    fun isVacuum(): Boolean {
        return baseValue < 0
    }
    
    /**
     * 게이지 압력 계산 (대기압 기준)
     */
    fun toGauge(): Pressure {
        return Pressure(
            baseValue = this.baseValue - 101325,
            symbol = this.symbol + " (gauge)",
            displayName = this.displayName + " (gauge)",
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 절대 압력 계산 (게이지 압력에서)
     */
    fun toAbsolute(): Pressure {
        if (symbol.contains("gauge")) {
            return Pressure(
                baseValue = this.baseValue + 101325,
                symbol = this.symbol.replace(" (gauge)", ""),
                displayName = this.displayName.replace(" (gauge)", ""),
                precision = this.precision,
                roundingMode = this.roundingMode
            )
        }
        return this
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
