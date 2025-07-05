package io.ecu.module

import io.ecu.UnitRegistry

/**
 * 모듈 관리를 위한 레지스트리
 * 
 * 등록된 모듈들을 추적하고 중복 등록을 방지합니다.
 * 
 * @since 1.1.0
 */
public object ModuleRegistry {
    private val registeredModules = mutableSetOf<String>()
    private val moduleInstances = mutableMapOf<String, UnitModule>()
    
    /**
     * 모듈 등록
     * 
     * @param module 등록할 모듈
     * @param force 기존 모듈을 덮어쓸지 여부
     * @return 모듈이 성공적으로 등록되었는지 여부
     */
    @JvmStatic
    public fun register(module: UnitModule, force: Boolean = false): Boolean {
        val moduleId = module.getModuleId()
        
        if (registeredModules.contains(moduleId) && !force) {
            return false
        }
        
        // 모듈 구성 실행
        module.configure(UnitRegistry)
        
        // 모듈 추적
        registeredModules.add(moduleId)
        moduleInstances[moduleId] = module
        
        return true
    }
    
    /**
     * 여러 모듈을 한 번에 등록
     */
    @JvmStatic
    public fun registerAll(vararg modules: UnitModule) {
        modules.forEach { register(it) }
    }
    
    /**
     * 모듈이 등록되었는지 확인
     */
    @JvmStatic
    public fun isRegistered(module: UnitModule): Boolean {
        return registeredModules.contains(module.getModuleId())
    }
    
    /**
     * 모듈 이름으로 등록 여부 확인
     */
    @JvmStatic
    public fun isRegistered(name: String, version: String): Boolean {
        return registeredModules.contains("$name:$version")
    }
    
    /**
     * 등록된 모든 모듈 ID 조회
     */
    @JvmStatic
    public fun getRegisteredModules(): Set<String> {
        return registeredModules.toSet()
    }
    
    /**
     * 특정 모듈 인스턴스 조회
     */
    @JvmStatic
    public fun getModule(moduleId: String): UnitModule? {
        return moduleInstances[moduleId]
    }
    
    /**
     * 모듈 레지스트리 초기화 (테스트용)
     */
    internal fun reset() {
        registeredModules.clear()
        moduleInstances.clear()
    }
}
