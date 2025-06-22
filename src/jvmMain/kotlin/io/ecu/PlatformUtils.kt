package io.ecu

import java.math.BigDecimal
import java.math.RoundingMode as JavaRoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * JVM 플랫폼에서의 숫자 포맷팅 구현
 */
actual fun formatNumber(value: Double, precision: Int, roundingMode: RoundingMode): String {
    val rounded = applyRounding(value, precision, roundingMode)
    val format = DecimalFormat().apply {
        minimumFractionDigits = precision
        maximumFractionDigits = precision
        isGroupingUsed = false
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    }
    return format.format(rounded)
}

/**
 * JVM 플랫폼에서의 반올림 구현
 */
actual fun applyRounding(value: Double, digits: Int, mode: RoundingMode): Double {
    val bigDecimal = BigDecimal(value.toString())
    val javaMode = when (mode) {
        RoundingMode.HALF_UP -> JavaRoundingMode.HALF_UP
        RoundingMode.HALF_DOWN -> JavaRoundingMode.HALF_DOWN
        RoundingMode.HALF_EVEN -> JavaRoundingMode.HALF_EVEN
        RoundingMode.UP -> JavaRoundingMode.UP
        RoundingMode.DOWN -> JavaRoundingMode.DOWN
    }
    return bigDecimal.setScale(digits, javaMode).toDouble()
}
