package io.ecu

import io.ecu.RoundingMode
import io.ecu.RoundingProfile as CoreRoundingProfile
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

/**
 * 상거래용 확장 라운딩 프로파일
 * 상거래에서 필요한 다양한 라운딩 규칙을 정의합니다.
 */
data class CommerceRoundingProfile(
    /** 최소 주문 단위 (MOQ - Minimum Order Quantity) */
    val minimumOrderQuantity: Double = 1.0,
    
    /** 포장/배송 단위 */
    val packagingUnit: Double = 1.0,
    
    /** 라운딩 모드 */
    val roundingMode: RoundingMode = RoundingMode.UP,
    
    /** 증분 단위 (예: 6개 단위로만 주문 가능) */
    val incrementUnit: Double = 1.0,
    
    /** 최대 주문 수량 제한 */
    val maximumOrderQuantity: Double? = null,
    
    /** 특별 라운딩 규칙 */
    val specialRules: List<SpecialRule> = emptyList()
) {
    
    /**
     * 특별 라운딩 규칙
     */
    data class SpecialRule(
        /** 규칙이 적용되는 범위 */
        val range: ClosedRange<Double>,
        
        /** 해당 범위에서의 라운딩 모드 */
        val mode: RoundingMode,
        
        /** 해당 범위에서의 증분 단위 */
        val increment: Double? = null
    )
    
    /**
     * 수량에 라운딩 프로파일 적용
     */
    fun applyRounding(quantity: Double): Double {
        // 1. 최소 주문 수량 확인
        if (quantity < minimumOrderQuantity) {
            return when (roundingMode) {
                RoundingMode.UP -> minimumOrderQuantity
                RoundingMode.DOWN -> 0.0
                else -> minimumOrderQuantity
            }
        }
        
        // 2. 최대 주문 수량 확인
        maximumOrderQuantity?.let { max ->
            if (quantity > max) {
                return when (roundingMode) {
                    RoundingMode.DOWN -> max
                    RoundingMode.UP -> throw IllegalArgumentException(
                        "Quantity $quantity exceeds maximum order quantity $max"
                    )
                    else -> max
                }
            }
        }
        
        // 3. 특별 규칙 확인
        specialRules.forEach { rule ->
            if (quantity in rule.range) {
                val increment = rule.increment ?: incrementUnit
                return applyRoundingWithMode(quantity, increment, rule.mode)
            }
        }
        
        // 4. 포장 단위로 라운딩
        val roundedToPackaging = applyRoundingWithMode(quantity, packagingUnit, roundingMode)
        
        // 5. 증분 단위 확인
        if (incrementUnit > 1.0) {
            return applyRoundingWithMode(roundedToPackaging, incrementUnit, roundingMode)
        }
        
        return roundedToPackaging
    }
    
    /**
     * 특정 모드와 단위로 라운딩 수행
     */
    private fun applyRoundingWithMode(
        value: Double,
        unit: Double,
        mode: RoundingMode
    ): Double {
        if (unit <= 0) return value
        
        val factor = value / unit
        
        return when (mode) {
            RoundingMode.UP -> ceil(factor) * unit
            RoundingMode.DOWN -> floor(factor) * unit
            RoundingMode.HALF_UP -> round(factor) * unit
            RoundingMode.HALF_DOWN -> {
                val truncated = factor.toInt()
                val fraction = factor - truncated
                if (fraction > 0.5) {
                    (truncated + 1) * unit
                } else {
                    truncated * unit
                }
            }
            RoundingMode.HALF_EVEN -> {
                val rounded = round(factor)
                if (kotlin.math.abs(factor - rounded) == 0.5) {
                    if (rounded.toLong() % 2 == 0L) rounded * unit
                    else floor(factor) * unit
                } else rounded * unit
            }
        }
    }
    
    /**
     * 수량이 유효한지 검증
     */
    fun isValidQuantity(quantity: Double): ValidationResult {
        // 최소 수량 검증
        if (quantity < minimumOrderQuantity) {
            return ValidationResult(
                isValid = false,
                reason = "Quantity must be at least $minimumOrderQuantity",
                suggestedQuantity = minimumOrderQuantity
            )
        }
        
        // 최대 수량 검증
        maximumOrderQuantity?.let { max ->
            if (quantity > max) {
                return ValidationResult(
                    isValid = false,
                    reason = "Quantity cannot exceed $max",
                    suggestedQuantity = max
                )
            }
        }
        
        // 증분 단위 검증
        if (incrementUnit > 1.0) {
            val remainder = quantity % incrementUnit
            if (remainder > 0.001) { // 부동소수점 오차 고려
                val suggested = applyRounding(quantity)
                return ValidationResult(
                    isValid = false,
                    reason = "Quantity must be in increments of $incrementUnit",
                    suggestedQuantity = suggested
                )
            }
        }
        
        // 포장 단위 검증
        if (packagingUnit > 1.0) {
            val remainder = quantity % packagingUnit
            if (remainder > 0.001) { // 부동소수점 오차 고려
                val suggested = applyRounding(quantity)
                return ValidationResult(
                    isValid = false,
                    reason = "Quantity must be in multiples of packaging unit ($packagingUnit)",
                    suggestedQuantity = suggested
                )
            }
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * 검증 결과
     */
    data class ValidationResult(
        val isValid: Boolean,
        val reason: String? = null,
        val suggestedQuantity: Double? = null
    )
    
    /**
     * Core RoundingProfile로 변환
     */
    fun toCoreProfile(): CoreRoundingProfile {
        return CoreRoundingProfile(
            minimumOrderQuantity = minimumOrderQuantity,
            packagingUnit = packagingUnit,
            roundingMode = roundingMode,
            allowFractional = false
        )
    }
    
    companion object {
        /**
         * 일반적인 프로파일 프리셋
         */
        
        /** 소매용 프로파일 (개별 판매) */
        val RETAIL = CommerceRoundingProfile(
            minimumOrderQuantity = 1.0,
            packagingUnit = 1.0,
            roundingMode = RoundingMode.UP,
            incrementUnit = 1.0
        )
        
        /** 도매용 프로파일 (박스 단위) */
        val WHOLESALE = CommerceRoundingProfile(
            minimumOrderQuantity = 12.0,
            packagingUnit = 12.0,
            roundingMode = RoundingMode.UP,
            incrementUnit = 12.0
        )
        
        /** 벌크 프로파일 (팔레트 단위) */
        val BULK = CommerceRoundingProfile(
            minimumOrderQuantity = 144.0,
            packagingUnit = 144.0,
            roundingMode = RoundingMode.UP,
            incrementUnit = 144.0,
            specialRules = listOf(
                SpecialRule(
                    range = 0.0..143.9,
                    mode = RoundingMode.UP,
                    increment = 144.0
                )
            )
        )
        
        /** B2B 프로파일 (유연한 대량 주문) */
        val B2B = CommerceRoundingProfile(
            minimumOrderQuantity = 100.0,
            packagingUnit = 10.0,
            roundingMode = RoundingMode.HALF_UP,
            incrementUnit = 10.0,
            maximumOrderQuantity = 10000.0
        )
        
        /** 음료 6팩 프로파일 */
        val BEVERAGE_6PACK = CommerceRoundingProfile(
            minimumOrderQuantity = 6.0,
            packagingUnit = 6.0,
            roundingMode = RoundingMode.UP,
            incrementUnit = 6.0
        )
        
        /** 계란 프로파일 (12개 단위) */
        val EGG_DOZEN = CommerceRoundingProfile(
            minimumOrderQuantity = 12.0,
            packagingUnit = 12.0,
            roundingMode = RoundingMode.UP,
            incrementUnit = 6.0, // 반다스도 허용
            specialRules = listOf(
                SpecialRule(
                    range = 0.0..5.9,
                    mode = RoundingMode.UP,
                    increment = 6.0
                )
            )
        )
        
        /** 종이 프로파일 (500장 = 1 ream) */
        val PAPER_REAM = CommerceRoundingProfile(
            minimumOrderQuantity = 500.0,
            packagingUnit = 500.0,
            roundingMode = RoundingMode.UP,
            incrementUnit = 500.0
        )
    }
}

/**
 * 여러 프로파일 중 가장 적합한 것 선택
 */
class RoundingProfileSelector {
    private val profiles = mutableMapOf<String, CommerceRoundingProfile>()
    
    init {
        // 기본 프로파일 등록
        register("retail", CommerceRoundingProfile.RETAIL)
        register("wholesale", CommerceRoundingProfile.WHOLESALE)
        register("bulk", CommerceRoundingProfile.BULK)
        register("b2b", CommerceRoundingProfile.B2B)
        register("beverage", CommerceRoundingProfile.BEVERAGE_6PACK)
        register("egg", CommerceRoundingProfile.EGG_DOZEN)
        register("paper", CommerceRoundingProfile.PAPER_REAM)
    }
    
    /**
     * 프로파일 등록
     */
    fun register(name: String, profile: CommerceRoundingProfile) {
        profiles[name.lowercase()] = profile
    }
    
    /**
     * 프로파일 조회
     */
    fun get(name: String): CommerceRoundingProfile? {
        return profiles[name.lowercase()]
    }
    
    /**
     * 수량에 따라 자동으로 프로파일 선택
     */
    fun selectAutomatic(quantity: Double, productType: String? = null): CommerceRoundingProfile {
        // 제품 타입이 지정된 경우
        productType?.let { type ->
            profiles[type.lowercase()]?.let { return it }
        }
        
        // 수량에 따른 자동 선택
        return when {
            quantity < 10 -> CommerceRoundingProfile.RETAIL
            quantity < 100 -> CommerceRoundingProfile.WHOLESALE
            quantity < 1000 -> CommerceRoundingProfile.B2B
            else -> CommerceRoundingProfile.BULK
        }
    }
    
    /**
     * 가장 경제적인 프로파일 찾기
     */
    fun findMostEconomical(
        desiredQuantity: Double,
        availableProfiles: List<String> = profiles.keys.toList()
    ): ProfileRecommendation {
        val recommendations = mutableListOf<ProfileOption>()
        
        availableProfiles.forEach { profileName ->
            profiles[profileName]?.let { profile ->
                val rounded = profile.applyRounding(desiredQuantity)
                val waste = rounded - desiredQuantity
                val wastePercentage = if (desiredQuantity > 0) {
                    (waste / desiredQuantity) * 100
                } else 0.0
                
                recommendations.add(
                    ProfileOption(
                        profileName = profileName,
                        profile = profile,
                        resultQuantity = rounded,
                        waste = waste,
                        wastePercentage = wastePercentage
                    )
                )
            }
        }
        
        // 낭비가 가장 적은 프로파일 선택
        val best = recommendations.minByOrNull { it.waste }
        
        return ProfileRecommendation(
            desiredQuantity = desiredQuantity,
            options = recommendations.sortedBy { it.waste },
            recommended = best
        )
    }
    
    /**
     * 프로파일 옵션
     */
    data class ProfileOption(
        val profileName: String,
        val profile: CommerceRoundingProfile,
        val resultQuantity: Double,
        val waste: Double,
        val wastePercentage: Double
    )
    
    /**
     * 프로파일 추천 결과
     */
    data class ProfileRecommendation(
        val desiredQuantity: Double,
        val options: List<ProfileOption>,
        val recommended: ProfileOption?
    ) {
        fun format(): String {
            val sb = StringBuilder()
            sb.appendLine("Desired quantity: $desiredQuantity")
            sb.appendLine("\nAvailable options:")
            
            options.forEach { option ->
                val marker = if (option == recommended) " [RECOMMENDED]" else ""
                sb.appendLine("  ${option.profileName}$marker:")
                sb.appendLine("    Result: ${option.resultQuantity}")
                sb.appendLine("    Waste: ${option.waste} (${String.format("%.1f", option.wastePercentage)}%)")
            }
            
            return sb.toString()
        }
    }
}
