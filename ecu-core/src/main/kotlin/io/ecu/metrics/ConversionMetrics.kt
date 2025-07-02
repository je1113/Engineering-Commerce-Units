package io.ecu.metrics

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.LongAdder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.TimeSource

/**
 * 단위 변환 메트릭을 수집하는 인터페이스
 * 
 * @since 1.1.0
 */
public interface ConversionMetrics {
    /**
     * 변환 기록
     */
    fun recordConversion(
        from: String,
        to: String,
        duration: Duration,
        success: Boolean = true
    )
    
    /**
     * 오류 기록
     */
    fun recordError(
        from: String,
        to: String,
        error: Throwable
    )
    
    /**
     * 메트릭 조회
     */
    fun getMetrics(): MetricsSnapshot
    
    /**
     * 메트릭 초기화
     */
    fun reset()
}

/**
 * 메트릭 스냅샷
 */
public data class MetricsSnapshot(
    val totalConversions: Long,
    val successfulConversions: Long,
    val failedConversions: Long,
    val averageConversionTime: Duration,
    val mostUsedConversions: List<ConversionPair>,
    val errorsByType: Map<String, Long>,
    val conversionsByCategory: Map<String, Long>
)

/**
 * 변환 쌍
 */
public data class ConversionPair(
    val from: String,
    val to: String,
    val count: Long,
    val averageTime: Duration
)

/**
 * 기본 메트릭 구현
 */
public class DefaultConversionMetrics : ConversionMetrics {
    private val conversionCounts = ConcurrentHashMap<String, LongAdder>()
    private val conversionTimes = ConcurrentHashMap<String, AtomicLong>()
    private val errorCounts = ConcurrentHashMap<String, LongAdder>()
    private val categoryCounts = ConcurrentHashMap<String, LongAdder>()
    
    private val totalCount = LongAdder()
    private val successCount = LongAdder()
    private val failureCount = LongAdder()
    private val totalTime = AtomicLong(0)
    
    override fun recordConversion(
        from: String,
        to: String,
        duration: Duration,
        success: Boolean
    ) {
        val key = "$from->$to"
        
        totalCount.increment()
        if (success) {
            successCount.increment()
        } else {
            failureCount.increment()
        }
        
        conversionCounts.computeIfAbsent(key) { LongAdder() }.increment()
        
        val nanos = duration.inWholeNanoseconds
        conversionTimes.computeIfAbsent(key) { AtomicLong(0) }
            .addAndGet(nanos)
        totalTime.addAndGet(nanos)
        
        // 카테고리별 통계 (단위의 첫 글자로 간단히 분류)
        val category = when {
            from.startsWith("m") || from.startsWith("k") || from.startsWith("c") -> "length"
            from.startsWith("kg") || from.startsWith("g") || from.startsWith("lb") -> "weight"
            from.startsWith("l") || from.startsWith("ml") || from.startsWith("gal") -> "volume"
            else -> "other"
        }
        categoryCounts.computeIfAbsent(category) { LongAdder() }.increment()
    }
    
    override fun recordError(from: String, to: String, error: Throwable) {
        failureCount.increment()
        
        val errorType = error::class.simpleName ?: "Unknown"
        errorCounts.computeIfAbsent(errorType) { LongAdder() }.increment()
    }
    
    override fun getMetrics(): MetricsSnapshot {
        val total = totalCount.sum()
        val avgTime = if (total > 0) {
            (totalTime.get() / total).nanoseconds
        } else {
            Duration.ZERO
        }
        
        // 가장 많이 사용된 변환 Top 10
        val mostUsed = conversionCounts.entries
            .sortedByDescending { it.value.sum() }
            .take(10)
            .map { (key, count) ->
                val parts = key.split("->")
                val timeNanos = conversionTimes[key]?.get() ?: 0
                val cnt = count.sum()
                val avgTimeForPair = if (cnt > 0) {
                    (timeNanos / cnt).nanoseconds
                } else {
                    Duration.ZERO
                }
                
                ConversionPair(
                    from = parts.getOrNull(0) ?: "",
                    to = parts.getOrNull(1) ?: "",
                    count = cnt,
                    averageTime = avgTimeForPair
                )
            }
        
        return MetricsSnapshot(
            totalConversions = total,
            successfulConversions = successCount.sum(),
            failedConversions = failureCount.sum(),
            averageConversionTime = avgTime,
            mostUsedConversions = mostUsed,
            errorsByType = errorCounts.mapValues { it.value.sum() },
            conversionsByCategory = categoryCounts.mapValues { it.value.sum() }
        )
    }
    
    override fun reset() {
        conversionCounts.clear()
        conversionTimes.clear()
        errorCounts.clear()
        categoryCounts.clear()
        totalCount.reset()
        successCount.reset()
        failureCount.reset()
        totalTime.set(0)
    }
}

/**
 * 전역 메트릭 수집기
 */
public object GlobalMetrics {
    private val metrics: ConversionMetrics = DefaultConversionMetrics()
    private val timeSource = TimeSource.Monotonic
    
    /**
     * 변환 작업을 측정하며 실행
     */
    @JvmStatic
    public fun <T> measureConversion(
        from: String,
        to: String,
        block: () -> T
    ): T {
        val start = timeSource.markNow()
        return try {
            val result = block()
            val duration = start.elapsedNow()
            metrics.recordConversion(from, to, duration, true)
            result
        } catch (e: Exception) {
            val duration = start.elapsedNow()
            metrics.recordConversion(from, to, duration, false)
            metrics.recordError(from, to, e)
            throw e
        }
    }
    
    /**
     * 현재 메트릭 조회
     */
    @JvmStatic
    public fun getSnapshot(): MetricsSnapshot {
        return metrics.getMetrics()
    }
    
    /**
     * 메트릭 초기화
     */
    @JvmStatic
    public fun reset() {
        metrics.reset()
    }
    
    /**
     * 메트릭을 문자열로 포맷
     */
    @JvmStatic
    public fun formatMetrics(): String {
        val snapshot = getSnapshot()
        return buildString {
            appendLine("=== Conversion Metrics ===")
            appendLine("Total conversions: ${snapshot.totalConversions}")
            appendLine("Successful: ${snapshot.successfulConversions}")
            appendLine("Failed: ${snapshot.failedConversions}")
            appendLine("Average time: ${snapshot.averageConversionTime}")
            appendLine()
            appendLine("Top 10 Most Used Conversions:")
            snapshot.mostUsedConversions.forEachIndexed { index, pair ->
                appendLine("  ${index + 1}. ${pair.from} -> ${pair.to}: ${pair.count} times (avg: ${pair.averageTime})")
            }
            if (snapshot.errorsByType.isNotEmpty()) {
                appendLine()
                appendLine("Errors by Type:")
                snapshot.errorsByType.forEach { (type, count) ->
                    appendLine("  $type: $count")
                }
            }
            appendLine()
            appendLine("Conversions by Category:")
            snapshot.conversionsByCategory.forEach { (category, count) ->
                appendLine("  $category: $count")
            }
        }
    }
}

/**
 * 메트릭 리포터 인터페이스
 */
public interface MetricsReporter {
    fun report(snapshot: MetricsSnapshot)
}

/**
 * 콘솔 메트릭 리포터
 */
public class ConsoleMetricsReporter : MetricsReporter {
    override fun report(snapshot: MetricsSnapshot) {
        println(GlobalMetrics.formatMetrics())
    }
}
