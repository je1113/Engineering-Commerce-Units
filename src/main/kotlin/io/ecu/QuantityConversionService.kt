package io.ecu

/**
 * 수량 변환 서비스
 * 
 * 복잡한 수량 변환 및 포장 계층 관리를 담당합니다.
 */
class QuantityConversionService {
    
    internal val productConfigurations = mutableMapOf<String, ProductUnitConfiguration>()
    internal val packagingHierarchies = mutableMapOf<String, PackagingHierarchy>()
    
    /**
     * 제품 설정 등록
     */
    fun registerProduct(config: ProductUnitConfiguration) {
        productConfigurations[config.productId] = config
    }
    
    /**
     * 포장 계층 등록
     */
    fun registerPackagingHierarchy(productId: String, hierarchy: PackagingHierarchy) {
        packagingHierarchies[productId] = hierarchy
    }
    
    /**
     * 제품별 수량 변환
     */
    fun convert(
        productId: String,
        quantity: Quantity,
        targetUnit: String
    ): Quantity {
        val config = productConfigurations[productId]
            ?: throw IllegalArgumentException("Product configuration not found for: $productId")
        
        // 기본 단위로 변환
        val baseValue = config.convertToBase(quantity.pieces, quantity.symbol)
        
        // 대상 단위로 변환
        val targetValue = config.convertFromBase(baseValue, targetUnit)
        
        // 라운딩 프로파일 적용
        val roundedValue = config.getRoundingProfile(targetUnit)?.let { profile ->
            profile.applyRounding(targetValue)
        } ?: targetValue
        
        return Quantity.of(roundedValue, targetUnit)
    }
    
    /**
     * 최적 포장 단위 제안
     */
    fun suggestOptimalPackaging(
        productId: String,
        quantity: Quantity
    ): PackagingSuggestion {
        val hierarchy = packagingHierarchies[productId]
            ?: return PackagingSuggestion(
                original = quantity,
                suggestions = emptyList(),
                reason = "No packaging hierarchy defined for product"
            )
        
        val pieces = quantity.pieces
        val suggestions = mutableListOf<PackagingOption>()
        
        // 각 포장 레벨에 대해 계산
        hierarchy.levels.forEach { level ->
            val units = pieces / level.unitsPerPackage
            if (units >= level.minimumUnits) {
                suggestions.add(
                    PackagingOption(
                        level = level,
                        quantity = units,
                        remainder = pieces % level.unitsPerPackage
                    )
                )
            }
        }
        
        // 최적 조합 찾기
        val optimal = findOptimalCombination(pieces, hierarchy)
        
        return PackagingSuggestion(
            original = quantity,
            suggestions = suggestions,
            optimal = optimal,
            reason = generateSuggestionReason(suggestions, optimal)
        )
    }
    
    /**
     * 재고 가용성 확인
     */
    fun checkAvailability(
        productId: String,
        requested: Quantity,
        available: Quantity
    ): AvailabilityResult {
        val config = productConfigurations[productId]
            ?: throw IllegalArgumentException("Product configuration not found for: $productId")
        
        // 모두 기본 단위로 변환하여 비교
        val requestedBase = config.convertToBase(requested.pieces, requested.symbol)
        val availableBase = config.convertToBase(available.pieces, available.symbol)
        
        val canFulfill = availableBase >= requestedBase
        val shortage = if (!canFulfill) requestedBase - availableBase else 0.0
        
        return AvailabilityResult(
            canFulfill = canFulfill,
            requested = requested,
            available = available,
            shortage = if (shortage > 0) Quantity.pieces(shortage) else null,
            alternativeOptions = if (!canFulfill) {
                suggestAlternativeOptions(productId, requestedBase, availableBase)
            } else emptyList()
        )
    }
    
    private fun findOptimalCombination(
        totalUnits: Double,
        hierarchy: PackagingHierarchy
    ): OptimalPackaging {
        val combination = mutableListOf<PackagingComponent>()
        var remaining = totalUnits
        
        // 가장 큰 단위부터 시작
        hierarchy.levels.sortedByDescending { it.unitsPerPackage }.forEach { level ->
            if (remaining >= level.unitsPerPackage * level.minimumUnits) {
                val units = (remaining / level.unitsPerPackage).toInt()
                if (units > 0) {
                    combination.add(
                        PackagingComponent(
                            level = level,
                            count = units,
                            totalUnits = units * level.unitsPerPackage
                        )
                    )
                    remaining -= units * level.unitsPerPackage
                }
            }
        }
        
        // 남은 수량 처리
        if (remaining > 0) {
            combination.add(
                PackagingComponent(
                    level = PackagingLevel("piece", "pieces", 1.0, 1.0),
                    count = remaining.toInt(),
                    totalUnits = remaining
                )
            )
        }
        
        return OptimalPackaging(
            components = combination,
            totalUnits = totalUnits,
            efficiency = calculateEfficiency(combination, hierarchy)
        )
    }
    
    private fun calculateEfficiency(
        components: List<PackagingComponent>,
        hierarchy: PackagingHierarchy
    ): Double {
        // 포장 효율성 계산 (큰 단위 사용 비율)
        val totalUnits = components.sumOf { it.totalUnits }
        val weightedSum = components.sumOf { 
            it.totalUnits * hierarchy.levels.indexOf(it.level).coerceAtLeast(0)
        }
        
        return if (totalUnits > 0) {
            weightedSum / (totalUnits * hierarchy.levels.size)
        } else 0.0
    }
    
    private fun suggestAlternativeOptions(
        productId: String,
        requested: Double,
        available: Double
    ): List<AlternativeOption> {
        val options = mutableListOf<AlternativeOption>()
        val hierarchy = packagingHierarchies[productId] ?: return options
        
        // 가용 수량으로 만들 수 있는 최대 포장 옵션 제안
        hierarchy.levels.forEach { level ->
            val maxUnits = (available / level.unitsPerPackage).toInt()
            if (maxUnits > 0) {
                options.add(
                    AlternativeOption(
                        description = "Available: $maxUnits ${level.displayName}",
                        quantity = Quantity.of(maxUnits.toDouble(), level.symbol),
                        percentageOfRequest = (maxUnits * level.unitsPerPackage) / requested * 100
                    )
                )
            }
        }
        
        return options.sortedByDescending { it.percentageOfRequest }
    }
    
    private fun generateSuggestionReason(
        suggestions: List<PackagingOption>,
        optimal: OptimalPackaging?
    ): String {
        return when {
            optimal == null -> "No optimal packaging found"
            optimal.efficiency > 0.8 -> "Highly efficient packaging combination"
            optimal.efficiency > 0.5 -> "Good packaging combination"
            else -> "Consider ordering in larger units for better efficiency"
        }
    }
}

/**
 * 포장 계층 구조
 */
data class PackagingHierarchy(
    val productId: String,
    val levels: List<PackagingLevel>
) {
    init {
        require(levels.isNotEmpty()) { "At least one packaging level required" }
        // 레벨이 크기 순으로 정렬되어 있는지 확인
        val sorted = levels.sortedBy { it.unitsPerPackage }
        require(sorted == levels) { "Packaging levels must be sorted by units per package" }
    }
    
    companion object {
        /**
         * 표준 계층 구조
         */
        val STANDARD_RETAIL = PackagingHierarchy(
            productId = "standard",
            levels = listOf(
                PackagingLevel("ea", "each", 1.0, 1.0),
                PackagingLevel("pack", "pack", 6.0, 1.0),
                PackagingLevel("box", "box", 24.0, 1.0),
                PackagingLevel("case", "case", 144.0, 1.0),
                PackagingLevel("pallet", "pallet", 2880.0, 1.0)
            )
        )
        
        val STANDARD_WHOLESALE = PackagingHierarchy(
            productId = "wholesale",
            levels = listOf(
                PackagingLevel("box", "box", 12.0, 1.0),
                PackagingLevel("case", "case", 144.0, 1.0),
                PackagingLevel("pallet", "pallet", 1728.0, 1.0),
                PackagingLevel("container", "container", 20736.0, 1.0)
            )
        )
    }
}

/**
 * 포장 레벨 정의
 */
data class PackagingLevel(
    val symbol: String,
    val displayName: String,
    val unitsPerPackage: Double,
    val minimumUnits: Double = 1.0
)

/**
 * 포장 옵션
 */
data class PackagingOption(
    val level: PackagingLevel,
    val quantity: Double,
    val remainder: Double
)

/**
 * 포장 제안
 */
data class PackagingSuggestion(
    val original: Quantity,
    val suggestions: List<PackagingOption>,
    val optimal: OptimalPackaging? = null,
    val reason: String
)

/**
 * 최적 포장 조합
 */
data class OptimalPackaging(
    val components: List<PackagingComponent>,
    val totalUnits: Double,
    val efficiency: Double
)

/**
 * 포장 구성 요소
 */
data class PackagingComponent(
    val level: PackagingLevel,
    val count: Int,
    val totalUnits: Double
)

/**
 * 가용성 확인 결과
 */
data class AvailabilityResult(
    val canFulfill: Boolean,
    val requested: Quantity,
    val available: Quantity,
    val shortage: Quantity?,
    val alternativeOptions: List<AlternativeOption>
)

/**
 * 대안 옵션
 */
data class AlternativeOption(
    val description: String,
    val quantity: Quantity,
    val percentageOfRequest: Double
)
