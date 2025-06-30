package io.ecu

import java.math.BigDecimal

/**
 * Represents a torque (rotational force) measurement with type-safe unit conversions.
 * 
 * The base unit is Newton-meter (N⋅m) following SI standards.
 * Supports various engineering and automotive torque units.
 */
class Torque private constructor(
    baseValue: Double,
    symbol: String,
    displayName: String,
    precision: Int = -1,
    roundingMode: RoundingMode = RoundingMode.HALF_UP
) : BaseUnit<Torque>(baseValue, symbol, displayName, UnitCategory.TORQUE, precision, roundingMode) {

    companion object {
        init {
            // 토크 단위 등록
            UnitRegistry.register(UnitDefinition("Nm", "Newton-meter", UnitCategory.TORQUE, 1.0, true, setOf("N⋅m", "N·m", "N-m", "newton-meter")))
            UnitRegistry.register(UnitDefinition("kNm", "kilonewton-meter", UnitCategory.TORQUE, 1000.0, aliases = setOf("kN⋅m", "kN·m", "kN-m")))
            UnitRegistry.register(UnitDefinition("mNm", "millinewton-meter", UnitCategory.TORQUE, 0.001, aliases = setOf("mN⋅m", "mN·m", "mN-m")))
            UnitRegistry.register(UnitDefinition("dNm", "decinewton-meter", UnitCategory.TORQUE, 0.1, aliases = setOf("dN⋅m", "dN·m", "dN-m")))
            UnitRegistry.register(UnitDefinition("ft-lb", "foot-pound", UnitCategory.TORQUE, 1.35581795, aliases = setOf("ft⋅lb", "ft·lb", "ftlb", "lb-ft", "ft-lbf")))
            UnitRegistry.register(UnitDefinition("in-lb", "inch-pound", UnitCategory.TORQUE, 0.1129848, aliases = setOf("in⋅lb", "in·lb", "inlb", "lb-in")))
            UnitRegistry.register(UnitDefinition("kgf⋅m", "kilogram-force meter", UnitCategory.TORQUE, 9.80665, aliases = setOf("kgf-m", "kgf·m", "kgfm")))
            UnitRegistry.register(UnitDefinition("ozf⋅in", "ounce-force inch", UnitCategory.TORQUE, 0.00706155, aliases = setOf("ozf-in", "ozf·in", "ozfin")))
        }
        
        /**
         * Parse torque from string input
         * @param input String like "100Nm", "50ft-lb", etc.
         * @return Torque object
         */
        @JvmStatic
        fun parse(input: String): Torque {
            val trimmed = input.trim()
            val parts = parseValueAndUnit(trimmed)
            val value = parts.first
            val unit = parts.second
            
            val definition = UnitRegistry.getDefinition(unit)
                ?: throw IllegalArgumentException("Unknown torque unit: $unit")
            
            if (definition.category != UnitCategory.TORQUE) {
                throw IllegalArgumentException("$unit is not a torque unit")
            }
            
            val baseValue = value * definition.baseRatio
            return Torque(baseValue, definition.symbol, definition.displayName)
        }
        
        /**
         * Create torque with value and unit
         */
        @JvmStatic
        fun of(value: Double, unit: String): Torque {
            return parse("$value$unit")
        }
        
        // Factory methods for common units
        @JvmStatic
        fun newtonMeters(value: Double) = Torque(value, "Nm", "Newton-meter")
        
        @JvmStatic
        fun kilonewtonMeters(value: Double) = Torque(value * 1000.0, "kNm", "kilonewton-meter")
        
        @JvmStatic
        fun millinewtonMeters(value: Double) = Torque(value / 1000.0, "mNm", "millinewton-meter")
        
        @JvmStatic
        fun footPounds(value: Double) = Torque(value * 1.35581795, "ft-lb", "foot-pound")
        
        @JvmStatic
        fun inchPounds(value: Double) = Torque(value * 0.1129848, "in-lb", "inch-pound")
        
        @JvmStatic
        fun kilogramForceMeters(value: Double) = Torque(value * 9.80665, "kgf⋅m", "kilogram-force meter")
        
        @JvmStatic
        fun ounceForceInches(value: Double) = Torque(value * 0.00706155, "ozf⋅in", "ounce-force inch")
        
        @JvmStatic
        fun deciNewtonMeters(value: Double) = Torque(value / 10.0, "dNm", "decinewton-meter")
        
        /**
         * Parse value and unit from input string
         */
        private fun parseValueAndUnit(input: String): Pair<Double, String> {
            val regex = Regex("""^([+-]?\d*\.?\d+(?:[eE][+-]?\d+)?)\s*(.+)$""")
            val matchResult = regex.find(input)
                ?: throw IllegalArgumentException("Invalid format: $input. Expected format: '100Nm' or '50 ft-lb'")
            
            val value = matchResult.groupValues[1].toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid number: ${matchResult.groupValues[1]}")
            
            val unit = matchResult.groupValues[2].trim()
            
            return Pair(value, unit)
        }
    }
    
    /**
     * Convert to another torque unit
     */
    override fun to(targetSymbol: String): Torque {
        val targetDefinition = UnitRegistry.getDefinition(targetSymbol)
            ?: throw IllegalArgumentException("Unknown torque unit: $targetSymbol")
        
        if (targetDefinition.category != UnitCategory.TORQUE) {
            throw IllegalArgumentException("$targetSymbol is not a torque unit")
        }
        
        return Torque(
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
    ): Torque {
        return Torque(baseValue, symbol, displayName, precision, roundingMode)
    }
    
    /**
     * Current value in the unit
     */
    val value: Double
        get() {
            val definition = UnitRegistry.getDefinition(symbol)
                ?: throw IllegalStateException("Unknown unit: $symbol")
            return baseValue / definition.baseRatio
        }
    
    /**
     * The torque value in Newton-meters (base unit)
     */
    val newtonMeters: Double
        get() = baseValue
    
    /**
     * The torque value in kilonewton-meters
     */
    val kilonewtonMeters: Double
        get() = baseValue / 1000.0
    
    /**
     * The torque value in foot-pounds
     */
    val footPounds: Double
        get() = baseValue / 1.35581795
    
    /**
     * The torque value in inch-pounds
     */
    val inchPounds: Double
        get() = baseValue / 0.1129848
    
    // Arithmetic operations
    operator fun plus(other: Torque): Torque {
        return Torque(
            baseValue = this.baseValue + other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    operator fun minus(other: Torque): Torque {
        return Torque(
            baseValue = this.baseValue - other.baseValue,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    operator fun times(factor: Double): Torque {
        return Torque(
            baseValue = this.baseValue * factor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    operator fun div(factor: Double): Torque {
        require(factor != 0.0) { "Cannot divide by zero" }
        return Torque(
            baseValue = this.baseValue / factor,
            symbol = this.symbol,
            displayName = this.displayName,
            precision = this.precision,
            roundingMode = this.roundingMode
        )
    }
    
    // Comparison operators
    operator fun compareTo(other: Torque): Int = newtonMeters.compareTo(other.newtonMeters)
    
    // Torque-specific calculations
    
    /**
     * Calculates the power output at a given rotational speed
     * @param rpm Rotational speed in revolutions per minute
     * @return Power in watts
     */
    fun powerAtRPM(rpm: Double): Double {
        val angularVelocity = rpm * 2 * Math.PI / 60.0 // Convert RPM to rad/s
        return newtonMeters * angularVelocity
    }
    
    /**
     * Calculates the power output at a given angular velocity
     * @param angularVelocity Angular velocity in radians per second
     * @return Power in watts
     */
    fun powerAtAngularVelocity(angularVelocity: Double): Double {
        return newtonMeters * angularVelocity
    }
    
    /**
     * Calculates the force at a given lever arm distance
     * @param distance Distance from pivot point in meters
     * @return Force in newtons
     */
    fun forceAtDistance(distance: Double): Double {
        require(distance > 0) { "Distance must be positive" }
        return newtonMeters / distance
    }
    
    /**
     * Calculates the lever arm distance required to produce a given force
     * @param force Force in newtons
     * @return Distance in meters
     */
    fun distanceForForce(force: Double): Double {
        require(force > 0) { "Force must be positive" }
        return newtonMeters / force
    }
    
    /**
     * Returns the torque scale category based on the magnitude
     */
    fun getTorqueScale(): TorqueScale {
        return when (newtonMeters) {
            in 0.0..0.1 -> TorqueScale.MICRO
            in 0.1..1.0 -> TorqueScale.SMALL
            in 1.0..10.0 -> TorqueScale.HAND_TOOL
            in 10.0..100.0 -> TorqueScale.POWER_TOOL
            in 100.0..1000.0 -> TorqueScale.AUTOMOTIVE
            in 1000.0..10000.0 -> TorqueScale.INDUSTRIAL
            in 10000.0..100000.0 -> TorqueScale.HEAVY_MACHINERY
            else -> TorqueScale.MASSIVE
        }
    }
    
    /**
     * Suggests the best unit for displaying this torque value
     */
    fun suggestBestUnit(): String {
        return when {
            newtonMeters < 0.001 -> "mNm"
            newtonMeters < 0.1 -> "ozf⋅in"
            newtonMeters < 1.0 -> "in-lb"
            newtonMeters < 100.0 -> "Nm"
            newtonMeters < 1000.0 -> "ft-lb"
            newtonMeters < 10000.0 -> "kNm"
            else -> "kNm"
        }
    }
    
    /**
     * Formats the torque for automotive applications (commonly ft-lb)
     */
    fun toAutomotiveFormat(): String {
        val ftLb = to("ft-lb")
        return String.format("%.1f ft-lb", ftLb.value)
    }
    
    /**
     * Formats the torque for engineering applications (SI units)
     */
    fun toEngineeringFormat(): String {
        return when {
            newtonMeters < 1.0 -> String.format("%.3f N⋅m", newtonMeters)
            newtonMeters < 1000.0 -> String.format("%.2f N⋅m", newtonMeters)
            else -> String.format("%.3f kN⋅m", newtonMeters / 1000.0)
        }
    }
    
    /**
     * Calculates the work done by this torque over a given angle
     * @param angleRadians Angle of rotation in radians
     * @return Work done in joules
     */
    fun workOverAngle(angleRadians: Double): Double {
        return newtonMeters * angleRadians
    }
    
    /**
     * Calculates the angle of rotation for a given amount of work
     * @param workJoules Work in joules
     * @return Angle in radians
     */
    fun angleForWork(workJoules: Double): Double {
        require(newtonMeters > 0) { "Torque must be positive to calculate angle" }
        return workJoules / newtonMeters
    }
    
    /**
     * Formatted display with proper precision
     */
    override fun format(locale: String?): String {
        val formattedValue = if (precision >= 0) {
            val roundedValue = applyRounding(value, precision)
            String.format("%.${precision}f", roundedValue)
        } else {
            value.toString()
        }
        
        return "$formattedValue $symbol"
    }
}

/**
 * Enum representing different scales of torque for categorization
 */
enum class TorqueScale(val description: String) {
    MICRO("Micro-scale torque (< 0.1 N⋅m)"),
    SMALL("Small torque (0.1-1 N⋅m)"),
    HAND_TOOL("Hand tool torque (1-10 N⋅m)"),
    POWER_TOOL("Power tool torque (10-100 N⋅m)"),
    AUTOMOTIVE("Automotive torque (100-1000 N⋅m)"),
    INDUSTRIAL("Industrial torque (1-10 kN⋅m)"),
    HEAVY_MACHINERY("Heavy machinery torque (10-100 kN⋅m)"),
    MASSIVE("Massive torque (> 100 kN⋅m)")
}