package io.ecu

/**
 * ECU에 대한 확장 함수들
 * Commerce 모듈에서 사용하는 추가 기능을 제공합니다.
 */

// Quantity 클래스의 초기화를 보장
private val quantityInitializer = Quantity.pieces(0.0)

/**
 * 수량 단위 변환을 위한 진입점
 * 
 * @param input 값과 단위가 포함된 문자열 (예: "12 dozen", "100 pieces")
 * @return Quantity 객체
 */
fun ECU.quantity(input: String): Quantity {
    return Quantity.parse(input)
}

/**
 * 수량 단위 변환을 위한 진입점 (값과 단위 분리)
 * 
 * @param value 숫자 값
 * @param unit 단위 문자열
 * @return Quantity 객체
 */
fun ECU.quantity(value: Double, unit: String = "pcs"): Quantity {
    return Quantity.of(value, unit)
}
