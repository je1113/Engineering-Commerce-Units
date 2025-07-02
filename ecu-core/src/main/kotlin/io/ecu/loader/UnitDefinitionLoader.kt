package io.ecu.loader

import io.ecu.UnitCategory
import io.ecu.UnitDefinition
import io.ecu.UnitRegistry
import io.ecu.CustomConversionRegistry

/**
 * 단위 정의를 외부 소스에서 로드하기 위한 인터페이스
 * 
 * @since 1.1.0
 */
public interface UnitDefinitionLoader {
    /**
     * 단위 정의들을 로드
     * 
     * @return 로드된 단위 정의 목록
     */
    fun load(): List<UnitDefinitionData>
}

/**
 * 단위 정의 데이터 구조
 * 
 * JSON/YAML 파싱을 위한 데이터 클래스
 */
public data class UnitDefinitionData(
    val symbol: String,
    val displayName: String,
    val category: String,
    val baseRatio: Double,
    val isBaseUnit: Boolean = false,
    val aliases: List<String> = emptyList(),
    val conversions: Map<String, ConversionData> = emptyMap()
)

/**
 * 변환 규칙 데이터
 */
public data class ConversionData(
    val factor: Double? = null,
    val formula: String? = null,
    val type: ConversionType = ConversionType.MULTIPLICATION
)

/**
 * 변환 타입
 */
public enum class ConversionType {
    MULTIPLICATION,  // 단순 곱셈
    DIVISION,       // 단순 나눗셈
    FORMULA,        // 복잡한 수식
    CUSTOM          // 커스텀 로직
}

/**
 * 문자열 기반 단위 정의 로더
 * 
 * JSON 형식의 문자열에서 단위를 로드합니다.
 */
public class StringUnitDefinitionLoader(
    private val jsonString: String
) : UnitDefinitionLoader {
    
    override fun load(): List<UnitDefinitionData> {
        // 간단한 JSON 파싱 (실제로는 Jackson이나 Kotlinx.serialization 사용 권장)
        return parseJson(jsonString)
    }
    
    private fun parseJson(json: String): List<UnitDefinitionData> {
        // 이것은 간단한 예제입니다. 실제로는 적절한 JSON 라이브러리를 사용해야 합니다.
        val units = mutableListOf<UnitDefinitionData>()
        
        // 매우 간단한 파싱 로직 (데모용)
        if (json.contains("widget")) {
            units.add(
                UnitDefinitionData(
                    symbol = "widget",
                    displayName = "Widget",
                    category = "QUANTITY",
                    baseRatio = 1.0,
                    aliases = listOf("widgets", "wgt"),
                    conversions = mapOf(
                        "box" to ConversionData(factor = 12.0),
                        "pallet" to ConversionData(factor = 480.0)
                    )
                )
            )
        }
        
        return units
    }
}

/**
 * 단위 정의를 레지스트리에 적용하는 유틸리티
 */
public object UnitDefinitionApplier {
    
    /**
     * 로더에서 가져온 정의들을 레지스트리에 등록
     */
    @JvmStatic
    public fun applyDefinitions(loader: UnitDefinitionLoader) {
        val definitions = loader.load()
        
        definitions.forEach { data ->
            // UnitDefinition 생성 및 등록
            val category = try {
                UnitCategory.valueOf(data.category.uppercase())
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid category: ${data.category}")
            }
            
            val definition = UnitDefinition(
                symbol = data.symbol,
                displayName = data.displayName,
                category = category,
                baseRatio = data.baseRatio,
                isBaseUnit = data.isBaseUnit,
                aliases = data.aliases.toSet()
            )
            
            UnitRegistry.register(definition)
            
            // 커스텀 변환 규칙 등록
            if (data.conversions.isNotEmpty()) {
                val customConversions = mutableMapOf<String, (Double) -> Double?>()
                
                data.conversions.forEach { (targetUnit, conversion) ->
                    val converter: (Double) -> Double? = when (conversion.type) {
                        ConversionType.MULTIPLICATION -> { value ->
                            conversion.factor?.let { value * it }
                        }
                        ConversionType.DIVISION -> { value ->
                            conversion.factor?.let { value / it }
                        }
                        ConversionType.FORMULA -> { value ->
                            // 수식 평가 (실제로는 expression evaluator 사용)
                            evaluateFormula(conversion.formula ?: "", value)
                        }
                        ConversionType.CUSTOM -> { value ->
                            // 커스텀 로직은 별도로 등록해야 함
                            null
                        }
                    }
                    
                    customConversions[targetUnit] = converter
                }
                
                CustomConversionRegistry.registerConversions(data.symbol, customConversions)
            }
        }
    }
    
    /**
     * 간단한 수식 평가 (데모용)
     */
    private fun evaluateFormula(formula: String, value: Double): Double? {
        // 실제로는 수식 평가 라이브러리 사용
        // 예: "value * 2 + 10" 같은 수식 처리
        return when {
            formula.contains("*") && formula.contains("+") -> {
                // 매우 간단한 예제
                val parts = formula.split("+")
                if (parts.size == 2) {
                    val multPart = parts[0].trim()
                    val addPart = parts[1].trim().toDoubleOrNull() ?: 0.0
                    
                    if (multPart.contains("value *")) {
                        val factor = multPart.replace("value *", "").trim().toDoubleOrNull() ?: 1.0
                        value * factor + addPart
                    } else null
                } else null
            }
            else -> null
        }
    }
}
