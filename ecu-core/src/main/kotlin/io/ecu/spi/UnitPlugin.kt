package io.ecu.spi

import io.ecu.UnitRegistry
import io.ecu.module.UnitModule

/**
 * 단위 플러그인 인터페이스
 * 
 * Service Provider Interface (SPI)를 통해 자동으로 발견되고 로드됩니다.
 * 
 * @since 1.1.0
 */
public interface UnitPlugin {
    /**
     * 플러그인 이름
     */
    val name: String
    
    /**
     * 플러그인 버전
     */
    val version: String
    
    /**
     * 플러그인 설명
     */
    val description: String
    
    /**
     * 플러그인 우선순위 (낮을수록 먼저 로드)
     */
    val priority: Int
        get() = 100
    
    /**
     * 플러그인 활성화 여부
     */
    fun isEnabled(): Boolean = true
    
    /**
     * 플러그인 초기화
     * 
     * @param context 플러그인 컨텍스트
     */
    fun initialize(context: PluginContext)
    
    /**
     * 플러그인 종료
     */
    fun shutdown() {}
    
    /**
     * 제공하는 모듈들
     */
    fun getModules(): List<UnitModule> = emptyList()
}

/**
 * 플러그인 컨텍스트
 */
public interface PluginContext {
    /**
     * 메인 레지스트리
     */
    val registry: UnitRegistry
    
    /**
     * 설정 값 조회
     */
    fun getConfig(key: String): String?
    
    /**
     * 다른 플러그인 조회
     */
    fun getPlugin(name: String): UnitPlugin?
    
    /**
     * 로깅
     */
    fun log(level: LogLevel, message: String)
}

/**
 * 로그 레벨
 */
public enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

/**
 * 플러그인 매니저
 */
public object PluginManager {
    private val plugins = mutableMapOf<String, UnitPlugin>()
    private val serviceLoader = java.util.ServiceLoader.load(UnitPlugin::class.java)
    
    /**
     * 모든 플러그인 로드
     */
    @JvmStatic
    public fun loadPlugins(context: PluginContext = DefaultPluginContext()) {
        // SPI를 통해 플러그인 발견
        val discovered = serviceLoader
            .filter { it.isEnabled() }
            .sortedBy { it.priority }
        
        // 플러그인 초기화
        discovered.forEach { plugin ->
            try {
                context.log(LogLevel.INFO, "Loading plugin: ${plugin.name} v${plugin.version}")
                plugin.initialize(context)
                plugins[plugin.name] = plugin
                
                // 플러그인이 제공하는 모듈 등록
                plugin.getModules().forEach { module ->
                    module.configure(context.registry)
                }
                
                context.log(LogLevel.INFO, "Successfully loaded plugin: ${plugin.name}")
            } catch (e: Exception) {
                context.log(LogLevel.ERROR, "Failed to load plugin ${plugin.name}: ${e.message}")
            }
        }
    }
    
    /**
     * 특정 플러그인 로드
     */
    @JvmStatic
    public fun loadPlugin(plugin: UnitPlugin, context: PluginContext = DefaultPluginContext()) {
        if (!plugin.isEnabled()) return
        
        plugin.initialize(context)
        plugins[plugin.name] = plugin
        
        plugin.getModules().forEach { module ->
            module.configure(context.registry)
        }
    }
    
    /**
     * 등록된 플러그인 조회
     */
    @JvmStatic
    public fun getPlugin(name: String): UnitPlugin? {
        return plugins[name]
    }
    
    /**
     * 모든 플러그인 조회
     */
    @JvmStatic
    public fun getAllPlugins(): Map<String, UnitPlugin> {
        return plugins.toMap()
    }
    
    /**
     * 플러그인 언로드
     */
    @JvmStatic
    public fun unloadPlugin(name: String) {
        plugins[name]?.shutdown()
        plugins.remove(name)
    }
    
    /**
     * 모든 플러그인 언로드
     */
    @JvmStatic
    public fun unloadAll() {
        plugins.values.forEach { it.shutdown() }
        plugins.clear()
    }
}

/**
 * 기본 플러그인 컨텍스트 구현
 */
private class DefaultPluginContext : PluginContext {
    override val registry: UnitRegistry = UnitRegistry
    private val config = mutableMapOf<String, String>()
    
    override fun getConfig(key: String): String? = config[key]
    
    override fun getPlugin(name: String): UnitPlugin? = PluginManager.getPlugin(name)
    
    override fun log(level: LogLevel, message: String) {
        println("[${level.name}] $message")
    }
}

/**
 * 플러그인 기본 구현을 위한 추상 클래스
 */
public abstract class AbstractUnitPlugin(
    override val name: String,
    override val version: String,
    override val description: String = ""
) : UnitPlugin {
    
    protected lateinit var context: PluginContext
    
    override fun initialize(context: PluginContext) {
        this.context = context
        onInitialize()
    }
    
    /**
     * 서브클래스에서 구현할 초기화 로직
     */
    protected abstract fun onInitialize()
    
    /**
     * 편의 메서드: 로깅
     */
    protected fun log(level: LogLevel, message: String) {
        context.log(level, message)
    }
    
    /**
     * 편의 메서드: 설정 조회
     */
    protected fun getConfig(key: String, default: String? = null): String? {
        return context.getConfig(key) ?: default
    }
}