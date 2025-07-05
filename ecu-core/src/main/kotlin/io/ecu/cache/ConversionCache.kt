package io.ecu.cache

import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

/**
 * 단위 변환 결과를 캐싱하는 클래스
 * 
 * 자주 사용되는 변환을 캐싱하여 성능을 향상시킵니다.
 * 
 * @since 1.1.0
 */
public class ConversionCache(
    private val maxSize: Int = 1000,
    private val ttl: Duration = 60.minutes
) {
    private val cache = ConcurrentHashMap<CacheKey, CacheEntry>()
    private val timeSource = TimeSource.Monotonic
    
    /**
     * 캐시 키
     */
    public data class CacheKey(
        val value: Double,
        val fromUnit: String,
        val toUnit: String,
        val precision: Int = -1
    )
    
    /**
     * 캐시 엔트리
     */
    private data class CacheEntry(
        val value: Double,
        val timestamp: TimeSource.Monotonic.ValueTimeMark
    )
    
    /**
     * 캐시에서 값 조회
     */
    public fun get(key: CacheKey): Double? {
        val entry = cache[key] ?: return null
        
        // TTL 확인
        if (entry.timestamp.elapsedNow() > ttl) {
            cache.remove(key)
            return null
        }
        
        return entry.value
    }
    
    /**
     * 캐시에 값 저장
     */
    public fun put(key: CacheKey, value: Double) {
        // 캐시 크기 제한
        if (cache.size >= maxSize) {
            evictOldest()
        }
        
        cache[key] = CacheEntry(value, timeSource.markNow())
    }
    
    /**
     * 캐시에서 값을 조회하거나 계산하여 저장
     */
    public fun getOrCompute(key: CacheKey, compute: () -> Double): Double {
        get(key)?.let { return it }
        
        val computed = compute()
        put(key, computed)
        return computed
    }
    
    /**
     * 가장 오래된 엔트리 제거
     */
    private fun evictOldest() {
        val oldest = cache.entries.minByOrNull { it.value.timestamp }
        oldest?.let { cache.remove(it.key) }
    }
    
    /**
     * 캐시 초기화
     */
    public fun clear() {
        cache.clear()
    }
    
    /**
     * 캐시 통계
     */
    public fun getStats(): CacheStats {
        val entries = cache.entries.toList()
        val now = timeSource.markNow()
        
        return CacheStats(
            size = entries.size,
            oldestEntry = entries.minByOrNull { it.value.timestamp }?.let {
                now - it.value.timestamp
            },
            newestEntry = entries.maxByOrNull { it.value.timestamp }?.let {
                now - it.value.timestamp
            }
        )
    }
    
    /**
     * 캐시 통계 정보
     */
    public data class CacheStats(
        val size: Int,
        val oldestEntry: Duration?,
        val newestEntry: Duration?
    )
}

/**
 * 전역 변환 캐시
 */
public object GlobalConversionCache {
    private val cache = ConversionCache()
    
    /**
     * 캐싱된 변환 수행
     */
    @JvmStatic
    public fun cachedConvert(
        value: Double,
        fromUnit: String,
        toUnit: String,
        precision: Int = -1,
        compute: () -> Double
    ): Double {
        val key = ConversionCache.CacheKey(value, fromUnit, toUnit, precision)
        return cache.getOrCompute(key, compute)
    }
    
    /**
     * 캐시 초기화
     */
    @JvmStatic
    public fun clear() {
        cache.clear()
    }
    
    /**
     * 캐시 통계 조회
     */
    @JvmStatic
    public fun getStats(): ConversionCache.CacheStats {
        return cache.getStats()
    }
}
