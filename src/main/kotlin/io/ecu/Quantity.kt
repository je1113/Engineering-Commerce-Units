package io.ecu

/**
 * 수량/개수 단위를 나타내는 클래스
 * 
 * 커머스 도메인에서 필수적인 수량 단위 변환을 지원합니다.
 * 예: pieces, dozens, gross, ream
 */
class Quantity private constructor(
    baseValue: Double,
    symbol: String,
    displayName: String,
    precision: Int = -1,
    roundingMode: RoundingMode = RoundingMode.HALF_UP
) : BaseUnit<Quantity>(baseValue, symbol, displayName, UnitCategory.QUANTITY, precision, roundingMode) {
    
    /** 기본 단위(개수)로 변환된 값 */
    val pieces: Double get() = baseValue
    
    /** dozen(12개) 단위로 변환된 값 */
    val dozens: Double get() = baseValue / 12.0
    
    /** gross(144개) 단위로 변환된 값 */
    val gross: Double get() = baseValue / 144.0
    
    /** ream(500개) 단위로 변환된 값 */
    val reams: Double get() = baseValue / 500.0
    
    override fun createInstance(
        baseValue: Double,
        symbol: String,
        displayName: String,
        precision: Int,
        roundingMode: RoundingMode
    ): Quantity {
        return Quantity(baseValue, symbol, displayName, precision, roundingMode)
    }
    
    override fun to(targetSymbol: String): Quantity {
        val targetDef = UnitRegistry.getDefinition(targetSymbol)
            ?: throw IllegalArgumentException("Unknown quantity unit: $targetSymbol")
        
        require(targetDef.category == UnitCategory.QUANTITY) {
            "Target unit $targetSymbol is not a quantity unit"
        }
        
        val newValue = baseValue / targetDef.factor
        return Quantity(baseValue, targetSymbol, targetDef.displayName, precision, roundingMode)
    }
    
    /**
     * 포장 단위로 변환
     * @param packSize 포장 단위당 개수
     * @return 포장 단위로 변환된 수량
     */
    fun toPackages(packSize: Int): Quantity {
        require(packSize > 0) { "Pack size must be positive" }
        val packages = baseValue / packSize
        return Quantity(
            baseValue,
            "pack($packSize)",
            "$packSize-pack",
            precision,
            roundingMode
        )
    }
    
    /**
     * 박스 단위로 변환
     * @param boxSize 박스당 개수
     * @return 박스 단위로 변환된 수량
     */
    fun toBoxes(boxSize: Int): Quantity {
        require(boxSize > 0) { "Box size must be positive" }
        val boxes = baseValue / boxSize
        return Quantity(
            baseValue,
            "box($boxSize)",
            "$boxSize-box",
            precision,
            roundingMode
        )
    }
    
    /**
     * 팔레트 단위로 변환
     * @param palletSize 팔레트당 개수
     * @return 팔레트 단위로 변환된 수량
     */
    fun toPallets(palletSize: Int): Quantity {
        require(palletSize > 0) { "Pallet size must be positive" }
        val pallets = baseValue / palletSize
        return Quantity(
            baseValue,
            "pallet($palletSize)",
            "$palletSize-pallet",
            precision,
            roundingMode
        )
    }
    
    /**
     * 기본 단위(pieces)로 변환
     */
    fun toPieces(): Quantity {
        return Quantity(
            baseValue,
            "pcs",
            "pieces",
            precision,
            roundingMode
        )
    }
    
    /**
     * 최적 단위 제안
     */
    fun suggestOptimalUnit(): String {
        val conversionService = ConversionService()
        val optimal = conversionService.suggestOptimalUnit(this)
        return optimal.recommended?.format() ?: this.toString()
    }
    
    companion object {
        init {
            // 기본 수량 단위 등록
            UnitRegistry.register(
                UnitDefinition("pcs", "pieces", UnitCategory.QUANTITY, 1.0)
            )
            UnitRegistry.register(
                UnitDefinition("piece", "piece", UnitCategory.QUANTITY, 1.0)
            )
            UnitRegistry.register(
                UnitDefinition("ea", "each", UnitCategory.QUANTITY, 1.0)
            )
            UnitRegistry.register(
                UnitDefinition("dz", "dozen", UnitCategory.QUANTITY, 12.0)
            )
            UnitRegistry.register(
                UnitDefinition("dozen", "dozen", UnitCategory.QUANTITY, 12.0)
            )
            UnitRegistry.register(
                UnitDefinition("gr", "gross", UnitCategory.QUANTITY, 144.0)
            )
            UnitRegistry.register(
                UnitDefinition("gross", "gross", UnitCategory.QUANTITY, 144.0)
            )
            UnitRegistry.register(
                UnitDefinition("ream", "ream", UnitCategory.QUANTITY, 500.0)
            )
            UnitRegistry.register(
                UnitDefinition("score", "score", UnitCategory.QUANTITY, 20.0)
            )
        }
        
        /**
         * 문자열로부터 수량 파싱
         */
        fun parse(input: String): Quantity {
            val (value, unit) = parseInput(input)
            return of(value, unit)
        }
        
        /**
         * 지정된 값과 단위로 수량 생성
         */
        fun of(value: Double, unit: String): Quantity {
            val def = UnitRegistry.getDefinition(unit)
                ?: throw IllegalArgumentException("Unknown quantity unit: $unit")
            
            require(def.category == UnitCategory.QUANTITY) {
                "Unit $unit is not a quantity unit"
            }
            
            val baseValue = value * def.factor
            return Quantity(baseValue, unit, def.displayName)
        }
        
        /**
         * Factory 메서드들
         */
        fun pieces(value: Double): Quantity = of(value, "pcs")
        fun dozens(value: Double): Quantity = of(value, "dozen")
        fun gross(value: Double): Quantity = of(value, "gross")
        fun reams(value: Double): Quantity = of(value, "ream")
        fun scores(value: Double): Quantity = of(value, "score")
        
        private fun parseInput(input: String): Pair<Double, String> {
            val trimmed = input.trim()
            val match = Regex("""^([-+]?\d*\.?\d+)\s*(.+)$""").find(trimmed)
                ?: throw IllegalArgumentException("Invalid quantity format: $input")
            
            val value = match.groupValues[1].toDouble()
            val unit = match.groupValues[2].trim()
            
            return value to unit
        }
    }
    
    /**
     * 산술 연산
     */
    operator fun plus(other: Quantity): Quantity {
        return Quantity(baseValue + other.baseValue, symbol, displayName, precision, roundingMode)
    }
    
    operator fun minus(other: Quantity): Quantity {
        return Quantity(baseValue - other.baseValue, symbol, displayName, precision, roundingMode)
    }
    
    operator fun times(factor: Double): Quantity {
        return Quantity(baseValue * factor, symbol, displayName, precision, roundingMode)
    }
    
    operator fun div(factor: Double): Quantity {
        require(factor != 0.0) { "Division by zero" }
        return Quantity(baseValue / factor, symbol, displayName, precision, roundingMode)
    }
}
