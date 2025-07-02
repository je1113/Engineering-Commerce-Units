package io.ecu.typesafe

import io.ecu.*

/**
 * 타입 세이프한 단위 정의
 * 
 * 컴파일 타임에 단위 타입을 검증할 수 있도록 합니다.
 * 
 * @since 1.1.0
 */
public sealed class TypeSafeUnit<T : io.ecu.Unit<T>> {
    abstract val symbol: String
    abstract fun convert(value: Double): T
}

// 길이 단위 타입
public sealed class LengthUnit : TypeSafeUnit<Length>() {
    public object Meter : LengthUnit() {
        override val symbol = "m"
        override fun convert(value: Double) = Length.meters(value)
    }
    
    public object Centimeter : LengthUnit() {
        override val symbol = "cm"
        override fun convert(value: Double) = Length.centimeters(value)
    }
    
    public object Kilometer : LengthUnit() {
        override val symbol = "km"
        override fun convert(value: Double) = Length.of(value, "km")
    }
    
    public object Inch : LengthUnit() {
        override val symbol = "in"
        override fun convert(value: Double) = Length.inches(value)
    }
    
    public object Foot : LengthUnit() {
        override val symbol = "ft"
        override fun convert(value: Double) = Length.feet(value)
    }
    
    public object Mile : LengthUnit() {
        override val symbol = "mi"
        override fun convert(value: Double) = Length.of(value, "mi")
    }
}

// 무게 단위 타입
public sealed class WeightUnit : TypeSafeUnit<Weight>() {
    public object Kilogram : WeightUnit() {
        override val symbol = "kg"
        override fun convert(value: Double) = Weight.kilograms(value)
    }
    
    public object Gram : WeightUnit() {
        override val symbol = "g"
        override fun convert(value: Double) = Weight.grams(value)
    }
    
    public object Pound : WeightUnit() {
        override val symbol = "lb"
        override fun convert(value: Double) = Weight.pounds(value)
    }
    
    public object Ounce : WeightUnit() {
        override val symbol = "oz"
        override fun convert(value: Double) = Weight.ounces(value)
    }
}

// 부피 단위 타입
public sealed class VolumeUnit : TypeSafeUnit<Volume>() {
    public object Liter : VolumeUnit() {
        override val symbol = "l"
        override fun convert(value: Double) = Volume.liters(value)
    }
    
    public object Milliliter : VolumeUnit() {
        override val symbol = "ml"
        override fun convert(value: Double) = Volume.milliliters(value)
    }
    
    public object Gallon : VolumeUnit() {
        override val symbol = "gal"
        override fun convert(value: Double) = Volume.gallons(value)
    }
}

// 온도 단위 타입
public sealed class TemperatureUnit : TypeSafeUnit<Temperature>() {
    public object Celsius : TemperatureUnit() {
        override val symbol = "°C"
        override fun convert(value: Double) = Temperature.celsius(value)
    }
    
    public object Fahrenheit : TemperatureUnit() {
        override val symbol = "°F"
        override fun convert(value: Double) = Temperature.fahrenheit(value)
    }
    
    public object Kelvin : TemperatureUnit() {
        override val symbol = "K"
        override fun convert(value: Double) = Temperature.kelvin(value)
    }
}

/**
 * 타입 세이프한 단위 변환 DSL
 */
public class TypeSafeConversion<T : io.ecu.Unit<T>>(
    private val value: Double,
    private val fromUnit: TypeSafeUnit<T>
) {
    private val unit: T = fromUnit.convert(value)
    
    /**
     * 타입 세이프한 변환
     */
    public infix fun to(targetUnit: TypeSafeUnit<T>): T {
        return unit.to(targetUnit.symbol)
    }
    
    /**
     * 값 가져오기
     */
    public fun getValue(): Double = value
    
    /**
     * 단위 가져오기
     */
    public fun getUnit(): T = unit
}

/**
 * 타입 세이프 DSL 진입점
 */
public object TypeSafe {
    // 길이 생성
    public fun length(value: Double, unit: LengthUnit): TypeSafeConversion<Length> {
        return TypeSafeConversion(value, unit)
    }
    
    // 무게 생성
    public fun weight(value: Double, unit: WeightUnit): TypeSafeConversion<Weight> {
        return TypeSafeConversion(value, unit)
    }
    
    // 부피 생성
    public fun volume(value: Double, unit: VolumeUnit): TypeSafeConversion<Volume> {
        return TypeSafeConversion(value, unit)
    }
    
    // 온도 생성
    public fun temperature(value: Double, unit: TemperatureUnit): TypeSafeConversion<Temperature> {
        return TypeSafeConversion(value, unit)
    }
}

/**
 * 편의를 위한 확장 함수
 */
public val Int.meters: Length get() = Length.meters(this.toDouble())
public val Double.meters: Length get() = Length.meters(this)

public val Int.centimeters: Length get() = Length.centimeters(this.toDouble())
public val Double.centimeters: Length get() = Length.centimeters(this)

public val Int.inches: Length get() = Length.inches(this.toDouble())
public val Double.inches: Length get() = Length.inches(this)

public val Int.feet: Length get() = Length.feet(this.toDouble())
public val Double.feet: Length get() = Length.feet(this)

public val Int.kilograms: Weight get() = Weight.kilograms(this.toDouble())
public val Double.kilograms: Weight get() = Weight.kilograms(this)

public val Int.grams: Weight get() = Weight.grams(this.toDouble())
public val Double.grams: Weight get() = Weight.grams(this)

public val Int.pounds: Weight get() = Weight.pounds(this.toDouble())
public val Double.pounds: Weight get() = Weight.pounds(this)

public val Int.liters: Volume get() = Volume.liters(this.toDouble())
public val Double.liters: Volume get() = Volume.liters(this)

public val Int.milliliters: Volume get() = Volume.milliliters(this.toDouble())
public val Double.milliliters: Volume get() = Volume.milliliters(this)

public val Int.celsius: Temperature get() = Temperature.celsius(this.toDouble())
public val Double.celsius: Temperature get() = Temperature.celsius(this)

public val Int.fahrenheit: Temperature get() = Temperature.fahrenheit(this.toDouble())
public val Double.fahrenheit: Temperature get() = Temperature.fahrenheit(this)

/**
 * 타입 세이프한 범위 체크
 */
public class TypeSafeRange<T : io.ecu.Unit<T>>(
    private val min: T,
    private val max: T
) {
    init {
        require(min.baseValue <= max.baseValue) { "Min must be less than or equal to max" }
    }
    
    /**
     * 값이 범위 내에 있는지 확인
     */
    public operator fun contains(value: T): Boolean {
        return value.baseValue in min.baseValue..max.baseValue
    }
    
    /**
     * 범위 내로 값을 제한
     */
    public fun coerce(value: T): T {
        return when {
            value.baseValue < min.baseValue -> min
            value.baseValue > max.baseValue -> max
            else -> value
        }
    }
}

/**
 * 범위 생성 확장 함수
 */
public infix fun <T : io.ecu.Unit<T>> T.until(other: T): TypeSafeRange<T> {
    return TypeSafeRange(this, other)
}

/**
 * 컴파일 타임 단위 검증을 위한 어노테이션
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
public annotation class RequiresUnit(
    val category: String,
    val allowedUnits: Array<String> = []
)

/**
 * 단위 변환 체인을 위한 빌더
 */
public class ConversionChain<T : io.ecu.Unit<T>>(
    private var current: T
) {
    private val history = mutableListOf<Pair<String, T>>()
    
    init {
        history.add("initial" to current)
    }
    
    /**
     * 다음 단위로 변환
     */
    public fun then(targetSymbol: String): ConversionChain<T> {
        current = current.to(targetSymbol)
        history.add(targetSymbol to current)
        return this
    }
    
    /**
     * 조건부 변환
     */
    public fun thenIf(condition: Boolean, targetSymbol: String): ConversionChain<T> {
        if (condition) {
            then(targetSymbol)
        }
        return this
    }
    
    /**
     * 최종 결과
     */
    public fun result(): T = current
    
    /**
     * 변환 이력
     */
    public fun getHistory(): List<Pair<String, T>> = history.toList()
}

/**
 * 체인 시작 확장 함수
 */
public fun <T : io.ecu.Unit<T>> T.chain(): ConversionChain<T> = ConversionChain(this)
