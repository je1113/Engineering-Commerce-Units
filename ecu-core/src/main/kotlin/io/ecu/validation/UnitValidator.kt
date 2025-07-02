package io.ecu.validation

import io.ecu.UnitCategory
import io.ecu.UnitRegistry

/**
 * 단위 변환 입력값 검증을 위한 유틸리티
 * 
 * @since 1.1.0
 */
public object UnitValidator {
    
    /**
     * 입력값 검증 규칙
     */
    public data class ValidationRule(
        val name: String,
        val check: (Double, String) -> ValidationResult
    )
    
    /**
     * 검증 결과
     */
    public sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }
    
    /**
     * 기본 검증 규칙
     */
    private val defaultRules = listOf(
        ValidationRule("finite_check") { value, _ ->
            if (!value.isFinite()) {
                ValidationResult.Invalid("Value must be finite")
            } else {
                ValidationResult.Valid
            }
        },
        ValidationRule("nan_check") { value, _ ->
            if (value.isNaN()) {
                ValidationResult.Invalid("Value cannot be NaN")
            } else {
                ValidationResult.Valid
            }
        },
        ValidationRule("unit_format") { _, unit ->
            if (!isValidUnitFormat(unit)) {
                ValidationResult.Invalid("Invalid unit format: $unit")
            } else {
                ValidationResult.Valid
            }
        }
    )
    
    /**
     * 카테고리별 특수 규칙
     */
    private val categoryRules = mapOf(
        UnitCategory.TEMPERATURE to listOf(
            ValidationRule("absolute_zero") { value, unit ->
                val definition = UnitRegistry.getDefinition(unit)
                if (definition != null && definition.category == UnitCategory.TEMPERATURE) {
                    val kelvin = when (unit.lowercase()) {
                        "k", "kelvin" -> value
                        "°c", "c", "celsius" -> value + 273.15
                        "°f", "f", "fahrenheit" -> (value + 459.67) * 5/9
                        else -> value
                    }
                    if (kelvin < 0) {
                        ValidationResult.Invalid("Temperature cannot be below absolute zero")
                    } else {
                        ValidationResult.Valid
                    }
                } else {
                    ValidationResult.Valid
                }
            }
        ),
        UnitCategory.LENGTH to listOf(
            ValidationRule("non_negative") { value, _ ->
                if (value < 0) {
                    ValidationResult.Invalid("Length cannot be negative")
                } else {
                    ValidationResult.Valid
                }
            }
        ),
        UnitCategory.WEIGHT to listOf(
            ValidationRule("non_negative") { value, _ ->
                if (value < 0) {
                    ValidationResult.Invalid("Weight cannot be negative")
                } else {
                    ValidationResult.Valid
                }
            }
        ),
        UnitCategory.VOLUME to listOf(
            ValidationRule("non_negative") { value, _ ->
                if (value < 0) {
                    ValidationResult.Invalid("Volume cannot be negative")
                } else {
                    ValidationResult.Valid
                }
            }
        )
    )
    
    /**
     * 값과 단위 검증
     * 
     * @param value 검증할 값
     * @param unit 단위
     * @return 검증 결과
     */
    @JvmStatic
    public fun validate(value: Double, unit: String): ValidationResult {
        // 기본 규칙 검증
        for (rule in defaultRules) {
            val result = rule.check(value, unit)
            if (result is ValidationResult.Invalid) {
                return result
            }
        }
        
        // 단위가 존재하는지 확인
        val definition = UnitRegistry.getDefinition(unit)
            ?: return ValidationResult.Invalid("Unknown unit: $unit")
        
        // 카테고리별 특수 규칙 검증
        val specificRules = categoryRules[definition.category] ?: emptyList()
        for (rule in specificRules) {
            val result = rule.check(value, unit)
            if (result is ValidationResult.Invalid) {
                return result
            }
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * 값과 단위를 검증하고 예외 발생
     * 
     * @throws IllegalArgumentException 검증 실패 시
     */
    @JvmStatic
    public fun validateOrThrow(value: Double, unit: String) {
        when (val result = validate(value, unit)) {
            is ValidationResult.Invalid -> throw IllegalArgumentException(result.reason)
            is ValidationResult.Valid -> { /* OK */ }
        }
    }
    
    /**
     * 단위 형식 검증
     */
    private fun isValidUnitFormat(unit: String): Boolean {
        // 기본 패턴: 알파벳, 숫자, 특수문자(°, ², ³, -, _, 괄호)
        val pattern = Regex("^[a-zA-Z0-9°²³\\-_()]+$")
        return unit.matches(pattern)
    }
    
    /**
     * 범위 검증
     */
    @JvmStatic
    public fun validateRange(
        value: Double,
        unit: String,
        min: Double? = null,
        max: Double? = null
    ): ValidationResult {
        // 기본 검증 수행
        val basicValidation = validate(value, unit)
        if (basicValidation is ValidationResult.Invalid) {
            return basicValidation
        }
        
        // 범위 검증
        if (min != null && value < min) {
            return ValidationResult.Invalid("Value $value is below minimum $min")
        }
        
        if (max != null && value > max) {
            return ValidationResult.Invalid("Value $value is above maximum $max")
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * 커스텀 검증 규칙 추가
     */
    private val customRules = mutableMapOf<UnitCategory, MutableList<ValidationRule>>()
    
    /**
     * 커스텀 검증 규칙 등록
     */
    @JvmStatic
    public fun registerRule(category: UnitCategory, rule: ValidationRule) {
        customRules.getOrPut(category) { mutableListOf() }.add(rule)
    }
    
    /**
     * 특정 카테고리의 모든 규칙 가져오기
     */
    @JvmStatic
    public fun getRulesForCategory(category: UnitCategory): List<ValidationRule> {
        val rules = mutableListOf<ValidationRule>()
        rules.addAll(defaultRules)
        categoryRules[category]?.let { rules.addAll(it) }
        customRules[category]?.let { rules.addAll(it) }
        return rules
    }
}

/**
 * 단위 변환 시 검증을 수행하는 래퍼 클래스
 */
public class ValidatedUnit<T : io.ecu.Unit<T>>(
    private val unit: T,
    private val validator: (Double, String) -> UnitValidator.ValidationResult = { v, u -> 
        UnitValidator.validate(v, u) 
    }
) {
    init {
        // 생성 시점에 검증
        val result = validator(unit.baseValue, unit.symbol)
        if (result is UnitValidator.ValidationResult.Invalid) {
            throw IllegalArgumentException("Invalid unit: ${result.reason}")
        }
    }
    
    /**
     * 검증된 변환
     */
    fun to(targetSymbol: String): T {
        val converted = unit.to(targetSymbol)
        val result = validator(converted.baseValue, targetSymbol)
        if (result is UnitValidator.ValidationResult.Invalid) {
            throw IllegalArgumentException("Conversion resulted in invalid value: ${result.reason}")
        }
        return converted
    }
    
    /**
     * 원본 단위 접근
     */
    fun unwrap(): T = unit
}
