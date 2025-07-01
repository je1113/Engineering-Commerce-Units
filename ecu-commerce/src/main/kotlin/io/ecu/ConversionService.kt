package io.ecu

/**
 * 고급 수량 환산 서비스
 * 체인 환산, 최적 단위 제안 등의 기능을 제공합니다.
 */
class ConversionService {
    
    /**
     * 단위 체인 정의
     * 예: piece -> box -> pallet
     */
    data class UnitChain(
        val units: List<ChainUnit>,
        val name: String
    ) {
        data class ChainUnit(
            val symbol: String,
            val displayName: String,
            val conversionFactor: Double, // 이전 단위 대비 환산 비율
            val abbreviation: String? = null
        )
        
        /**
         * 체인의 기본 단위(첫 번째 단위)로 환산
         */
        fun toBaseUnit(value: Double, fromUnit: String): Double {
            var result = value
            var foundUnit = false
            
            for (i in units.indices.reversed()) {
                if (units[i].symbol == fromUnit || units[i].abbreviation == fromUnit) {
                    foundUnit = true
                    // 기본 단위까지 역산
                    for (j in i downTo 1) {
                        result *= units[j].conversionFactor
                    }
                    break
                }
            }
            
            if (!foundUnit) {
                throw IllegalArgumentException("Unit $fromUnit not found in chain ${name}")
            }
            
            return result
        }
        
        /**
         * 특정 단위로 환산
         */
        fun convert(value: Double, fromUnit: String, toUnit: String): Double {
            val baseValue = toBaseUnit(value, fromUnit)
            
            // 목표 단위로 환산
            var result = baseValue
            for (i in 1 until units.size) {
                if (units[i].symbol == toUnit || units[i].abbreviation == toUnit) {
                    for (j in 1..i) {
                        result /= units[j].conversionFactor
                    }
                    return result
                }
            }
            
            if (units[0].symbol == toUnit || units[0].abbreviation == toUnit) {
                return baseValue
            }
            
            throw IllegalArgumentException("Unit $toUnit not found in chain ${name}")
        }
    }
    
    /**
     * 미리 정의된 체인들
     */
    companion object {
        val STANDARD_PACKAGING = UnitChain(
            name = "Standard Packaging",
            units = listOf(
                UnitChain.ChainUnit("piece", "piece", 1.0, "pcs"),
                UnitChain.ChainUnit("box", "box", 12.0, "bx"),
                UnitChain.ChainUnit("carton", "carton", 4.0, "ctn"),
                UnitChain.ChainUnit("pallet", "pallet", 10.0, "plt")
            )
        )
        
        val BEVERAGE_PACKAGING = UnitChain(
            name = "Beverage Packaging",
            units = listOf(
                UnitChain.ChainUnit("can", "can", 1.0),
                UnitChain.ChainUnit("6-pack", "6-pack", 6.0, "6pk"),
                UnitChain.ChainUnit("case", "case", 4.0, "cs"),
                UnitChain.ChainUnit("pallet", "pallet", 50.0, "plt")
            )
        )
        
        val PAPER_UNITS = UnitChain(
            name = "Paper Units",
            units = listOf(
                UnitChain.ChainUnit("sheet", "sheet", 1.0),
                UnitChain.ChainUnit("quire", "quire", 25.0),
                UnitChain.ChainUnit("ream", "ream", 20.0),
                UnitChain.ChainUnit("case", "case", 10.0, "cs")
            )
        )
        
        val EGG_PACKAGING = UnitChain(
            name = "Egg Packaging",
            units = listOf(
                UnitChain.ChainUnit("egg", "egg", 1.0),
                UnitChain.ChainUnit("half-dozen", "half-dozen", 6.0, "6"),
                UnitChain.ChainUnit("dozen", "dozen", 2.0, "dz"),
                UnitChain.ChainUnit("flat", "flat", 2.5, "flt"),
                UnitChain.ChainUnit("case", "case", 12.0, "cs")
            )
        )
    }
    
    private val chains = mutableMapOf<String, UnitChain>()
    
    init {
        // 기본 체인 등록
        registerChain(STANDARD_PACKAGING)
        registerChain(BEVERAGE_PACKAGING)
        registerChain(PAPER_UNITS)
        registerChain(EGG_PACKAGING)
    }
    
    /**
     * 체인 등록
     */
    fun registerChain(chain: UnitChain) {
        chains[chain.name] = chain
    }
    
    /**
     * 체인 환산 수행
     */
    fun convertInChain(
        quantity: Quantity,
        targetUnit: String,
        chainName: String
    ): Quantity {
        val chain = chains[chainName]
            ?: throw IllegalArgumentException("Chain $chainName not found")
        
        // 현재 단위 찾기
        val currentUnit = findUnitInChains(quantity.symbol)
            ?: throw IllegalArgumentException("Unit ${quantity.symbol} not found in any chain")
        
        val convertedValue = chain.convert(quantity.baseValue, currentUnit, targetUnit)
        return Quantity.of(convertedValue, targetUnit)
    }
    
    /**
     * 모든 체인에서 단위 찾기
     */
    private fun findUnitInChains(unit: String): String? {
        chains.values.forEach { chain ->
            chain.units.forEach { chainUnit ->
                if (chainUnit.symbol == unit || chainUnit.abbreviation == unit) {
                    return chainUnit.symbol
                }
            }
        }
        return null
    }
    
    /**
     * 최적 단위 제안
     */
    fun suggestOptimalUnit(quantity: Quantity): OptimalUnitSuggestion {
        val pieces = quantity.pieces
        
        // 각 체인에 대해 최적 표현 찾기
        val suggestions = mutableListOf<OptimalUnitSuggestion.Suggestion>()
        
        chains.values.forEach { chain ->
            val suggestion = findOptimalRepresentation(pieces, chain)
            if (suggestion != null) {
                suggestions.add(suggestion)
            }
        }
        
        // 가장 간단한 표현 선택 (구성 요소가 적은 것)
        val optimal = suggestions.minByOrNull { it.components.size }
        
        return OptimalUnitSuggestion(
            originalQuantity = quantity,
            suggestions = suggestions,
            recommended = optimal
        )
    }
    
    /**
     * 특정 체인에서 최적 표현 찾기
     */
    private fun findOptimalRepresentation(
        pieces: Double,
        chain: UnitChain
    ): OptimalUnitSuggestion.Suggestion? {
        val components = mutableListOf<OptimalUnitSuggestion.Component>()
        var remaining = pieces
        
        // 큰 단위부터 시도
        for (i in chain.units.indices.reversed()) {
            if (i == 0) continue // 기본 단위는 나중에 처리
            
            val unit = chain.units[i]
            val unitValue = calculateUnitValue(i, chain)
            
            if (remaining >= unitValue) {
                val count = (remaining / unitValue).toInt()
                if (count > 0) {
                    components.add(
                        OptimalUnitSuggestion.Component(
                            value = count.toDouble(),
                            unit = unit.symbol,
                            displayName = unit.displayName
                        )
                    )
                    remaining -= count * unitValue
                }
            }
        }
        
        // 남은 기본 단위 처리
        if (remaining > 0.001) { // 부동소수점 오차 고려
            components.add(
                OptimalUnitSuggestion.Component(
                    value = remaining,
                    unit = chain.units[0].symbol,
                    displayName = chain.units[0].displayName
                )
            )
        }
        
        return if (components.isNotEmpty()) {
            OptimalUnitSuggestion.Suggestion(
                chainName = chain.name,
                components = components,
                totalInBaseUnit = pieces
            )
        } else {
            null
        }
    }
    
    /**
     * 체인에서 단위의 기본 단위 대비 값 계산
     */
    private fun calculateUnitValue(index: Int, chain: UnitChain): Double {
        var value = 1.0
        for (i in 1..index) {
            value *= chain.units[i].conversionFactor
        }
        return value
    }
    
    /**
     * 분수 표현 지원
     */
    fun toFractionalRepresentation(
        quantity: Quantity,
        targetUnit: String,
        chainName: String? = null
    ): FractionalQuantity {
        val chain = if (chainName != null) {
            chains[chainName] ?: throw IllegalArgumentException("Chain $chainName not found")
        } else {
            // 자동으로 적절한 체인 찾기
            findChainForUnit(targetUnit)
                ?: throw IllegalArgumentException("No chain found for unit $targetUnit")
        }
        
        val converted = if (chainName != null) {
            convertInChain(quantity, targetUnit, chainName)
        } else {
            quantity.to(targetUnit)
        }
        
        val value = converted.baseValue / (UnitRegistry.getDefinition(targetUnit)?.baseRatio ?: 1.0)
        val whole = value.toInt()
        val fractional = value - whole
        
        return FractionalQuantity(
            whole = whole,
            fractional = fractional,
            unit = targetUnit,
            displayValue = formatFractional(whole, fractional, targetUnit)
        )
    }
    
    /**
     * 단위에 해당하는 체인 찾기
     */
    private fun findChainForUnit(unit: String): UnitChain? {
        chains.values.forEach { chain ->
            if (chain.units.any { it.symbol == unit || it.abbreviation == unit }) {
                return chain
            }
        }
        return null
    }
    
    /**
     * 분수 형식으로 포맷
     */
    private fun formatFractional(whole: Int, fractional: Double, unit: String): String {
        return when {
            fractional < 0.001 -> "$whole $unit"
            fractional < 0.125 -> "$whole $unit"
            fractional < 0.375 -> "$whole¼ $unit"
            fractional < 0.625 -> "$whole½ $unit"
            fractional < 0.875 -> "$whole¾ $unit"
            else -> "${whole + 1} $unit"
        }
    }
}

/**
 * 최적 단위 제안 결과
 */
data class OptimalUnitSuggestion(
    val originalQuantity: Quantity,
    val suggestions: List<Suggestion>,
    val recommended: Suggestion?
) {
    data class Suggestion(
        val chainName: String,
        val components: List<Component>,
        val totalInBaseUnit: Double
    ) {
        fun format(): String {
            return components.joinToString(" + ") { component ->
                val formattedValue = if (component.value % 1.0 == 0.0) {
                    component.value.toInt().toString()
                } else {
                    component.value.toString()
                }
                "$formattedValue ${component.displayName}"
            }
        }
    }
    
    data class Component(
        val value: Double,
        val unit: String,
        val displayName: String
    )
}

/**
 * 분수 표현 수량
 */
data class FractionalQuantity(
    val whole: Int,
    val fractional: Double,
    val unit: String,
    val displayValue: String
)
