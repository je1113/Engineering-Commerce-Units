package io.ecu

import kotlin.math.pow
import kotlin.math.round
import kotlin.math.floor
import kotlin.math.ceil
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 에너지 단위를 나타내는 클래스
 *
 * 기본 단위: 줄(J)
 */
class Energy private constructor(
    baseValue: Double,
    symbol: String,
    displayName: String,
    precision: Int = -1,
    roundingMode: RoundingMode = RoundingMode.HALF_UP
) : BaseUnit<Energy>(baseValue, symbol, displayName, UnitCategory.ENERGY, precision, roundingMode) {

    companion object {
        init {
            // 에너지 단위 등록
            UnitRegistry.register(UnitDefinition("J", "joule", UnitCategory.ENERGY, 1.0, true, setOf("joule", "joules")))
            UnitRegistry.register(UnitDefinition("kJ", "kilojoule", UnitCategory.ENERGY, 1000.0, aliases = setOf("kilojoule", "kilojoules")))
            UnitRegistry.register(UnitDefinition("MJ", "megajoule", UnitCategory.ENERGY, 1_000_000.0, aliases = setOf("megajoule", "megajoules")))
            UnitRegistry.register(UnitDefinition("GJ", "gigajoule", UnitCategory.ENERGY, 1_000_000_000.0, aliases = setOf("gigajoule", "gigajoules")))
            UnitRegistry.register(UnitDefinition("mJ", "millijoule", UnitCategory.ENERGY, 0.001, aliases = setOf("millijoule", "millijoules")))
            UnitRegistry.register(UnitDefinition("Wh", "watt hour", UnitCategory.ENERGY, 3600.0, aliases = setOf("watt-hour", "watthour")))
            UnitRegistry.register(UnitDefinition("kWh", "kilowatt hour", UnitCategory.ENERGY, 3_600_000.0, aliases = setOf("kilowatt-hour", "kilowatthour")))
            UnitRegistry.register(UnitDefinition("MWh", "megawatt hour", UnitCategory.ENERGY, 3_600_000_000.0, aliases = setOf("megawatt-hour", "megawatthour")))
            UnitRegistry.register(UnitDefinition("cal", "calorie", UnitCategory.ENERGY, 4.184, aliases = setOf("calorie", "calories")))
            UnitRegistry.register(UnitDefinition("kcal", "kilocalorie", UnitCategory.ENERGY, 4184.0, aliases = setOf("kilocalorie", "kilocalories")))
            UnitRegistry.register(UnitDefinition("BTU", "British thermal unit", UnitCategory.ENERGY, 1055.06, aliases = setOf("btu", "Btu", "British thermal units")))
            UnitRegistry.register(UnitDefinition("eV", "electron volt", UnitCategory.ENERGY, 1.602176634e-19, aliases = setOf("electronvolt")))
            UnitRegistry.register(UnitDefinition("keV", "kiloelectron volt", UnitCategory.ENERGY, 1.602176634e-16, aliases = setOf("kiloelectronvolt")))
            UnitRegistry.register(UnitDefinition("MeV", "megaelectron volt", UnitCategory.ENERGY, 1.602176634e-13, aliases = setOf("megaelectronvolt")))
            UnitRegistry.register(UnitDefinition("ft⋅lbf", "foot-pound", UnitCategory.ENERGY, 1.35582, aliases = setOf("ft-lbf", "ftlbf", "foot-pound")))
            UnitRegistry.register(UnitDefinition("tTNT", "ton of TNT", UnitCategory.ENERGY, 4.184e9, aliases = setOf("ton-TNT")))
        }
        
        /**
         * 문자열에서 에너지 객체 생성
         *
         * @param input "100J", "50kWh", "2000cal" 등의 형식
         * @return Energy 객체
         */
        fun parse(input: String): Energy {
            val trimmed = input.trim()
            val parts = parseValueAndUnit(trimmed)
            val value = parts.first
            val unit = parts.second

            val definition = UnitRegistry.getDefinition(unit)
                ?: throw IllegalArgumentException("Unknown energy unit: $unit")

            if (definition.category != UnitCategory.ENERGY) {
                throw IllegalArgumentException("$unit is not an energy unit")
            }

            val baseValue = value * definition.baseRatio
            return Energy(baseValue, definition.symbol, definition.displayName)
        }

        /**
         * 값과 단위로 에너지 객체 생성
         */
        fun of(value: Double, unit: String): Energy {
            return parse("$value$unit")
        }

        /**
         * 줄 단위로 에너지 객체 생성
         */
        fun joules(value: Double): Energy {
            return Energy(value, "J", "joule")
        }

        /**
         * 킬로줄 단위로 에너지 객체 생성
         */
        fun kilojoules(value: Double): Energy {
            return Energy(value * 1000, "kJ", "kilojoule")
        }

        /**
         * 메가줄 단위로 에너지 객체 생성
         */
        fun megajoules(value: Double): Energy {
            return Energy(value * 1_000_000, "MJ", "megajoule")
        }

        /**
         * 기가줄 단위로 에너지 객체 생성
         */
        fun gigajoules(value: Double): Energy {
            return Energy(value * 1_000_000_000, "GJ", "gigajoule")
        }

        /**
         * 밀리줄 단위로 에너지 객체 생성
         */
        fun millijoules(value: Double): Energy {
            return Energy(value * 0.001, "mJ", "millijoule")
        }

        /**
         * 와트시 단위로 에너지 객체 생성
         */
        fun wattHours(value: Double): Energy {
            return Energy(value * 3600, "Wh", "watt hour")
        }

        /**
         * 킬로와트시 단위로 에너지 객체 생성
         */
        fun kilowattHours(value: Double): Energy {
            return Energy(value * 3_600_000, "kWh", "kilowatt hour")
        }

        /**
         * 메가와트시 단위로 에너지 객체 생성
         */
        fun megawattHours(value: Double): Energy {
            return Energy(value * 3_600_000_000, "MWh", "megawatt hour")
        }

        /**
         * 칼로리 단위로 에너지 객체 생성 (열화학 칼로리)
         */
        fun calories(value: Double): Energy {
            return Energy(value * 4.184, "cal", "calorie")
        }

        /**
         * 킬로칼로리 단위로 에너지 객체 생성 (식품 칼로리)
         */
        fun kilocalories(value: Double): Energy {
            return Energy(value * 4184, "kcal", "kilocalorie")
        }

        /**
         * BTU 단위로 에너지 객체 생성
         */
        fun btu(value: Double): Energy {
            return Energy(value * 1055.06, "BTU", "British thermal unit")
        }

        /**
         * 전자볼트 단위로 에너지 객체 생성
         */
        fun electronVolts(value: Double): Energy {
            return Energy(value * 1.602176634e-19, "eV", "electron volt")
        }

        /**
         * 킬로전자볼트 단위로 에너지 객체 생성
         */
        fun kiloelectronVolts(value: Double): Energy {
            return Energy(value * 1.602176634e-16, "keV", "kiloelectron volt")
        }

        /**
         * 메가전자볼트 단위로 에너지 객체 생성
         */
        fun megaelectronVolts(value: Double): Energy {
            return Energy(value * 1.602176634e-13, "MeV", "megaelectron volt")
        }

        /**
         * 풋파운드 단위로 에너지 객체 생성
         */
        fun footPounds(value: Double): Energy {
            return Energy(value * 1.35582, "ft⋅lbf", "foot-pound")
        }

        /**
         * TNT 톤 단위로 에너지 객체 생성
         */
        fun tonTNT(value: Double): Energy {
            return Energy(value * 4.184e9, "tTNT", "ton of TNT")
        }

        /**
         * 입력 문자열에서 값과 단위 파싱
         */
        private fun parseValueAndUnit(input: String): Pair<Double, String> {
            val regex = Regex("""^([+-]?\d*\.?\d+(?:[eE][+-]?\d+)?)\s*(.+)$""")
            val matchResult = regex.find(input)
                ?: throw IllegalArgumentException("Invalid format: $input. Expected format: '100J' or '50 kWh'")

            val value = matchResult.groupValues[1].toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid number: ${matchResult.groupValues[1]}")

            val unit = matchResult.groupValues[2].trim()

            return Pair(value, unit)
        }
    }

    /**
     * 다른 에너지 단위로 변환
     */
    override fun to(targetSymbol: String): Energy {
        val targetDefinition = UnitRegistry.getDefinition(targetSymbol)
            ?: throw IllegalArgumentException("Unknown energy unit: $targetSymbol")

        if (targetDefinition.category != UnitCategory.ENERGY) {
            throw IllegalArgumentException("$targetSymbol is not an energy unit")
        }

        return Energy(
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
    ): Energy {
        return Energy(baseValue, symbol, displayName, precision, roundingMode)
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
     * 줄 단위로 값 반환
     */
    val joules: Double
        get() = baseValue

    /**
     * 킬로줄 단위로 값 반환
     */
    val kilojoules: Double
        get() = baseValue / 1000

    /**
     * 메가줄 단위로 값 반환
     */
    val megajoules: Double
        get() = baseValue / 1_000_000

    /**
     * 기가줄 단위로 값 반환
     */
    val gigajoules: Double
        get() = baseValue / 1_000_000_000

    /**
     * 밀리줄 단위로 값 반환
     */
    val millijoules: Double
        get() = baseValue * 1000

    /**
     * 와트시 단위로 값 반환
     */
    val wattHours: Double
        get() = baseValue / 3600

    /**
     * 킬로와트시 단위로 값 반환
     */
    val kilowattHours: Double
        get() = baseValue / 3_600_000

    /**
     * 메가와트시 단위로 값 반환
     */
    val megawattHours: Double
        get() = baseValue / 3_600_000_000

    /**
     * 칼로리 단위로 값 반환
     */
    val calories: Double
        get() = baseValue / 4.184

    /**
     * 킬로칼로리 단위로 값 반환
     */
    val kilocalories: Double
        get() = baseValue / 4184

    /**
     * BTU 단위로 값 반환
     */
    val btu: Double
        get() = baseValue / 1055.06

    /**
     * 전자볼트 단위로 값 반환
     */
    val electronVolts: Double
        get() = baseValue / 1.602176634e-19

    /**
     * 킬로전자볼트 단위로 값 반환
     */
    val kiloelectronVolts: Double
        get() = baseValue / 1.602176634e-16

    /**
     * 메가전자볼트 단위로 값 반환
     */
    val megaelectronVolts: Double
        get() = baseValue / 1.602176634e-13

    /**
     * 풋파운드 단위로 값 반환
     */
    val footPounds: Double
        get() = baseValue / 1.35582

    /**
     * TNT 톤 단위로 값 반환
     */
    val tonTNT: Double
        get() = baseValue / 4.184e9

    /**
     * 에너지 덧셈
     */
    operator fun plus(other: Energy): Energy {
        return Energy(
            baseValue = this.baseValue + other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }

    /**
     * 에너지 뺄셈
     */
    operator fun minus(other: Energy): Energy {
        return Energy(
            baseValue = this.baseValue - other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }

    /**
     * 에너지에 배수 적용
     */
    operator fun times(factor: Double): Energy {
        return Energy(
            baseValue = this.baseValue * factor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }

    /**
     * 에너지를 나눔
     */
    operator fun div(divisor: Double): Energy {
        require(divisor != 0.0) { "Cannot divide by zero" }
        return Energy(
            baseValue = this.baseValue / divisor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }

    /**
     * 에너지 비교
     */
    operator fun compareTo(other: Energy): Int {
        return this.baseValue.compareTo(other.baseValue)
    }

    /**
     * 에너지 규모 분류
     */
    fun getEnergyScale(): EnergyScale {
        val j = baseValue
        return when {
            j < 1e-12 -> EnergyScale.ATOMIC      // 원자/분자 레벨
            j < 1e-6 -> EnergyScale.MICROSCOPIC  // 미시적
            j < 1 -> EnergyScale.TINY            // 극소량
            j < 1000 -> EnergyScale.SMALL        // 소량
            j < 1e6 -> EnergyScale.MODERATE      // 중간
            j < 1e9 -> EnergyScale.LARGE         // 대량
            j < 1e12 -> EnergyScale.VERY_LARGE   // 매우 큰
            j < 1e15 -> EnergyScale.HUGE         // 거대
            else -> EnergyScale.ASTRONOMICAL     // 천문학적
        }
    }

    /**
     * 주어진 전력으로 이 에너지를 생성/소비하는데 필요한 시간 계산
     * @param power 전력 (와트)
     * @return 시간 (초)
     */
    fun timeAtPower(power: Double): Double {
        require(power > 0) { "Power must be positive" }
        return baseValue / power
    }

    /**
     * 주어진 시간동안 이 에너지를 생성/소비하는데 필요한 평균 전력
     * @param time 시간 (초)
     * @return 전력 (와트)
     */
    fun powerOverTime(time: Double): Double {
        require(time > 0) { "Time must be positive" }
        return baseValue / time
    }

    /**
     * 이 에너지로 물의 온도를 올릴 수 있는 온도
     * @param mass 물의 질량 (kg)
     * @return 온도 상승 (°C)
     */
    fun waterTemperatureRise(mass: Double): Double {
        require(mass > 0) { "Mass must be positive" }
        val specificHeat = 4184.0 // J/(kg·°C) for water
        return baseValue / (mass * specificHeat)
    }

    /**
     * 이 에너지로 물체를 들어올릴 수 있는 높이
     * @param mass 물체의 질량 (kg)
     * @return 높이 (m)
     */
    fun liftHeight(mass: Double): Double {
        require(mass > 0) { "Mass must be positive" }
        val g = 9.81 // 중력가속도 m/s²
        return baseValue / (mass * g)
    }

    /**
     * 운동에너지로부터 속도 계산
     * @param mass 질량 (kg)
     * @return 속도 (m/s)
     */
    fun velocityFromKineticEnergy(mass: Double): Double {
        require(mass > 0) { "Mass must be positive" }
        require(baseValue >= 0) { "Kinetic energy must be non-negative" }
        return sqrt(2 * baseValue / mass)
    }

    /**
     * 이 에너지로 재료의 온도를 올릴 수 있는 온도
     * @param mass 재료의 질량 (kg)
     * @param specificHeat 비열 (J/(kg·°C))
     * @return 온도 상승 (°C)
     */
    fun temperatureRise(mass: Double, specificHeat: Double): Double {
        require(mass > 0) { "Mass must be positive" }
        require(specificHeat > 0) { "Specific heat must be positive" }
        return baseValue / (mass * specificHeat)
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
     * 에너지를 상황에 맞는 적절한 단위로 제안
     */
    fun suggestBestUnit(): Energy {
        val j = baseValue

        return when {
            // 원자/분자 레벨
            j < 1e-16 -> to("eV")

            // 작은 에너지
            j < 1 -> to("mJ")

            // 중간 크기
            j < 1000 -> to("J")

            // 큰 에너지
            j < 1e6 -> to("kJ")

            // 전기 에너지
            j < 3.6e7 -> to("kWh")

            // 매우 큰 에너지
            j < 1e9 -> to("MJ")

            // 거대한 에너지
            j < 1e12 -> to("GJ")

            // 폭발 에너지
            else -> to("tTNT")
        }
    }

    /**
     * 식품 에너지로 포맷팅
     */
    fun toFoodCalories(): String {
        return "${String.format("%.1f", kilocalories)} kcal"
    }

    /**
     * 전기 에너지로 포맷팅
     */
    fun toElectricalEnergy(): String {
        return when {
            kilowattHours < 1 -> "${String.format("%.1f", wattHours)} Wh"
            kilowattHours < 1000 -> "${String.format("%.3f", kilowattHours)} kWh"
            else -> "${String.format("%.3f", megawattHours)} MWh"
        }
    }

    /**
     * 폭발 에너지로 포맷팅 (TNT 당량)
     */
    fun toExplosiveEnergy(): String {
        val tnt = tonTNT
        return when {
            tnt < 0.001 -> "${String.format("%.1f", tnt * 1000)} kg TNT"
            tnt < 1 -> "${String.format("%.3f", tnt)} t TNT"
            tnt < 1000 -> "${String.format("%.1f", tnt)} t TNT"
            else -> "${String.format("%.1f", tnt / 1000)} kt TNT"
        }
    }
}

/**
 * 에너지 규모 열거형
 */
enum class EnergyScale(val description: String) {
    ATOMIC("Atomic level (< 1 pJ)"),
    MICROSCOPIC("Microscopic (< 1 μJ)"),
    TINY("Tiny (< 1 J)"),
    SMALL("Small (< 1 kJ)"),
    MODERATE("Moderate (< 1 MJ)"),
    LARGE("Large (< 1 GJ)"),
    VERY_LARGE("Very Large (< 1 TJ)"),
    HUGE("Huge (< 1 PJ)"),
    ASTRONOMICAL("Astronomical (≥ 1 PJ)")
}
