package io.ecu.i18n

import io.ecu.Unit
import java.text.NumberFormat
import java.util.Locale
import java.util.ResourceBundle
import java.util.concurrent.ConcurrentHashMap

/**
 * 단위 포맷터 인터페이스
 * 
 * 지역화된 단위 표시를 위한 인터페이스입니다.
 * 
 * @since 1.1.0
 */
public interface UnitFormatter {
    /**
     * 단위를 포맷팅
     */
    fun format(unit: io.ecu.Unit<*>, locale: Locale = Locale.getDefault()): String
    
    /**
     * 값과 단위를 분리하여 포맷팅
     */
    fun formatValue(value: Double, unitSymbol: String, locale: Locale = Locale.getDefault()): String
    
    /**
     * 단위 심볼의 지역화된 이름 조회
     */
    fun getLocalizedUnitName(unitSymbol: String, locale: Locale = Locale.getDefault()): String?
    
    /**
     * 단위 심볼의 지역화된 약어 조회
     */
    fun getLocalizedAbbreviation(unitSymbol: String, locale: Locale = Locale.getDefault()): String?
}

/**
 * 기본 단위 포맷터 구현
 */
public class DefaultUnitFormatter : UnitFormatter {
    private val resourceBundles = ConcurrentHashMap<Locale, ResourceBundle>()
    private val numberFormatters = ConcurrentHashMap<Locale, NumberFormat>()
    
    override fun format(unit: io.ecu.Unit<*>, locale: Locale): String {
        val value = unit.baseValue / getBaseRatio(unit)
        return formatValue(value, unit.symbol, locale)
    }
    
    override fun formatValue(value: Double, unitSymbol: String, locale: Locale): String {
        val formatter = getNumberFormatter(locale)
        val formattedValue = formatter.format(value)
        val localizedUnit = getLocalizedAbbreviation(unitSymbol, locale) ?: unitSymbol
        
        // 로케일별 포맷 규칙
        return when (locale.language) {
            "fr", "de" -> "$formattedValue $localizedUnit" // 프랑스어, 독일어: 공백 포함
            "zh", "ja", "ko" -> "$formattedValue$localizedUnit" // 중국어, 일본어, 한국어: 공백 없음
            else -> "$formattedValue $localizedUnit" // 기본: 공백 포함
        }
    }
    
    override fun getLocalizedUnitName(unitSymbol: String, locale: Locale): String? {
        val bundle = getResourceBundle(locale)
        return try {
            bundle.getString("unit.name.$unitSymbol")
        } catch (e: Exception) {
            null
        }
    }
    
    override fun getLocalizedAbbreviation(unitSymbol: String, locale: Locale): String? {
        val bundle = getResourceBundle(locale)
        return try {
            bundle.getString("unit.abbr.$unitSymbol")
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getResourceBundle(locale: Locale): ResourceBundle {
        return resourceBundles.computeIfAbsent(locale) {
            try {
                ResourceBundle.getBundle("io.ecu.i18n.units", locale)
            } catch (e: Exception) {
                // 폴백: 기본 번들 사용
                ResourceBundle.getBundle("io.ecu.i18n.units", Locale.ROOT)
            }
        }
    }
    
    private fun getNumberFormatter(locale: Locale): NumberFormat {
        return numberFormatters.computeIfAbsent(locale) {
            NumberFormat.getInstance(locale)
        }
    }
    
    private fun getBaseRatio(unit: io.ecu.Unit<*>): Double {
        // 실제로는 UnitRegistry에서 조회
        return 1.0
    }
}

/**
 * 고급 포맷팅 옵션
 */
public data class FormatOptions(
    val locale: Locale = Locale.getDefault(),
    val style: FormatStyle = FormatStyle.DEFAULT,
    val unitDisplay: UnitDisplay = UnitDisplay.SHORT,
    val minimumFractionDigits: Int? = null,
    val maximumFractionDigits: Int? = null,
    val useGrouping: Boolean = true
)

/**
 * 포맷 스타일
 */
public enum class FormatStyle {
    DEFAULT,    // 기본 스타일
    COMPACT,    // 축약형 (1.5k m)
    SCIENTIFIC, // 과학적 표기법
    SPELLED_OUT // 전체 표기 (one thousand five hundred meters)
}

/**
 * 단위 표시 방법
 */
public enum class UnitDisplay {
    SHORT,  // m, kg
    NARROW, // m, kg (더 짧은 형태)
    LONG    // meters, kilograms
}

/**
 * 고급 단위 포맷터
 */
public class AdvancedUnitFormatter : UnitFormatter {
    private val defaultFormatter = DefaultUnitFormatter()
    
    override fun format(unit: io.ecu.Unit<*>, locale: Locale): String {
        return format(unit, FormatOptions(locale = locale))
    }
    
    override fun formatValue(value: Double, unitSymbol: String, locale: Locale): String {
        return defaultFormatter.formatValue(value, unitSymbol, locale)
    }
    
    override fun getLocalizedUnitName(unitSymbol: String, locale: Locale): String? {
        return defaultFormatter.getLocalizedUnitName(unitSymbol, locale)
    }
    
    override fun getLocalizedAbbreviation(unitSymbol: String, locale: Locale): String? {
        return defaultFormatter.getLocalizedAbbreviation(unitSymbol, locale)
    }
    
    public fun format(unit: io.ecu.Unit<*>, options: FormatOptions): String {
        val locale = options.locale
        val value = unit.baseValue / getBaseRatio(unit)
        
        // 숫자 포맷팅
        val numberFormat = when (options.style) {
            FormatStyle.COMPACT -> createCompactFormat(locale)
            FormatStyle.SCIENTIFIC -> createScientificFormat(locale)
            FormatStyle.SPELLED_OUT -> createSpelledOutFormat(locale)
            else -> NumberFormat.getInstance(locale)
        }
        
        // 옵션 적용
        options.minimumFractionDigits?.let { numberFormat.minimumFractionDigits = it }
        options.maximumFractionDigits?.let { numberFormat.maximumFractionDigits = it }
        numberFormat.isGroupingUsed = options.useGrouping
        
        val formattedValue = numberFormat.format(value)
        
        // 단위 포맷팅
        val unitString = when (options.unitDisplay) {
            UnitDisplay.LONG -> defaultFormatter.getLocalizedUnitName(unit.symbol, locale) ?: unit.displayName
            UnitDisplay.SHORT -> defaultFormatter.getLocalizedAbbreviation(unit.symbol, locale) ?: unit.symbol
            UnitDisplay.NARROW -> unit.symbol // 가장 짧은 형태
        }
        
        // 복수형 처리
        val finalUnitString = if (options.unitDisplay == UnitDisplay.LONG && value != 1.0) {
            getPluralForm(unitString, value, locale)
        } else {
            unitString
        }
        
        return formatByLocale(formattedValue, finalUnitString, locale)
    }
    
    private fun createCompactFormat(locale: Locale): NumberFormat {
        // Java 12+ CompactNumberFormat 또는 커스텀 구현
        return NumberFormat.getInstance(locale)
    }
    
    private fun createScientificFormat(locale: Locale): NumberFormat {
        return NumberFormat.getInstance(locale).apply {
            // 과학적 표기법 설정
        }
    }
    
    private fun createSpelledOutFormat(locale: Locale): NumberFormat {
        // RuleBasedNumberFormat 또는 커스텀 구현
        return NumberFormat.getInstance(locale)
    }
    
    private fun getPluralForm(unit: String, value: Double, locale: Locale): String {
        // 로케일별 복수형 규칙 적용
        return when (locale.language) {
            "en" -> if (value == 1.0) unit else "${unit}s"
            else -> unit
        }
    }
    
    private fun formatByLocale(value: String, unit: String, locale: Locale): String {
        return when (locale.language) {
            "fr" -> "$value $unit"
            "de" -> "$value $unit"
            "zh", "ja", "ko" -> "$value$unit"
            else -> "$value $unit"
        }
    }
    
    private fun getBaseRatio(unit: io.ecu.Unit<*>): Double = 1.0
}

/**
 * 전역 단위 포맷터
 */
public object GlobalUnitFormatter {
    private var formatter: UnitFormatter = AdvancedUnitFormatter()
    
    /**
     * 기본 포맷터 설정
     */
    @JvmStatic
    public fun setFormatter(formatter: UnitFormatter) {
        this.formatter = formatter
    }
    
    /**
     * 단위 포맷팅
     */
    @JvmStatic
    public fun format(unit: io.ecu.Unit<*>, locale: Locale = Locale.getDefault()): String {
        return formatter.format(unit, locale)
    }
    
    /**
     * 고급 포맷팅
     */
    @JvmStatic
    public fun format(unit: io.ecu.Unit<*>, options: FormatOptions): String {
        val currentFormatter = formatter
        return when (currentFormatter) {
            is AdvancedUnitFormatter -> currentFormatter.format(unit, options)
            else -> currentFormatter.format(unit, options.locale)
        }
    }
}