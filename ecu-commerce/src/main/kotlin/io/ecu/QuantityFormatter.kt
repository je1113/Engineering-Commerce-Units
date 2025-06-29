package io.ecu

import kotlin.math.floor

/**
 * 수량 표시 형식 포맷터
 * 지역별 형식, 약어 표현, 복합 단위 표현을 지원합니다.
 */
class QuantityFormatter(
    private val locale: Locale = Locale.US,
    private val options: FormattingOptions = FormattingOptions()
) {
    
    /**
     * 지역 설정
     */
    enum class Locale {
        US,      // 1,234.56
        EU,      // 1.234,56
        KR,      // 1,234.56
        JP,      // 1,234
        CN       // 1,234.56
    }
    
    /**
     * 포맷팅 옵션
     */
    data class FormattingOptions(
        /** 약어 사용 여부 */
        val useAbbreviations: Boolean = true,
        
        /** 복합 단위 표현 사용 여부 */
        val useCompoundUnits: Boolean = true,
        
        /** 소수점 자릿수 */
        val decimalPlaces: Int = 2,
        
        /** 천 단위 구분자 표시 여부 */
        val useThousandsSeparator: Boolean = true,
        
        /** 영(0) 값 표시 여부 */
        val showZeroValues: Boolean = false,
        
        /** 단위 표시 스타일 */
        val unitStyle: UnitStyle = UnitStyle.ABBREVIATED,
        
        /** 복합 단위 구분자 */
        val compoundSeparator: String = " + ",
        
        /** 음수 표시 형식 */
        val negativeFormat: NegativeFormat = NegativeFormat.MINUS_SIGN
    )
    
    /**
     * 단위 표시 스타일
     */
    enum class UnitStyle {
        ABBREVIATED,    // pcs, dz, bx
        SHORT,         // pc, doz, box
        FULL,          // pieces, dozens, boxes
        SYMBOL         // #, dz, □
    }
    
    /**
     * 음수 표시 형식
     */
    enum class NegativeFormat {
        MINUS_SIGN,     // -123
        PARENTHESES,    // (123)
        RED_COLOR       // 123 (색상으로 표시)
    }
    
    /**
     * 단위 약어 매핑
     */
    private val abbreviations = mapOf(
        // 기본 수량 단위
        "piece" to "pcs",
        "pieces" to "pcs",
        "dozen" to "dz",
        "dozens" to "dz",
        "gross" to "gr",
        "ream" to "rm",
        "score" to "sc",
        
        // 포장 단위
        "box" to "bx",
        "boxes" to "bx",
        "carton" to "ctn",
        "cartons" to "ctn",
        "case" to "cs",
        "cases" to "cs",
        "pallet" to "plt",
        "pallets" to "plt",
        "package" to "pkg",
        "packages" to "pkg",
        
        // 특수 단위
        "each" to "ea",
        "unit" to "u",
        "units" to "u",
        "pack" to "pk",
        "packs" to "pk",
        "bundle" to "bdl",
        "bundles" to "bdl",
        "set" to "set",
        "sets" to "set"
    )
    
    /**
     * 단위 기호 매핑
     */
    private val symbols = mapOf(
        "piece" to "#",
        "pieces" to "#",
        "dozen" to "dz",
        "dozens" to "dz",
        "box" to "□",
        "boxes" to "□",
        "carton" to "📦",
        "pallet" to "▭"
    )
    
    /**
     * 수량 포맷팅
     */
    fun format(quantity: Quantity): String {
        val value = quantity.baseValue / (UnitRegistry.getDefinition(quantity.symbol)?.factor ?: 1.0)
        val unit = formatUnit(quantity.symbol, value)
        
        return if (options.useCompoundUnits && value > 1) {
            formatCompound(quantity)
        } else {
            "${formatNumber(value)} $unit"
        }
    }
    
    /**
     * 복합 단위 포맷팅
     */
    fun formatCompound(quantity: Quantity): String {
        val conversionService = ConversionService()
        val optimal = conversionService.suggestOptimalUnit(quantity)
        
        return optimal.recommended?.let { suggestion ->
            val components = suggestion.components
                .filter { options.showZeroValues || it.value > 0 }
                .map { component ->
                    val formattedValue = formatNumber(component.value)
                    val unit = formatUnit(component.unit, component.value)
                    "$formattedValue $unit"
                }
            
            components.joinToString(options.compoundSeparator)
        } ?: format(quantity)
    }
    
    /**
     * 숫자 포맷팅
     */
    private fun formatNumber(value: Double): String {
        // 음수 처리
        val absValue = kotlin.math.abs(value)
        val isNegative = value < 0
        
        // 정수 부분과 소수 부분 분리
        val integerPart = floor(absValue).toLong()
        val decimalPart = absValue - integerPart
        
        // 정수 부분 포맷팅
        var formattedInteger = formatIntegerPart(integerPart)
        
        // 소수 부분 포맷팅
        val formattedDecimal = if (options.decimalPlaces > 0 && (decimalPart > 0 || options.showZeroValues)) {
            formatDecimalPart(decimalPart)
        } else {
            ""
        }
        
        // 조합
        val formatted = if (formattedDecimal.isNotEmpty()) {
            "$formattedInteger${getDecimalSeparator()}$formattedDecimal"
        } else {
            formattedInteger
        }
        
        // 음수 표시
        return if (isNegative) {
            when (options.negativeFormat) {
                NegativeFormat.MINUS_SIGN -> "-$formatted"
                NegativeFormat.PARENTHESES -> "($formatted)"
                NegativeFormat.RED_COLOR -> formatted // 실제로는 색상 정보와 함께 반환
            }
        } else {
            formatted
        }
    }
    
    /**
     * 정수 부분 포맷팅
     */
    private fun formatIntegerPart(value: Long): String {
        val str = value.toString()
        
        if (!options.useThousandsSeparator || str.length <= 3) {
            return str
        }
        
        val separator = getThousandsSeparator()
        val result = StringBuilder()
        
        for (i in str.indices) {
            if (i > 0 && (str.length - i) % 3 == 0) {
                result.append(separator)
            }
            result.append(str[i])
        }
        
        return result.toString()
    }
    
    /**
     * 소수 부분 포맷팅
     */
    private fun formatDecimalPart(value: Double): String {
        val precision = options.decimalPlaces
        val factor = kotlin.math.pow(10.0, precision.toDouble())
        val rounded = kotlin.math.round(value * factor).toInt()
        
        return rounded.toString().padStart(precision, '0').take(precision)
    }
    
    /**
     * 천 단위 구분자
     */
    private fun getThousandsSeparator(): String {
        return when (locale) {
            Locale.US, Locale.KR, Locale.JP, Locale.CN -> ","
            Locale.EU -> "."
        }
    }
    
    /**
     * 소수점 구분자
     */
    private fun getDecimalSeparator(): String {
        return when (locale) {
            Locale.US, Locale.KR, Locale.CN -> "."
            Locale.EU -> ","
            Locale.JP -> "."
        }
    }
    
    /**
     * 단위 포맷팅
     */
    private fun formatUnit(unit: String, value: Double): String {
        val plural = value != 1.0
        
        return when (options.unitStyle) {
            UnitStyle.ABBREVIATED -> {
                abbreviations[unit.lowercase()] ?: unit
            }
            UnitStyle.SHORT -> {
                // 짧은 형식 (약어보다는 길지만 전체보다는 짧음)
                when (unit.lowercase()) {
                    "piece", "pieces" -> if (plural) "pcs" else "pc"
                    "dozen", "dozens" -> "doz"
                    "box", "boxes" -> "box"
                    else -> abbreviations[unit.lowercase()] ?: unit
                }
            }
            UnitStyle.FULL -> {
                // 전체 단위명
                when (unit.lowercase()) {
                    "piece", "pcs", "pc" -> if (plural) "pieces" else "piece"
                    "dozen", "dz", "doz" -> if (plural) "dozens" else "dozen"
                    "box", "bx" -> if (plural) "boxes" else "box"
                    else -> unit
                }
            }
            UnitStyle.SYMBOL -> {
                symbols[unit.lowercase()] ?: abbreviations[unit.lowercase()] ?: unit
            }
        }
    }
    
    /**
     * 지역별 복수형 규칙
     */
    private fun getPluralForm(unit: String, value: Double, locale: Locale): String {
        // 영어권 복수형 규칙
        if (locale == Locale.US) {
            if (value == 1.0) {
                return unit
            }
            
            return when (unit) {
                "box" -> "boxes"
                "piece" -> "pieces"
                "dozen" -> "dozens"
                "gross" -> "gross" // 불변
                "series" -> "series" // 불변
                else -> {
                    if (unit.endsWith("y") && !unit.endsWith("ay") && !unit.endsWith("ey") && !unit.endsWith("oy") && !unit.endsWith("uy")) {
                        unit.dropLast(1) + "ies"
                    } else if (unit.endsWith("s") || unit.endsWith("x") || unit.endsWith("z") || unit.endsWith("ch") || unit.endsWith("sh")) {
                        unit + "es"
                    } else {
                        unit + "s"
                    }
                }
            }
        }
        
        // 다른 언어는 단순히 단위 그대로 반환
        return unit
    }
    
    companion object {
        /**
         * 기본 포맷터들
         */
        val US_STANDARD = QuantityFormatter(
            locale = Locale.US,
            options = FormattingOptions(
                useAbbreviations = true,
                unitStyle = UnitStyle.ABBREVIATED
            )
        )
        
        val EU_STANDARD = QuantityFormatter(
            locale = Locale.EU,
            options = FormattingOptions(
                useAbbreviations = true,
                unitStyle = UnitStyle.ABBREVIATED
            )
        )
        
        val COMPACT = QuantityFormatter(
            locale = Locale.US,
            options = FormattingOptions(
                useAbbreviations = true,
                unitStyle = UnitStyle.SYMBOL,
                useCompoundUnits = false,
                decimalPlaces = 0
            )
        )
        
        val VERBOSE = QuantityFormatter(
            locale = Locale.US,
            options = FormattingOptions(
                useAbbreviations = false,
                unitStyle = UnitStyle.FULL,
                useCompoundUnits = true,
                showZeroValues = true
            )
        )
        
        val ACCOUNTING = QuantityFormatter(
            locale = Locale.US,
            options = FormattingOptions(
                useAbbreviations = true,
                unitStyle = UnitStyle.ABBREVIATED,
                negativeFormat = NegativeFormat.PARENTHESES,
                decimalPlaces = 2
            )
        )
    }
}

/**
 * Quantity 확장 함수
 */
fun Quantity.format(formatter: QuantityFormatter = QuantityFormatter.US_STANDARD): String {
    return formatter.format(this)
}

fun Quantity.formatCompound(formatter: QuantityFormatter = QuantityFormatter.US_STANDARD): String {
    return formatter.formatCompound(this)
}

/**
 * 빠른 포맷팅을 위한 확장 함수들
 */
fun Quantity.toCompactString(): String = format(QuantityFormatter.COMPACT)
fun Quantity.toVerboseString(): String = format(QuantityFormatter.VERBOSE)
fun Quantity.toAccountingString(): String = format(QuantityFormatter.ACCOUNTING)
