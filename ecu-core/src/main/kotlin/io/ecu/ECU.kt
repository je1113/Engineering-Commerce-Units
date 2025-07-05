package io.ecu

import io.ecu.module.UnitModule
import io.ecu.module.ModuleRegistry
import io.ecu.loader.UnitDefinitionLoader
import io.ecu.loader.UnitDefinitionApplier
import io.ecu.loader.StringUnitDefinitionLoader

/**
 * Engineering Commerce Units Core의 메인 진입점
 * 
 * ECU Core는 기본적인 단위 변환 기능을 제공합니다:
 * - Length (길이): m, cm, mm, km, in, ft, yd, mi
 * - Weight (무게): kg, g, mg, t, lb, oz
 * - Volume (부피): l, ml, m³, gal, qt, pt, fl oz
 * - Temperature (온도): K, °C, °F
 * 
 * Java 8 호환성을 위해 @JvmStatic 어노테이션을 사용합니다.
 */
public object ECU {
    
    /**
     * 길이 단위 변환을 위한 진입점
     * 
     * @param input 값과 단위가 포함된 문자열 (예: "100 cm", "5.5 ft")
     * @return Length 객체
     */
    @JvmStatic
    public fun length(input: String): Length {
        return Length.parse(input)
    }
    
    /**
     * 길이 단위 변환을 위한 진입점 (값과 단위 분리)
     * 
     * @param value 숫자 값
     * @param unit 단위 문자열
     * @return Length 객체
     */
    @JvmStatic
    @JvmOverloads
    public fun length(value: Double, unit: String = "m"): Length {
        return Length.of(value, unit)
    }
    
    /**
     * 무게 단위 변환을 위한 진입점
     * 
     * @param input 값과 단위가 포함된 문자열 (예: "500 g", "2.5 kg")
     * @return Weight 객체
     */
    @JvmStatic
    public fun weight(input: String): Weight {
        return Weight.parse(input)
    }
    
    /**
     * 무게 단위 변환을 위한 진입점 (값과 단위 분리)
     * 
     * @param value 숫자 값
     * @param unit 단위 문자열
     * @return Weight 객체
     */
    @JvmStatic
    @JvmOverloads
    public fun weight(value: Double, unit: String = "kg"): Weight {
        return Weight.of(value, unit)
    }
    
    /**
     * 부피 단위 변환을 위한 진입점
     * 
     * @param input 값과 단위가 포함된 문자열 (예: "250 ml", "3.5 l")
     * @return Volume 객체
     */
    @JvmStatic
    public fun volume(input: String): Volume {
        return Volume.parse(input)
    }
    
    /**
     * 부피 단위 변환을 위한 진입점 (값과 단위 분리)
     * 
     * @param value 숫자 값
     * @param unit 단위 문자열
     * @return Volume 객체
     */
    @JvmStatic
    @JvmOverloads
    public fun volume(value: Double, unit: String = "l"): Volume {
        return Volume.of(value, unit)
    }
    
    /**
     * 온도 단위 변환을 위한 진입점
     * 
     * @param input 값과 단위가 포함된 문자열 (예: "25 °C", "100 °F")
     * @return Temperature 객체
     */
    @JvmStatic
    public fun temperature(input: String): Temperature {
        return Temperature.parse(input)
    }
    
    /**
     * 온도 단위 변환을 위한 진입점 (값과 단위 분리)
     * 
     * @param value 숫자 값
     * @param unit 단위 문자열
     * @return Temperature 객체
     */
    @JvmStatic
    @JvmOverloads
    public fun temperature(value: Double, unit: String = "°C"): Temperature {
        return Temperature.of(value, unit)
    }

    /**
     * 면적 단위 변환을 위한 진입점
     *
     * @param input 값과 단위가 포함된 문자열 (예: "25 m²", "100 ft²")
     * @return Area 객체
     */
    @JvmStatic
    public fun area(input: String): Area {
        return Area.parse(input)
    }

    /**
     * 면적 단위 변환을 위한 진입점 (값과 단위 분리)
     *
     * @param value 숫자 값
     * @param unit  단위 문자열
     * @return Area 객체
     */
    @JvmStatic
    @JvmOverloads
    public fun area(value: Double, unit: String = "m²"): Area {
        return Area.of(value, unit)
    }

    /**
     * 배치 변환 시스템
     * 
     * 여러 값을 동시에 변환할 때 사용합니다.
     */
    public object Batch {
        /**
         * 여러 길이를 동일한 단위로 변환
         * 
         * @param inputs 변환할 값들의 리스트
         * @param targetUnit 목표 단위
         * @return 변환된 Length 객체들의 리스트
         */
        @JvmStatic
        public fun convertLengths(inputs: List<String>, targetUnit: String): List<Length> {
            return inputs.map { input ->
                length(input).to(targetUnit)
            }
        }
        
        /**
         * 여러 무게를 동일한 단위로 변환
         * 
         * @param inputs 변환할 값들의 리스트
         * @param targetUnit 목표 단위
         * @return 변환된 Weight 객체들의 리스트
         */
        @JvmStatic
        public fun convertWeights(inputs: List<String>, targetUnit: String): List<Weight> {
            return inputs.map { input ->
                weight(input).to(targetUnit)
            }
        }
        
        /**
         * 여러 부피를 동일한 단위로 변환
         * 
         * @param inputs 변환할 값들의 리스트
         * @param targetUnit 목표 단위
         * @return 변환된 Volume 객체들의 리스트
         */
        @JvmStatic
        public fun convertVolumes(inputs: List<String>, targetUnit: String): List<Volume> {
            return inputs.map { input ->
                volume(input).to(targetUnit)
            }
        }
        
        /**
         * 여러 온도를 동일한 단위로 변환
         * 
         * @param inputs 변환할 값들의 리스트
         * @param targetUnit 목표 단위
         * @return 변환된 Temperature 객체들의 리스트
         */
        @JvmStatic
        public fun convertTemperatures(inputs: List<String>, targetUnit: String): List<Temperature> {
            return inputs.map { input ->
                temperature(input).to(targetUnit)
            }
        }

        /**
         * 문자열 리스트를 원하는 면적 단위로 일괄 변환
         *
         * @param inputs   "100m²", "2 acre" 와 같은 입력 목록
         * @param targetUnit 결과 단위 (기본은 m²)
         * @return 변환된 Area 객체 리스트
         */
        @JvmStatic
        @JvmOverloads
        fun convertAreas(
            inputs: List<String>,
            targetUnit: String = "m²"
        ): List<Area> = inputs.map { Area.parse(it).to(targetUnit) }

    }
    
    /**
     * 단위 정보 조회 시스템
     * 
     * 지원되는 단위들의 정보를 조회합니다.
     */
    public object Info {
        /**
         * 지원되는 모든 길이 단위 조회
         * 
         * @return 길이 단위 심볼들의 집합
         */
        @JvmStatic
        public fun getSupportedLengthUnits(): Set<String> {
            return UnitRegistry.getUnitsByCategory(UnitCategory.LENGTH)
        }
        
        /**
         * 지원되는 모든 무게 단위 조회
         * 
         * @return 무게 단위 심볼들의 집합
         */
        @JvmStatic
        public fun getSupportedWeightUnits(): Set<String> {
            return UnitRegistry.getUnitsByCategory(UnitCategory.WEIGHT)
        }
        
        /**
         * 지원되는 모든 부피 단위 조회
         * 
         * @return 부피 단위 심볼들의 집합
         */
        @JvmStatic
        public fun getSupportedVolumeUnits(): Set<String> {
            return UnitRegistry.getUnitsByCategory(UnitCategory.VOLUME)
        }
        
        /**
         * 지원되는 모든 온도 단위 조회
         * 
         * @return 온도 단위 심볼들의 집합
         */
        @JvmStatic
        public fun getSupportedTemperatureUnits(): Set<String> {
            return UnitRegistry.getUnitsByCategory(UnitCategory.TEMPERATURE)
        }

        /**
         * 지원되는 모든 면적 단위 조회
         *
         * @return 면적 단위 심볼들의 집합
         */
        @JvmStatic
        public fun getSupportedAreaUnits(): Set<String> {
            return UnitRegistry.getUnitsByCategory(UnitCategory.AREA)
        }


        /**
         * 단위가 유효한지 확인
         * 
         * @param unit 확인할 단위 심볼
         * @return 유효 여부
         */
        @JvmStatic
        public fun isValidUnit(unit: String): Boolean {
            return UnitRegistry.isValidUnit(unit)
        }
        
        /**
         * 단위 정보 조회
         * 
         * @param unit 조회할 단위 심볼
         * @return 단위 정의 또는 null
         */
        @JvmStatic
        public fun getUnitInfo(unit: String): UnitDefinition? {
            return UnitRegistry.getDefinition(unit)
        }
    }
    /**
     * 모듈 등록
     * 
     * Jackson 스타일의 모듈 시스템을 사용하여 커스텀 단위들을 그룹으로 등록합니다.
     * 
     * @param module 등록할 단위 모듈
     * @return 모듈이 성공적으로 등록되었는지 여부
     * @since 1.1.0
     */
    @JvmStatic
    public fun registerModule(module: UnitModule): Boolean {
        return ModuleRegistry.register(module)
    }
    
    /**
     * 여러 모듈을 한 번에 등록
     * 
     * @param modules 등록할 모듈들
     * @since 1.1.0
     */
    @JvmStatic
    public fun registerModules(vararg modules: UnitModule) {
        ModuleRegistry.registerAll(*modules)
    }
    
    /**
     * JSON 문자열에서 단위 정의 로드
     * 
     * @param json JSON 형식의 단위 정의
     * @since 1.1.0
     */
    @JvmStatic
    public fun loadFromJson(json: String) {
        val loader = StringUnitDefinitionLoader(json)
        UnitDefinitionApplier.applyDefinitions(loader)
    }
    
    /**
     * 커스텀 로더에서 단위 정의 로드
     * 
     * @param loader 단위 정의 로더
     * @since 1.1.0
     */
    @JvmStatic
    public fun loadFromLoader(loader: UnitDefinitionLoader) {
        UnitDefinitionApplier.applyDefinitions(loader)
    }
}

/**
 * 전역 ECU 인스턴스 (편의를 위한 별칭)
 * 
 * Kotlin에서는 `ecu.length("100 cm")` 형태로 사용할 수 있습니다.
 */
public val ecu: ECU = ECU
