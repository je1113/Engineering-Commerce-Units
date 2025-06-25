package io.ecu

/**
 * 단위 정의를 저장하는 데이터 클래스
 */
data class UnitDefinition(
    val symbol: String,
    val displayName: String,
    val category: UnitCategory,
    val baseRatio: Double,
    val isBaseUnit: Boolean = false,
    val aliases: Set<String> = emptySet()
)

/**
 * 단위 정의를 관리하는 레지스트리
 */
object UnitRegistry {
    
    private val definitions = mutableMapOf<String, UnitDefinition>()
    private val categoryMap = mutableMapOf<UnitCategory, MutableSet<String>>()
    
    init {
        registerStandardUnits()
    }
    
    /**
     * 새로운 단위 정의를 등록
     */
    fun register(definition: UnitDefinition) {
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
     */
    fun getDefinition(symbol: String): UnitDefinition? {
        return definitions[symbol.lowercase()]
    }
    
    /**
     * 카테고리별 모든 단위 조회
     */
    fun getUnitsByCategory(category: UnitCategory): Set<String> {
        return categoryMap[category] ?: emptySet()
    }
    
    /**
     * 두 단위 간 변환 비율 계산
     */
    fun getConversionRatio(fromSymbol: String, toSymbol: String): Double? {
        val fromDef = getDefinition(fromSymbol) ?: return null
        val toDef = getDefinition(toSymbol) ?: return null
        
        // 같은 카테고리인지 확인
        if (fromDef.category != toDef.category) return null
        
        return fromDef.baseRatio / toDef.baseRatio
    }
    
    /**
     * 단위가 유효한지 확인
     */
    fun isValidUnit(symbol: String): Boolean {
        return definitions.containsKey(symbol.lowercase())
    }
    
    /**
     * 카테고리의 기본 단위 조회
     */
    fun getBaseUnit(category: UnitCategory): String? {
        return definitions.values
            .find { it.category == category && it.isBaseUnit }
            ?.symbol
    }
    
    /**
     * 표준 단위들을 등록
     */
    private fun registerStandardUnits() {
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
        register(UnitDefinition("st", "stone", UnitCategory.WEIGHT, 6.35029, aliases = setOf("stone", "stones")))
        
        // 부피 단위 (기본: 리터)
        register(UnitDefinition("l", "liter", UnitCategory.VOLUME, 1.0, true, setOf("liter", "liters", "litre", "litres")))
        register(UnitDefinition("ml", "milliliter", UnitCategory.VOLUME, 0.001, aliases = setOf("milliliter", "milliliters", "millilitre", "millilitres")))
        register(UnitDefinition("m³", "cubic meter", UnitCategory.VOLUME, 1000.0, aliases = setOf("m3", "cubic meter", "cubic meters")))
        register(UnitDefinition("cm³", "cubic centimeter", UnitCategory.VOLUME, 0.001, aliases = setOf("cm3", "cubic centimeter", "cubic centimeters", "cc")))
        register(UnitDefinition("gal", "gallon", UnitCategory.VOLUME, 3.78541, aliases = setOf("gallon", "gallons")))
        register(UnitDefinition("qt", "quart", UnitCategory.VOLUME, 0.946353, aliases = setOf("quart", "quarts")))
        register(UnitDefinition("pt", "pint", UnitCategory.VOLUME, 0.473176, aliases = setOf("pint", "pints")))
        register(UnitDefinition("fl oz", "fluid ounce", UnitCategory.VOLUME, 0.0295735, aliases = setOf("fluid ounce", "fluid ounces", "floz")))
        
        // 온도 단위 (기본: 켈빈)
        register(UnitDefinition("K", "Kelvin", UnitCategory.TEMPERATURE, 1.0, true, setOf("kelvin")))
        register(UnitDefinition("°C", "Celsius", UnitCategory.TEMPERATURE, 1.0, aliases = setOf("celsius", "C")))
        register(UnitDefinition("°F", "Fahrenheit", UnitCategory.TEMPERATURE, 1.0, aliases = setOf("fahrenheit", "F")))
        
        // 면적 단위 (기본: 제곱미터)
        register(UnitDefinition("m²", "square meter", UnitCategory.AREA, 1.0, true, setOf("m2", "square meter", "square meters")))
        register(UnitDefinition("cm²", "square centimeter", UnitCategory.AREA, 0.0001, aliases = setOf("cm2", "square centimeter", "square centimeters")))
        register(UnitDefinition("km²", "square kilometer", UnitCategory.AREA, 1000000.0, aliases = setOf("km2", "square kilometer", "square kilometers")))
        register(UnitDefinition("in²", "square inch", UnitCategory.AREA, 0.00064516, aliases = setOf("in2", "square inch", "square inches")))
        register(UnitDefinition("ft²", "square foot", UnitCategory.AREA, 0.092903, aliases = setOf("ft2", "square foot", "square feet")))
        register(UnitDefinition("yd²", "square yard", UnitCategory.AREA, 0.836127, aliases = setOf("yd2", "square yard", "square yards")))
        register(UnitDefinition("acre", "acre", UnitCategory.AREA, 4046.86, aliases = setOf("acres")))
        register(UnitDefinition("ha", "hectare", UnitCategory.AREA, 10000.0, aliases = setOf("hectare", "hectares")))
        
        // 압력 단위 (기본: Pascal)
        register(UnitDefinition("Pa", "Pascal", UnitCategory.PRESSURE, 1.0, true, setOf("pascal")))
        register(UnitDefinition("kPa", "kilopascal", UnitCategory.PRESSURE, 1000.0, aliases = setOf("kilopascal")))
        register(UnitDefinition("bar", "bar", UnitCategory.PRESSURE, 100000.0, aliases = setOf("bars")))
        register(UnitDefinition("psi", "pounds per square inch", UnitCategory.PRESSURE, 6894.76, aliases = setOf("lb/in²")))
        register(UnitDefinition("atm", "atmosphere", UnitCategory.PRESSURE, 101325.0, aliases = setOf("atmospheres")))
        register(UnitDefinition("mmHg", "millimeter of mercury", UnitCategory.PRESSURE, 133.322, aliases = setOf("mm Hg", "torr")))
        
        // 속도 단위 (기본: m/s)
        register(UnitDefinition("m/s", "meters per second", UnitCategory.SPEED, 1.0, true, setOf("mps", "meter per second", "meters per second")))
        register(UnitDefinition("km/h", "kilometers per hour", UnitCategory.SPEED, 1.0/3.6, aliases = setOf("kmh", "kph", "kilometer per hour", "kilometers per hour")))
        register(UnitDefinition("mph", "miles per hour", UnitCategory.SPEED, 0.44704, aliases = setOf("mi/h", "mile per hour", "miles per hour")))
        register(UnitDefinition("kn", "knot", UnitCategory.SPEED, 0.514444, aliases = setOf("knot", "knots", "kt", "nautical mile per hour")))
        register(UnitDefinition("ft/s", "feet per second", UnitCategory.SPEED, 0.3048, aliases = setOf("fps", "foot per second", "feet per second")))
        register(UnitDefinition("cm/s", "centimeters per second", UnitCategory.SPEED, 0.01, aliases = setOf("centimeter per second", "centimeters per second")))
        register(UnitDefinition("Ma", "Mach", UnitCategory.SPEED, 343.0, aliases = setOf("mach", "Mach number")))
    }
    
    /**
     * 등록된 모든 단위 목록 조회
     */
    fun getAllUnits(): Set<String> {
        return definitions.keys.toSet()
    }
    
    /**
     * 단위 정의 제거
     */
    fun unregister(symbol: String) {
        val definition = definitions[symbol]
        if (definition != null) {
            definitions.remove(symbol)
            definition.aliases.forEach { alias ->
                definitions.remove(alias)
            }
            categoryMap[definition.category]?.remove(symbol)
        }
    }
    
    /**
     * 레지스트리 초기화
     */
    fun clear() {
        definitions.clear()
        categoryMap.clear()
        registerStandardUnits()
    }
}
