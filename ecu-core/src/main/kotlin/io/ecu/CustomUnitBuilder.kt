package io.ecu

/**
 * 커스텀 단위를 생성하기 위한 빌더 클래스
 * 
 * DSL 스타일로 단위를 정의할 수 있도록 지원합니다.
 * 
 * @since 1.1.0
 */
public class CustomUnitBuilder {
    private var symbol: String? = null
    private var displayName: String? = null
    private var category: UnitCategory? = null
    private var baseRatio: Double = 1.0
    private var isBaseUnit: Boolean = false
    private val aliases = mutableSetOf<String>()
    private val customConversions = mutableMapOf<String, (Double) -> Double?>()
    
    /**
     * 단위 기호 설정
     */
    public fun symbol(value: String): CustomUnitBuilder = apply {
        this.symbol = value
    }
    
    /**
     * 표시 이름 설정
     */
    public fun displayName(value: String): CustomUnitBuilder = apply {
        this.displayName = value
    }
    
    /**
     * 단위 카테고리 설정
     */
    public fun category(value: UnitCategory): CustomUnitBuilder = apply {
        this.category = value
    }
    
    /**
     * 기본 단위로의 변환 비율 설정
     */
    public fun baseRatio(value: Double): CustomUnitBuilder = apply {
        require(value > 0) { "Base ratio must be positive" }
        this.baseRatio = value
    }
    
    /**
     * 기본 단위 여부 설정
     */
    public fun isBaseUnit(value: Boolean): CustomUnitBuilder = apply {
        this.isBaseUnit = value
    }
    
    /**
     * 별칭 추가
     */
    public fun alias(vararg values: String): CustomUnitBuilder = apply {
        aliases.addAll(values)
    }
    
    /**
     * 커스텀 변환 로직 추가
     * 
     * @param targetUnit 변환 대상 단위
     * @param converter 변환 함수 (null을 반환하면 기본 변환 사용)
     */
    public fun customConversion(
        targetUnit: String, 
        converter: (Double) -> Double?
    ): CustomUnitBuilder = apply {
        customConversions[targetUnit] = converter
    }
    
    /**
     * UnitDefinition 생성
     */
    public fun build(): UnitDefinition {
        requireNotNull(symbol) { "Symbol must be specified" }
        requireNotNull(displayName) { "Display name must be specified" }
        requireNotNull(category) { "Category must be specified" }
        
        val definition = UnitDefinition(
            symbol = symbol!!,
            displayName = displayName!!,
            category = category!!,
            baseRatio = baseRatio,
            isBaseUnit = isBaseUnit,
            aliases = aliases.toSet()
        )
        
        // 커스텀 변환 로직이 있으면 별도로 저장
        if (customConversions.isNotEmpty()) {
            CustomConversionRegistry.registerConversions(symbol!!, customConversions)
        }
        
        return definition
    }
}

/**
 * 커스텀 변환 로직을 관리하는 레지스트리
 */
public object CustomConversionRegistry {
    private val conversions = mutableMapOf<String, Map<String, (Double) -> Double?>>()
    
    /**
     * 커스텀 변환 로직 등록
     */
    internal fun registerConversions(
        fromUnit: String, 
        conversions: Map<String, (Double) -> Double?>
    ) {
        this.conversions[fromUnit.lowercase()] = conversions
    }
    
    /**
     * 커스텀 변환 함수 조회
     */
    @JvmStatic
    public fun getCustomConverter(
        fromUnit: String, 
        toUnit: String
    ): ((Double) -> Double?)? {
        return conversions[fromUnit.lowercase()]?.get(toUnit.lowercase())
    }
    
    /**
     * 커스텀 변환 수행
     */
    @JvmStatic
    public fun convertCustom(
        value: Double,
        fromUnit: String,
        toUnit: String
    ): Double? {
        val converter = getCustomConverter(fromUnit, toUnit)
        return converter?.invoke(value)
    }
    
    /**
     * 레지스트리 초기화 (테스트용)
     */
    internal fun reset() {
        conversions.clear()
    }
}

/**
 * DSL 스타일의 커스텀 단위 생성 함수
 */
public fun customUnit(block: CustomUnitBuilder.() -> kotlin.Unit): UnitDefinition {
    return CustomUnitBuilder().apply(block).build()
}
