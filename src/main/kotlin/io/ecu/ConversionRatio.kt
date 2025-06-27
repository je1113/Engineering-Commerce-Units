package io.ecu

/**
 * 단위 간 환산 비율을 나타내는 클래스
 * 
 * @property fromValue 원본 단위의 값
 * @property toValue 대상 단위의 값
 */
data class ConversionRatio(
    val fromValue: Double,
    val toValue: Double
) {
    init {
        require(fromValue > 0) { "From value must be positive" }
        require(toValue > 0) { "To value must be positive" }
    }
    
    /**
     * 환산 계수 (toValue / fromValue)
     */
    val factor: Double get() = toValue / fromValue
    
    /**
     * 역환산 계수 (fromValue / toValue)
     */
    val inverseFactor: Double get() = fromValue / toValue
    
    /**
     * 주어진 값을 환산
     */
    fun convert(value: Double): Double = value * factor
    
    /**
     * 역방향으로 환산
     */
    fun convertInverse(value: Double): Double = value * inverseFactor
    
    /**
     * 역방향 환산 비율 생성
     */
    fun inverse(): ConversionRatio = ConversionRatio(toValue, fromValue)
    
    /**
     * 환산 비율 합성 (체인 환산)
     */
    fun chain(other: ConversionRatio): ConversionRatio {
        // this: A -> B, other: B -> C => result: A -> C
        return ConversionRatio(fromValue, toValue * other.factor)
    }
    
    companion object {
        /**
         * 단순 비율로 생성
         */
        fun of(ratio: Double): ConversionRatio = ConversionRatio(1.0, ratio)
        
        /**
         * 백분율로 생성
         */
        fun fromPercentage(percentage: Double): ConversionRatio = 
            ConversionRatio(100.0, percentage)
    }
}

/**
 * 제품별 단위 환산 설정
 * 
 * @property productId 제품 식별자
 * @property baseUnit 기본 단위
 * @property conversions 단위별 환산 비율
 * @property roundingProfiles 단위별 라운딩 프로파일
 */
data class ProductUnitConfiguration(
    val productId: String,
    val baseUnit: String,
    val conversions: Map<String, ConversionRatio>,
    val roundingProfiles: Map<String, RoundingProfile> = emptyMap()
) {
    init {
        require(productId.isNotBlank()) { "Product ID must not be blank" }
        require(baseUnit.isNotBlank()) { "Base unit must not be blank" }
    }
    
    /**
     * 특정 단위로의 환산 비율 조회
     */
    fun getConversionRatio(unit: String): ConversionRatio? = conversions[unit]
    
    /**
     * 특정 단위의 라운딩 프로파일 조회
     */
    fun getRoundingProfile(unit: String): RoundingProfile? = roundingProfiles[unit]
    
    /**
     * 기본 단위에서 대상 단위로 환산
     */
    fun convertFromBase(value: Double, targetUnit: String): Double {
        if (targetUnit == baseUnit) return value
        
        val ratio = conversions[targetUnit]
            ?: throw IllegalArgumentException("No conversion defined from $baseUnit to $targetUnit")
        
        return ratio.convert(value)
    }
    
    /**
     * 대상 단위에서 기본 단위로 환산
     */
    fun convertToBase(value: Double, sourceUnit: String): Double {
        if (sourceUnit == baseUnit) return value
        
        val ratio = conversions[sourceUnit]
            ?: throw IllegalArgumentException("No conversion defined from $sourceUnit to $baseUnit")
        
        return ratio.convertInverse(value)
    }
    
    /**
     * 두 단위 간 직접 환산
     */
    fun convert(value: Double, fromUnit: String, toUnit: String): Double {
        if (fromUnit == toUnit) return value
        
        val baseValue = convertToBase(value, fromUnit)
        return convertFromBase(baseValue, toUnit)
    }
    
    companion object {
        /**
         * 빌더 패턴으로 설정 생성
         */
        fun builder(productId: String, baseUnit: String): Builder = 
            Builder(productId, baseUnit)
    }
    
    class Builder(
        private val productId: String,
        private val baseUnit: String
    ) {
        private val conversions = mutableMapOf<String, ConversionRatio>()
        private val roundingProfiles = mutableMapOf<String, RoundingProfile>()
        
        /**
         * 환산 비율 추가
         */
        fun addConversion(unit: String, ratio: ConversionRatio): Builder {
            conversions[unit] = ratio
            return this
        }
        
        /**
         * 간단한 환산 비율 추가
         */
        fun addConversion(unit: String, fromValue: Double, toValue: Double): Builder {
            return addConversion(unit, ConversionRatio(fromValue, toValue))
        }
        
        /**
         * 라운딩 프로파일 추가
         */
        fun addRoundingProfile(unit: String, profile: RoundingProfile): Builder {
            roundingProfiles[unit] = profile
            return this
        }
        
        /**
         * 설정 빌드
         */
        fun build(): ProductUnitConfiguration {
            return ProductUnitConfiguration(
                productId = productId,
                baseUnit = baseUnit,
                conversions = conversions.toMap(),
                roundingProfiles = roundingProfiles.toMap()
            )
        }
    }
}

/**
 * 라운딩 프로파일
 * 
 * @property minimumOrderQuantity 최소 주문 수량
 * @property packagingUnit 포장 단위
 * @property roundingMode 라운딩 모드
 * @property allowFractional 분수 허용 여부
 */
data class RoundingProfile(
    val minimumOrderQuantity: Double = 1.0,
    val packagingUnit: Double = 1.0,
    val roundingMode: RoundingMode = RoundingMode.UP,
    val allowFractional: Boolean = false
) {
    init {
        require(minimumOrderQuantity > 0) { "Minimum order quantity must be positive" }
        require(packagingUnit > 0) { "Packaging unit must be positive" }
    }
    
    /**
     * 주어진 수량에 라운딩 규칙 적용
     */
    fun applyRounding(quantity: Double): Double {
        // 최소 주문 수량 확인
        if (quantity < minimumOrderQuantity) {
            return minimumOrderQuantity
        }
        
        // 분수 허용하지 않으면 정수로 반올림
        val adjustedQuantity = if (!allowFractional) {
            when (roundingMode) {
                RoundingMode.UP -> kotlin.math.ceil(quantity)
                RoundingMode.DOWN -> kotlin.math.floor(quantity)
                RoundingMode.HALF_UP -> kotlin.math.round(quantity)
                RoundingMode.HALF_DOWN -> {
                    val truncated = kotlin.math.truncate(quantity)
                    val fraction = quantity - truncated
                    if (fraction > 0.5) truncated + 1 else truncated
                }
                RoundingMode.HALF_EVEN -> {
                    val rounded = kotlin.math.round(quantity)
                    if (kotlin.math.abs(quantity - rounded) == 0.5) {
                        if (rounded.toLong() % 2 == 0L) rounded
                        else kotlin.math.floor(quantity)
                    } else rounded
                }
            }
        } else {
            quantity
        }
        
        // 포장 단위로 조정
        if (packagingUnit != 1.0) {
            val units = adjustedQuantity / packagingUnit
            val roundedUnits = when (roundingMode) {
                RoundingMode.UP -> kotlin.math.ceil(units)
                RoundingMode.DOWN -> kotlin.math.floor(units)
                else -> kotlin.math.round(units)
            }
            return roundedUnits * packagingUnit
        }
        
        return adjustedQuantity
    }
    
    companion object {
        /**
         * 일반적인 프로파일들
         */
        val DEFAULT = RoundingProfile()
        
        val RETAIL = RoundingProfile(
            minimumOrderQuantity = 1.0,
            packagingUnit = 1.0,
            roundingMode = RoundingMode.UP,
            allowFractional = false
        )
        
        val WHOLESALE = RoundingProfile(
            minimumOrderQuantity = 12.0,
            packagingUnit = 12.0,
            roundingMode = RoundingMode.UP,
            allowFractional = false
        )
        
        val BULK = RoundingProfile(
            minimumOrderQuantity = 100.0,
            packagingUnit = 100.0,
            roundingMode = RoundingMode.UP,
            allowFractional = false
        )
    }
}
