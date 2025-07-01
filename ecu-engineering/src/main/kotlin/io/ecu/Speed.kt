package io.ecu

import kotlin.math.pow
import kotlin.math.round
import kotlin.math.floor
import kotlin.math.ceil
import kotlin.math.abs

/**
 * 속도 단위를 나타내는 클래스
 * 
 * 기본 단위: 미터/초(m/s)
 */
class Speed private constructor(
    baseValue: Double,
    symbol: String,
    displayName: String,
    precision: Int = -1,
    roundingMode: RoundingMode = RoundingMode.HALF_UP
) : BaseUnit<Speed>(baseValue, symbol, displayName, UnitCategory.SPEED, precision, roundingMode) {
    
    companion object {
        init {
            // 속도 단위 등록
            UnitRegistry.register(UnitDefinition("m/s", "meter per second", UnitCategory.SPEED, 1.0, true, setOf("mps", "m/sec")))
            UnitRegistry.register(UnitDefinition("km/h", "kilometer per hour", UnitCategory.SPEED, 1.0/3.6, aliases = setOf("kmh", "kph", "kmph")))
            UnitRegistry.register(UnitDefinition("mph", "mile per hour", UnitCategory.SPEED, 0.44704, aliases = setOf("mi/h")))
            UnitRegistry.register(UnitDefinition("ft/s", "foot per second", UnitCategory.SPEED, 0.3048, aliases = setOf("fps", "ft/sec")))
            UnitRegistry.register(UnitDefinition("kn", "knot", UnitCategory.SPEED, 0.514444, aliases = setOf("knot", "knots", "kt")))
            UnitRegistry.register(UnitDefinition("Ma", "Mach", UnitCategory.SPEED, 343.0, aliases = setOf("mach", "M")))
            UnitRegistry.register(UnitDefinition("cm/s", "centimeter per second", UnitCategory.SPEED, 0.01, aliases = setOf("cmps", "cm/sec")))
            UnitRegistry.register(UnitDefinition("rpm", "revolutions per minute", UnitCategory.SPEED, 0.10471975511966, aliases = setOf("RPM")))
        }
        
        /**
         * 문자열에서 속도 객체 생성
         * 
         * @param input "60km/h", "25mph", "100m/s", "200ft/s" 등의 형식
         * @return Speed 객체
         */
        fun parse(input: String): Speed {
            val trimmed = input.trim()
            val parts = parseValueAndUnit(trimmed)
            val value = parts.first
            val unit = parts.second
            
            val definition = UnitRegistry.getDefinition(unit)
                ?: throw IllegalArgumentException("Unknown speed unit: $unit")
            
            if (definition.category != UnitCategory.SPEED) {
                throw IllegalArgumentException("$unit is not a speed unit")
            }
            
            val baseValue = value * definition.baseRatio
            return Speed(baseValue, definition.symbol, definition.displayName)
        }
        
        /**
         * 값과 단위로 속도 객체 생성
         */
        fun of(value: Double, unit: String): Speed {
            return parse("$value$unit")
        }
        
        /**
         * 미터/초 단위로 속도 객체 생성
         */
        fun metersPerSecond(value: Double): Speed {
            return Speed(value, "m/s", "meter per second")
        }
        
        /**
         * 킬로미터/시간 단위로 속도 객체 생성
         */
        fun kilometersPerHour(value: Double): Speed {
            return Speed(value / 3.6, "km/h", "kilometer per hour")
        }
        
        /**
         * 마일/시간 단위로 속도 객체 생성
         */
        fun milesPerHour(value: Double): Speed {
            return Speed(value * 0.44704, "mph", "mile per hour")
        }
        
        /**
         * 피트/초 단위로 속도 객체 생성
         */
        fun feetPerSecond(value: Double): Speed {
            return Speed(value * 0.3048, "ft/s", "foot per second")
        }
        
        /**
         * 노트 단위로 속도 객체 생성 (해상/항공 속도)
         */
        fun knots(value: Double): Speed {
            return Speed(value * 0.514444, "kn", "knot")
        }
        
        /**
         * 마하 단위로 속도 객체 생성 (표준 조건 기준)
         */
        fun mach(value: Double): Speed {
            return Speed(value * 343.0, "Ma", "Mach")
        }
        
        /**
         * 센티미터/초 단위로 속도 객체 생성
         */
        fun centimetersPerSecond(value: Double): Speed {
            return Speed(value * 0.01, "cm/s", "centimeter per second")
        }
        
        /**
         * 입력 문자열에서 값과 단위 파싱
         */
        private fun parseValueAndUnit(input: String): Pair<Double, String> {
            val regex = Regex("""^([+-]?\d*\.?\d+(?:[eE][+-]?\d+)?)\s*(.+)$""")
            val matchResult = regex.find(input)
                ?: throw IllegalArgumentException("Invalid format: $input. Expected format: '60km/h' or '25 mph'")
            
            val value = matchResult.groupValues[1].toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid number: ${matchResult.groupValues[1]}")
            
            val unit = matchResult.groupValues[2].trim()
            
            return Pair(value, unit)
        }
    }
    
    /**
     * 다른 속도 단위로 변환
     */
    override fun to(targetSymbol: String): Speed {
        val targetDefinition = UnitRegistry.getDefinition(targetSymbol)
            ?: throw IllegalArgumentException("Unknown speed unit: $targetSymbol")
        
        if (targetDefinition.category != UnitCategory.SPEED) {
            throw IllegalArgumentException("$targetSymbol is not a speed unit")
        }
        
        return Speed(
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
    ): Speed {
        return Speed(baseValue, symbol, displayName, precision, roundingMode)
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
     * 미터/초 단위로 값 반환
     */
    val metersPerSecond: Double
        get() = baseValue
    
    /**
     * 킬로미터/시간 단위로 값 반환
     */
    val kilometersPerHour: Double
        get() = baseValue * 3.6
    
    /**
     * 마일/시간 단위로 값 반환
     */
    val milesPerHour: Double
        get() = baseValue / 0.44704
    
    /**
     * 피트/초 단위로 값 반환
     */
    val feetPerSecond: Double
        get() = baseValue / 0.3048
    
    /**
     * 노트 단위로 값 반환
     */
    val knots: Double
        get() = baseValue / 0.514444
    
    /**
     * 마하 단위로 값 반환 (표준 조건 기준)
     */
    val mach: Double
        get() = baseValue / 343.0
    
    /**
     * 센티미터/초 단위로 값 반환
     */
    val centimetersPerSecond: Double
        get() = baseValue * 100
    
    /**
     * RPM(분당 회전수) 단위로 값 반환
     * 1 rpm = 2π/60 rad/s = 0.10471975511966 rad/s
     */
    val rpm: Double
        get() = baseValue / 0.10471975511966
    
    /**
     * 속도 덧셈 (가속/감속)
     */
    operator fun plus(other: Speed): Speed {
        return Speed(
            baseValue = this.baseValue + other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 속도 뺄셈 (감속)
     */
    operator fun minus(other: Speed): Speed {
        return Speed(
            baseValue = this.baseValue - other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 속도에 배수 적용
     */
    operator fun times(factor: Double): Speed {
        return Speed(
            baseValue = this.baseValue * factor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 속도를 나눔
     */
    operator fun div(divisor: Double): Speed {
        require(divisor != 0.0) { "Cannot divide by zero" }
        return Speed(
            baseValue = this.baseValue / divisor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    /**
     * 속도 비교
     */
    operator fun compareTo(other: Speed): Int {
        return this.baseValue.compareTo(other.baseValue)
    }
    
    /**
     * 속도 분류 (저속, 중속, 고속, 초고속)
     */
    fun getSpeedCategory(): SpeedCategory {
        val mps = baseValue
        return when {
            mps < 1.0 -> SpeedCategory.VERY_SLOW      // < 1 m/s (보행속도)
            mps < 10.0 -> SpeedCategory.SLOW          // < 10 m/s (자전거 속도)
            mps < 30.0 -> SpeedCategory.MODERATE      // < 30 m/s (도시 주행)
            mps < 100.0 -> SpeedCategory.FAST         // < 100 m/s (고속도로)
            mps < 343.0 -> SpeedCategory.VERY_FAST    // < 소음 속도
            else -> SpeedCategory.SUPERSONIC          // 초음속
        }
    }
    
    /**
     * 주어진 시간 동안 이동할 거리 계산
     * @param duration 시간 (초)
     * @return 이동 거리 (미터)
     */
    fun distanceInTime(duration: Double): Double {
        require(duration >= 0) { "Duration must be non-negative" }
        return baseValue * duration
    }
    
    /**
     * 주어진 거리를 이동하는데 필요한 시간 계산
     * @param distance 거리 (미터)
     * @return 필요한 시간 (초)
     */
    fun timeForDistance(distance: Double): Double {
        require(distance >= 0) { "Distance must be non-negative" }
        require(baseValue > 0) { "Speed must be positive" }
        return distance / baseValue
    }
    
    /**
     * 동적 에너지 계산 (질량 필요)
     * @param mass 질량 (kg)
     * @return 동적 에너지 (J)
     */
    fun kineticEnergy(mass: Double): Double {
        require(mass > 0) { "Mass must be positive" }
        return 0.5 * mass * baseValue * baseValue
    }
    
    /**
     * 정밀도가 적용된 포맷팅
     */
    override fun format(locale: String?): String {
        val currentValue = value  // 현재 단위의 값을 가져옴
        val formattedValue = if (precision >= 0) {
            val factor = 10.0.pow(precision.toDouble())
            val rounded = when (roundingMode) {
                RoundingMode.HALF_UP -> round(currentValue * factor) / factor
                RoundingMode.HALF_DOWN -> {
                    val scaled = currentValue * factor
                    val floored = floor(scaled)
                    val decimal = scaled - floored
                    if (decimal > 0.5) {
                        ceil(scaled) / factor
                    } else {
                        floored / factor
                    }
                }
                RoundingMode.HALF_EVEN -> {
                    val scaled = currentValue * factor
                    val rounded = round(scaled)
                    if (abs(scaled - rounded) == 0.5) {
                        if (rounded.toLong() % 2L == 0L) rounded / factor
                        else floor(scaled) / factor
                    } else rounded / factor
                }
                RoundingMode.UP -> ceil(currentValue * factor) / factor
                RoundingMode.DOWN -> floor(currentValue * factor) / factor
            }
            String.format("%.${precision}f", rounded)
        } else {
            currentValue.toString()
        }
        
        return "$formattedValue $symbol"
    }
    
    /**
     * 속도를 상황에 맞는 적절한 단위로 제안
     */
    fun suggestBestUnit(): Speed {
        val mps = baseValue
        
        return when {
            // 매우 느린 속도 (보행속도): cm/s
            mps < 0.1 -> to("cm/s")
            
            // 느린 속도 (도시 주행): km/h
            mps < 50.0 -> to("km/h")
            
            // 빠른 속도 (고속도로): mph
            mps in 50.0..150.0 -> to("mph")
            
            // 초고속 (제트기): 마하
            mps >= 343.0 -> to("Ma")
            
            // 기본: m/s
            else -> this  // 현재 단위 유지
        }
    }
}

/**
 * 속도 분류 열거형
 */
enum class SpeedCategory(val description: String) {
    VERY_SLOW("Very Slow (Walking pace)"),
    SLOW("Slow (Bicycle pace)"),
    MODERATE("Moderate (City driving)"),
    FAST("Fast (Highway driving)"),
    VERY_FAST("Very Fast (Aircraft)"),
    SUPERSONIC("Supersonic")
}
