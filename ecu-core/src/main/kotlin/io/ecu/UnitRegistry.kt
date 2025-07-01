@file:JvmName("UnitRegistry")
package io.ecu

/**
 * 단위 정의를 저장하는 데이터 클래스
 * 
 * @property symbol 단위 기호
 * @property displayName 표시 이름
 * @property category 단위 카테고리
 * @property baseRatio 기본 단위로의 변환 비율
 * @property isBaseUnit 기본 단위 여부
 * @property aliases 별칭 목록
 */
public data class UnitDefinition(
    val symbol: String,
    val displayName: String,
    val category: UnitCategory,
    val baseRatio: Double,
    val isBaseUnit: Boolean = false,
    val aliases: Set<String> = emptySet()
)

/**
 * 단위 정의를 관리하는 레지스트리
 * 
 * ECU Core에서는 기본적인 단위들만 포함합니다:
 * - 길이 (Length): m, cm, mm, km, in, ft, yd, mi
 * - 무게 (Weight): kg, g, mg, t, lb, oz
 * - 부피 (Volume): l, ml, m³, gal, qt, pt, fl oz
 * - 온도 (Temperature): K, °C, °F
 * - 면적 (Area): m², cm², mm², km², in², ft², yd², mi², ha, ac
 */
public object UnitRegistry {
    
    private val definitions = mutableMapOf<String, UnitDefinition>()
    private val categoryMap = mutableMapOf<UnitCategory, MutableSet<String>>()
    
    init {
        registerCoreUnits()
    }
    
    /**
     * 새로운 단위 정의를 등록
     * 
     * @param definition 등록할 단위 정의
     */
    @JvmStatic
    public fun register(definition: UnitDefinition) {
        definitions[definition.symbol.lowercase()] = definition
        
        // 별명들도 등록
        definition.aliases.forEach { alias ->
            definitions[alias.lowercase()] = definition
        }
        
        // 카테고리별 인덱스 업데이트
        categoryMap.getOrPut(definition.category) { mutableSetOf() }
            .add(definition.symbol)
    }
    
    /**
     * 심볼로 단위 정의 조회
     * 
     * @param symbol 조회할 단위 심볼
     * @return 단위 정의 또는 null
     */
    @JvmStatic
    public fun getDefinition(symbol: String): UnitDefinition? {
        return definitions[symbol.lowercase()]
    }
    
    /**
     * 카테고리별 모든 단위 조회
     * 
     * @param category 조회할 카테고리
     * @return 해당 카테고리의 모든 단위 심볼
     */
    @JvmStatic
    public fun getUnitsByCategory(category: UnitCategory): Set<String> {
        return categoryMap[category]?.toSet() ?: emptySet()
    }
    
    /**
     * 두 단위 간 변환 비율 계산
     * 
     * @param fromSymbol 변환 시작 단위
     * @param toSymbol 변환 목표 단위
     * @return 변환 비율 또는 null (같은 카테고리가 아닌 경우)
     */
    @JvmStatic
    public fun getConversionRatio(fromSymbol: String, toSymbol: String): Double? {
        val fromDef = getDefinition(fromSymbol) ?: return null
        val toDef = getDefinition(toSymbol) ?: return null
        
        // 같은 카테고리인지 확인
        if (fromDef.category != toDef.category) return null
        
        return fromDef.baseRatio / toDef.baseRatio
    }
    
    /**
     * 단위가 유효한지 확인
     * 
     * @param symbol 확인할 단위 심볼
     * @return 유효 여부
     */
    @JvmStatic
    public fun isValidUnit(symbol: String): Boolean {
        return definitions.containsKey(symbol.lowercase())
    }
    
    /**
     * 카테고리의 기본 단위 조회
     * 
     * @param category 조회할 카테고리
     * @return 기본 단위 심볼 또는 null
     */
    @JvmStatic
    public fun getBaseUnit(category: UnitCategory): String? {
        return definitions.values
            .find { it.category == category && it.isBaseUnit }
            ?.symbol
    }
    
    /**
     * ECU Core의 기본 단위들을 등록
     */
    private fun registerCoreUnits() {
        // 길이 단위 (기본: 미터)
        register(UnitDefinition("m", "meter", UnitCategory.LENGTH, 1.0, true, setOf("meter", "meters")))
        register(UnitDefinition("cm", "centimeter", UnitCategory.LENGTH, 0.01, aliases = setOf("centimeter", "centimeters")))
        register(UnitDefinition("mm", "millimeter", UnitCategory.LENGTH, 0.001, aliases = setOf("millimeter", "millimeters")))
        register(UnitDefinition("km", "kilometer", UnitCategory.LENGTH, 1000.0, aliases = setOf("kilometer", "kilometers")))
        register(UnitDefinition("in", "inch", UnitCategory.LENGTH, 0.0254, aliases = setOf("inch", "inches")))
        register(UnitDefinition("ft", "foot", UnitCategory.LENGTH, 0.3048, aliases = setOf("foot", "feet")))
        register(UnitDefinition("yd", "yard", UnitCategory.LENGTH, 0.9144, aliases = setOf("yard", "yards")))
        register(UnitDefinition("mi", "mile", UnitCategory.LENGTH, 1609.344, aliases = setOf("mile", "miles")))
        
        // 무게 단위 (기본: 킬로그램)
        register(UnitDefinition("kg", "kilogram", UnitCategory.WEIGHT, 1.0, true, setOf("kilogram", "kilograms")))
        register(UnitDefinition("g", "gram", UnitCategory.WEIGHT, 0.001, aliases = setOf("gram", "grams")))
        register(UnitDefinition("mg", "milligram", UnitCategory.WEIGHT, 0.000001, aliases = setOf("milligram", "milligrams")))
        register(UnitDefinition("t", "metric ton", UnitCategory.WEIGHT, 1000.0, aliases = setOf("ton", "tons", "tonne", "tonnes")))
        register(UnitDefinition("lb", "pound", UnitCategory.WEIGHT, 0.453592, aliases = setOf("pound", "pounds", "lbs")))
        register(UnitDefinition("oz", "ounce", UnitCategory.WEIGHT, 0.0283495, aliases = setOf("ounce", "ounces")))
        
        // 부피 단위 (기본: 리터)
        register(UnitDefinition("l", "liter", UnitCategory.VOLUME, 1.0, true, setOf("liter", "liters", "litre", "litres")))
        register(UnitDefinition("ml", "milliliter", UnitCategory.VOLUME, 0.001, aliases = setOf("milliliter", "milliliters", "millilitre", "millilitres")))
        register(UnitDefinition("m³", "cubic meter", UnitCategory.VOLUME, 1000.0, aliases = setOf("m3", "cubic meter", "cubic meters")))
        register(UnitDefinition("gal", "gallon", UnitCategory.VOLUME, 3.78541, aliases = setOf("gallon", "gallons")))
        register(UnitDefinition("qt", "quart", UnitCategory.VOLUME, 0.946353, aliases = setOf("quart", "quarts")))
        register(UnitDefinition("pt", "pint", UnitCategory.VOLUME, 0.473176, aliases = setOf("pint", "pints")))
        register(UnitDefinition("fl oz", "fluid ounce", UnitCategory.VOLUME, 0.0295735, aliases = setOf("fluid ounce", "fluid ounces", "floz")))
        
        // 온도 단위 (기본: 켈빈)
        register(UnitDefinition("K", "Kelvin", UnitCategory.TEMPERATURE, 1.0, true, setOf("kelvin")))
        register(UnitDefinition("°C", "Celsius", UnitCategory.TEMPERATURE, 1.0, aliases = setOf("celsius", "C")))
        register(UnitDefinition("°F", "Fahrenheit", UnitCategory.TEMPERATURE, 1.0, aliases = setOf("fahrenheit", "F")))

        // 면적 단위 (기본: 제곱미터)
        register(UnitDefinition(
            "m²", "square meter", UnitCategory.AREA,
            1.0, true,
            setOf("m2", "square meter", "square meters")
        ))
        register(UnitDefinition(
            "cm²", "square centimeter", UnitCategory.AREA,
            0.0001,
            aliases = setOf("cm2", "square centimeter", "square centimeters")
        ))
        register(UnitDefinition(
            "mm²", "square millimeter", UnitCategory.AREA,
            0.000001,
            aliases = setOf("mm2", "square millimeter", "square millimeters")
        ))
        register(UnitDefinition(
            "km²", "square kilometer", UnitCategory.AREA,
            1_000_000.0,
            aliases = setOf("km2", "square kilometer", "square kilometers")
        ))
        register(UnitDefinition(
            "in²", "square inch", UnitCategory.AREA,
            0.00064516,
            aliases = setOf("in2", "square inch", "square inches")
        ))
        register(UnitDefinition(
            "ft²", "square foot", UnitCategory.AREA,
            0.092903,
            aliases = setOf("ft2", "sq ft", "square foot", "square feet")
        ))
        register(UnitDefinition(
            "yd²", "square yard", UnitCategory.AREA,
            0.836127,
            aliases = setOf("yd2", "square yard", "square yards")
        ))
        register(UnitDefinition(
            "mi²", "square mile", UnitCategory.AREA,
            2_589_988.110336,
            aliases = setOf("mi2", "square mile", "square miles")
        ))
        register(UnitDefinition(
            "ha", "hectare", UnitCategory.AREA,
            10_000.0,
            aliases = setOf("hectare", "hectares")
        ))
        register(UnitDefinition(
            "ac", "acre", UnitCategory.AREA,
            4_046.8564224,
            aliases = setOf("acre", "acres")
        ))
        // 면적 단위  (기본: m²)
        register(UnitDefinition(
            "m²", "square meter", UnitCategory.AREA,
            baseRatio = 1.0, isBaseUnit = true,
            aliases = setOf("sqm", "m2", "square meter", "square meters")
        ))
        register(UnitDefinition(
            "cm²", "square centimeter", UnitCategory.AREA,
            baseRatio = 0.0001,
            aliases = setOf("cm2", "sqcm", "square centimeter", "square centimeters")
        ))
        register(UnitDefinition(
            "ft²", "square foot", UnitCategory.AREA,
            baseRatio = 0.092903,
            aliases = setOf("ft2", "sqft", "square foot", "square feet")
        ))
        register(UnitDefinition(
            "in²", "square inch", UnitCategory.AREA,
            baseRatio = 0.00064516,
            aliases = setOf("in2", "sqin", "square inch", "square inches")
        ))
        register(UnitDefinition(
            "acre", "acre", UnitCategory.AREA,
            baseRatio = 4046.86,
            aliases = setOf("acres")
        ))
        register(UnitDefinition(
            "ha", "hectare", UnitCategory.AREA,
            baseRatio = 10_000.0,
            aliases = setOf("hectare", "hectares")
        ))
        register(UnitDefinition(
            "km²", "square kilometer", UnitCategory.AREA,
            baseRatio = 1_000_000.0,
            aliases = setOf("km2", "sqkm", "square kilometer", "square kilometers")
        ))

    }



    /**
     * 등록된 모든 단위 목록 조회
     * 
     * @return 모든 단위 심볼 집합
     */
    @JvmStatic
    public fun getAllUnits(): Set<String> {
        return definitions.keys.toSet()
    }
    
    /**
     * 레지스트리 초기화 (테스트용)
     */
    @JvmStatic
    internal fun reset() {
        definitions.clear()
        categoryMap.clear()
        registerCoreUnits()
    }
}
