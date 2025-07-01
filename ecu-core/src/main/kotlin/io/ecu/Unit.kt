package io.ecu

import kotlin.math.pow
import kotlin.math.round
import kotlin.math.floor
import kotlin.math.ceil

/**
 * 모든 물리량의 기본 인터페이스
 * 
 * @param T 구체적인 단위 타입 (예: Length, Weight)
 */
public interface Unit<T : Unit<T>> {
    
    /** 기본 단위로 변환된 값 */
    public val baseValue: Double
    
    /** 현재 단위의 심볼 (예: "kg", "m", "°C") */
    public val symbol: String
    
    /** 현재 단위의 표시명 */
    public val displayName: String
    
    /** 단위 카테고리 */
    public val category: UnitCategory
    
    /**
     * 다른 단위로 변환
     * @param targetSymbol 변환할 단위의 심볼
     * @return 변환된 단위 객체
     */
    public fun to(targetSymbol: String): T
    
    /**
     * 정밀도 설정
     * @param digits 소수점 자릿수
     * @return 정밀도가 설정된 단위 객체
     */
    public fun withPrecision(digits: Int): T
    
    /**
     * 반올림 정책 설정
     * @param mode 반올림 모드
     * @return 반올림 정책이 적용된 단위 객체
     */
    public fun withRounding(mode: RoundingMode): T
    
    /**
     * 현재 값이 유효한지 검증
     * @return 유효성 여부
     */
    public fun isValid(): Boolean
    
    /**
     * 지정된 범위 내에 있는지 확인
     * @param range 확인할 범위
     * @return 범위 내 여부
     */
    public fun isWithinRange(range: ClosedRange<Double>): Boolean
    
    /**
     * 현재 단위를 문자열로 포맷팅
     * @param locale 지역 설정 (옵션)
     * @return 포맷된 문자열
     */
    public fun format(locale: String? = null): String
}

/**
 * 단위 카테고리 열거형
 */
public enum class UnitCategory {
    LENGTH,
    WEIGHT,
    VOLUME,
    TEMPERATURE,
    AREA,
    PRESSURE,
    SPEED,
    ENERGY,
    FLOW,
    POWER,
    TORQUE,
    FREQUENCY,
    QUANTITY
}

/**
 * 반올림 모드 열거형
 */
public enum class RoundingMode {
    /** 0.5에서 올림 */
    HALF_UP,
    /** 0.5에서 내림 */
    HALF_DOWN,
    /** 0.5에서 짝수로 */
    HALF_EVEN,
    /** 항상 올림 */
    UP,
    /** 항상 내림 */
    DOWN
}

/**
 * 단위 변환을 위한 추상 기본 클래스
 */
public abstract class BaseUnit<T : BaseUnit<T>>(
    override val baseValue: Double,
    override val symbol: String,
    override val displayName: String,
    override val category: UnitCategory,
    protected val precision: Int = -1,
    protected val roundingMode: RoundingMode = RoundingMode.HALF_UP
) : Unit<T> {
    
    /**
     * 새로운 인스턴스 생성 (팩토리 메서드)
     */
    protected abstract fun createInstance(
        baseValue: Double,
        symbol: String,
        displayName: String,
        precision: Int = this.precision,
        roundingMode: RoundingMode = this.roundingMode
    ): T
    
    public override fun withPrecision(digits: Int): T {
        require(digits >= 0) { "Precision must be non-negative" }
        return createInstance(baseValue, symbol, displayName, digits, roundingMode)
    }
    
    public override fun withRounding(mode: RoundingMode): T {
        return createInstance(baseValue, symbol, displayName, precision, mode)
    }
    
    public override fun isValid(): Boolean {
        return baseValue.isFinite() && !baseValue.isNaN()
    }
    
    public override fun isWithinRange(range: ClosedRange<Double>): Boolean {
        return baseValue in range
    }
    
    public override fun format(locale: String?): String {
        val formattedValue = if (precision >= 0) {
            applyRounding(baseValue, precision)
        } else {
            baseValue
        }
        
        return "$formattedValue $symbol"
    }
    
    /**
     * 반올림 적용
     */
    protected fun applyRounding(value: Double, digits: Int): Double {
        val factor = 10.0.pow(digits)
        return when (roundingMode) {
            RoundingMode.HALF_UP -> round(value * factor) / factor
            RoundingMode.HALF_DOWN -> {
                val scaled = value * factor
                val truncated = kotlin.math.truncate(scaled)
                val fraction = scaled - truncated
                if (fraction > 0.5) {
                    (truncated + 1) / factor
                } else {
                    truncated / factor
                }
            }
            RoundingMode.HALF_EVEN -> {
                val scaled = value * factor
                val rounded = round(scaled)
                if (kotlin.math.abs(scaled - rounded) == 0.5) {
                    if (rounded.toLong() % 2 == 0L) rounded / factor
                    else floor(scaled) / factor
                } else rounded / factor
            }
            RoundingMode.UP -> ceil(value * factor) / factor
            RoundingMode.DOWN -> floor(value * factor) / factor
        }
    }
    
    public override fun toString(): String = format()
    
    public override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        
        other as BaseUnit<*>
        
        // 같은 카테고리의 단위끼리만 비교하고, baseValue가 충분히 가까우면 같은 것으로 판단
        return category == other.category &&
                kotlin.math.abs(baseValue - other.baseValue) < 1e-10
    }
    
    public override fun hashCode(): Int {
        var result = baseValue.hashCode()
        result = 31 * result + category.hashCode()
        return result
    }
}