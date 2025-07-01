package io.ecu

/**
 * ECU Engineering 모듈의 확장 함수들
 * Core ECU 객체에 엔지니어링 단위들을 추가합니다.
 */

/**
 * 토크 단위 변환을 위한 진입점
 * 
 * @param input 값과 단위가 포함된 문자열 (예: "100 Nm", "50 ft-lb")
 * @return Torque 객체
 */
fun ECU.torque(input: String): Torque {
    return Torque.parse(input)
}

/**
 * 토크 단위 변환을 위한 진입점 (값과 단위 분리)
 * 
 * @param value 숫자 값
 * @param unit 단위 문자열
 * @return Torque 객체
 */
fun ECU.torque(value: Double, unit: String = "Nm"): Torque {
    return Torque.of(value, unit)
}

/**
 * 압력 단위 변환을 위한 진입점
 * 
 * @param input 값과 단위가 포함된 문자열 (예: "100 Pa", "50 psi")
 * @return Pressure 객체
 */
fun ECU.pressure(input: String): Pressure {
    return Pressure.parse(input)
}

/**
 * 압력 단위 변환을 위한 진입점 (값과 단위 분리)
 * 
 * @param value 숫자 값
 * @param unit 단위 문자열
 * @return Pressure 객체
 */
fun ECU.pressure(value: Double, unit: String = "Pa"): Pressure {
    return Pressure.of(value, unit)
}

/**
 * 속도 단위 변환을 위한 진입점
 * 
 * @param input 값과 단위가 포함된 문자열 (예: "100 km/h", "50 mph")
 * @return Speed 객체
 */
fun ECU.speed(input: String): Speed {
    return Speed.parse(input)
}

/**
 * 속도 단위 변환을 위한 진입점 (값과 단위 분리)
 * 
 * @param value 숫자 값
 * @param unit 단위 문자열
 * @return Speed 객체
 */
fun ECU.speed(value: Double, unit: String = "m/s"): Speed {
    return Speed.of(value, unit)
}

/**
 * 에너지 단위 변환을 위한 진입점
 * 
 * @param input 값과 단위가 포함된 문자열 (예: "100 J", "50 kWh")
 * @return Energy 객체
 */
fun ECU.energy(input: String): Energy {
    return Energy.parse(input)
}

/**
 * 에너지 단위 변환을 위한 진입점 (값과 단위 분리)
 * 
 * @param value 숫자 값
 * @param unit 단위 문자열
 * @return Energy 객체
 */
fun ECU.energy(value: Double, unit: String = "J"): Energy {
    return Energy.of(value, unit)
}

/**
 * 배치 변환 시스템 확장
 */

/**
 * 여러 토크를 동일한 단위로 변환
 * 
 * @param inputs 변환할 값들의 리스트
 * @param targetUnit 목표 단위
 * @return 변환된 Torque 객체들의 리스트
 */
fun ECU.Batch.convertTorques(inputs: List<String>, targetUnit: String): List<Torque> {
    return inputs.map { input ->
        Torque.parse(input).to(targetUnit)
    }
}

/**
 * 여러 압력을 동일한 단위로 변환
 * 
 * @param inputs 변환할 값들의 리스트
 * @param targetUnit 목표 단위
 * @return 변환된 Pressure 객체들의 리스트
 */
fun ECU.Batch.convertPressures(inputs: List<String>, targetUnit: String): List<Pressure> {
    return inputs.map { input ->
        Pressure.parse(input).to(targetUnit)
    }
}

/**
 * 여러 속도를 동일한 단위로 변환
 * 
 * @param inputs 변환할 값들의 리스트
 * @param targetUnit 목표 단위
 * @return 변환된 Speed 객체들의 리스트
 */
fun ECU.Batch.convertSpeeds(inputs: List<String>, targetUnit: String): List<Speed> {
    return inputs.map { input ->
        Speed.parse(input).to(targetUnit)
    }
}

/**
 * 여러 에너지를 동일한 단위로 변환
 * 
 * @param inputs 변환할 값들의 리스트
 * @param targetUnit 목표 단위
 * @return 변환된 Energy 객체들의 리스트
 */
fun ECU.Batch.convertEnergies(inputs: List<String>, targetUnit: String): List<Energy> {
    return inputs.map { input ->
        Energy.parse(input).to(targetUnit)
    }
}

/**
 * 단위 정보 조회 시스템 확장
 */

/**
 * 지원되는 모든 토크 단위 조회
 * 
 * @return 토크 단위 심볼들의 집합
 */
fun ECU.Info.getSupportedTorqueUnits(): Set<String> {
    return UnitRegistry.getUnitsByCategory(UnitCategory.TORQUE)
}

/**
 * 지원되는 모든 압력 단위 조회
 * 
 * @return 압력 단위 심볼들의 집합
 */
fun ECU.Info.getSupportedPressureUnits(): Set<String> {
    return UnitRegistry.getUnitsByCategory(UnitCategory.PRESSURE)
}

/**
 * 지원되는 모든 속도 단위 조회
 * 
 * @return 속도 단위 심볼들의 집합
 */
fun ECU.Info.getSupportedSpeedUnits(): Set<String> {
    return UnitRegistry.getUnitsByCategory(UnitCategory.SPEED)
}

/**
 * 지원되는 모든 에너지 단위 조회
 * 
 * @return 에너지 단위 심볼들의 집합
 */
fun ECU.Info.getSupportedEnergyUnits(): Set<String> {
    return UnitRegistry.getUnitsByCategory(UnitCategory.ENERGY)
}
