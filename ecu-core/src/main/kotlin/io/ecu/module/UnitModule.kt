package io.ecu.module

import io.ecu.UnitRegistry

/**
 * 단위 모듈 인터페이스
 * 
 * Jackson의 Module 시스템과 유사하게 설계되어,
 * 관련된 단위들을 그룹화하여 등록할 수 있습니다.
 * 
 * @since 1.1.0
 */
public interface UnitModule {
    /**
     * 모듈 이름
     */
    val name: String
    
    /**
     * 모듈 버전
     */
    val version: String
    
    /**
     * 모듈 설명
     */
    val description: String
        get() = "No description provided"
    
    /**
     * 모듈을 구성하고 단위들을 레지스트리에 등록
     * 
     * @param registry 단위를 등록할 레지스트리
     */
    fun configure(registry: UnitRegistry)
    
    /**
     * 모듈이 이미 등록되었는지 확인하기 위한 고유 ID
     */
    fun getModuleId(): String = "$name:$version"
}

/**
 * 단순한 단위 모듈 구현을 위한 추상 클래스
 */
public abstract class SimpleUnitModule(
    override val name: String,
    override val version: String = "1.0.0"
) : UnitModule {
    
    /**
     * 단위 등록을 위한 DSL 헬퍼 메서드
     */
    protected fun UnitRegistry.unit(block: io.ecu.CustomUnitBuilder.() -> Unit) {
        registerCustomUnit(block)
    }
}
