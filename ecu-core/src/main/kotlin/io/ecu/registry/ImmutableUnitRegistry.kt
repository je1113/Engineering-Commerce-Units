package io.ecu.registry

import io.ecu.UnitCategory
import io.ecu.UnitDefinition
import io.ecu.CustomUnitBuilder
import io.ecu.customUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * 불변 단위 레지스트리
 * 
 * 한 번 빌드되면 변경할 수 없는 스레드 안전한 레지스트리입니다.
 * 
 * @since 1.1.0
 */
public class ImmutableUnitRegistry private constructor(
    private val definitions: Map<String, UnitDefinition>,
    private val categoryIndex: Map<UnitCategory, Set<String>>
) {
    
    /**
     * 심볼로 단위 정의 조회
     */
    public fun getDefinition(symbol: String): UnitDefinition? {
        return definitions[symbol.lowercase()]
    }
    
    /**
     * 카테고리별 모든 단위 조회
     */
    public fun getUnitsByCategory(category: UnitCategory): Set<String> {
        return categoryIndex[category] ?: emptySet()
    }
    
    /**
     * 두 단위 간 변환 비율 계산
     */
    public fun getConversionRatio(fromSymbol: String, toSymbol: String): Double? {
        val fromDef = getDefinition(fromSymbol) ?: return null
        val toDef = getDefinition(toSymbol) ?: return null
        
        if (fromDef.category != toDef.category) return null
        
        return fromDef.baseRatio / toDef.baseRatio
    }
    
    /**
     * 단위가 유효한지 확인
     */
    public fun isValidUnit(symbol: String): Boolean {
        return definitions.containsKey(symbol.lowercase())
    }
    
    /**
     * 카테고리의 기본 단위 조회
     */
    public fun getBaseUnit(category: UnitCategory): String? {
        return definitions.values
            .find { it.category == category && it.isBaseUnit }
            ?.symbol
    }
    
    /**
     * 등록된 모든 단위 심볼
     */
    public fun getAllUnits(): Set<String> {
        return definitions.keys.toSet()
    }
    
    /**
     * 레지스트리 빌더
     */
    public class Builder {
        private val definitions = mutableMapOf<String, UnitDefinition>()
        
        /**
         * 단위 정의 등록
         */
        public fun register(definition: UnitDefinition): Builder = apply {
            definitions[definition.symbol.lowercase()] = definition
            
            // 별명들도 등록
            definition.aliases.forEach { alias ->
                definitions[alias.lowercase()] = definition
            }
        }
        
        /**
         * 커스텀 단위 등록 (DSL 스타일)
         */
        public fun registerCustomUnit(block: CustomUnitBuilder.() -> Unit): Builder = apply {
            val definition = customUnit(block)
            register(definition)
        }
        
        /**
         * 다른 레지스트리의 정의들을 가져오기
         */
        public fun importFrom(other: ImmutableUnitRegistry): Builder = apply {
            definitions.putAll(other.definitions)
        }
        
        /**
         * 불변 레지스트리 생성
         */
        public fun build(): ImmutableUnitRegistry {
            // 카테고리 인덱스 생성
            val categoryIndex = definitions.values
                .groupBy { it.category }
                .mapValues { (_, defs) -> defs.map { it.symbol }.toSet() }
            
            return ImmutableUnitRegistry(
                definitions = definitions.toMap(),
                categoryIndex = categoryIndex
            )
        }
    }
    
    companion object {
        /**
         * 빌더 생성
         */
        @JvmStatic
        public fun builder(): Builder = Builder()
        
        /**
         * 기본 단위들이 포함된 레지스트리 생성
         */
        @JvmStatic
        public fun withDefaults(): ImmutableUnitRegistry {
            return builder().apply {
                // 길이 단위
                register(UnitDefinition("m", "meter", UnitCategory.LENGTH, 1.0, true, setOf("meter", "meters")))
                register(UnitDefinition("cm", "centimeter", UnitCategory.LENGTH, 0.01, aliases = setOf("centimeter", "centimeters")))
                register(UnitDefinition("mm", "millimeter", UnitCategory.LENGTH, 0.001, aliases = setOf("millimeter", "millimeters")))
                register(UnitDefinition("km", "kilometer", UnitCategory.LENGTH, 1000.0, aliases = setOf("kilometer", "kilometers")))
                register(UnitDefinition("in", "inch", UnitCategory.LENGTH, 0.0254, aliases = setOf("inch", "inches")))
                register(UnitDefinition("ft", "foot", UnitCategory.LENGTH, 0.3048, aliases = setOf("foot", "feet")))
                register(UnitDefinition("yd", "yard", UnitCategory.LENGTH, 0.9144, aliases = setOf("yard", "yards")))
                register(UnitDefinition("mi", "mile", UnitCategory.LENGTH, 1609.344, aliases = setOf("mile", "miles")))
                
                // 무게 단위
                register(UnitDefinition("kg", "kilogram", UnitCategory.WEIGHT, 1.0, true, setOf("kilogram", "kilograms")))
                register(UnitDefinition("g", "gram", UnitCategory.WEIGHT, 0.001, aliases = setOf("gram", "grams")))
                register(UnitDefinition("mg", "milligram", UnitCategory.WEIGHT, 0.000001, aliases = setOf("milligram", "milligrams")))
                register(UnitDefinition("t", "metric ton", UnitCategory.WEIGHT, 1000.0, aliases = setOf("ton", "tons", "tonne", "tonnes")))
                register(UnitDefinition("lb", "pound", UnitCategory.WEIGHT, 0.453592, aliases = setOf("pound", "pounds", "lbs")))
                register(UnitDefinition("oz", "ounce", UnitCategory.WEIGHT, 0.0283495, aliases = setOf("ounce", "ounces")))
                
                // 추가 단위들은 동일한 패턴으로...
            }.build()
        }
    }
}

/**
 * 스레드 안전한 가변 레지스트리 래퍼
 * 
 * 런타임에 단위를 추가해야 하는 경우 사용합니다.
 * 
 * @since 1.1.0
 */
public class ThreadSafeUnitRegistry {
    private val lock = ReentrantReadWriteLock()
    private var registry: ImmutableUnitRegistry = ImmutableUnitRegistry.withDefaults()
    
    /**
     * 단위 정의 조회 (읽기 작업)
     */
    public fun getDefinition(symbol: String): UnitDefinition? = lock.read {
        registry.getDefinition(symbol)
    }
    
    /**
     * 새로운 단위 등록 (쓰기 작업)
     */
    public fun register(definition: UnitDefinition) = lock.write {
        registry = ImmutableUnitRegistry.builder()
            .importFrom(registry)
            .register(definition)
            .build()
    }
    
    /**
     * 커스텀 단위 등록 (쓰기 작업)
     */
    public fun registerCustomUnit(block: CustomUnitBuilder.() -> Unit) = lock.write {
        registry = ImmutableUnitRegistry.builder()
            .importFrom(registry)
            .registerCustomUnit(block)
            .build()
    }
    
    /**
     * 현재 레지스트리 스냅샷 가져오기
     */
    public fun snapshot(): ImmutableUnitRegistry = lock.read {
        registry
    }
    
    /**
     * 레지스트리 교체
     */
    public fun replaceWith(newRegistry: ImmutableUnitRegistry) = lock.write {
        registry = newRegistry
    }
}
