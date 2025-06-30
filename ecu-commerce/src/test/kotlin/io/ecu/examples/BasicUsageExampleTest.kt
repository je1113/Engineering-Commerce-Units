package io.ecu.examples

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class BasicUsageExampleTest {
    
    @Test
    fun `should run basic conversions without errors`() {
        val result = BasicUsageExample.basicConversions()
        
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("길이"))
        assertTrue(result.contains("무게"))
        assertTrue(result.contains("부피"))
        assertTrue(result.contains("온도"))
        assertTrue(result.contains("면적"))
        
        println("기본 변환 결과:")
        println(result)
    }
    
    @Test
    fun `should demonstrate precision control`() {
        val result = BasicUsageExample.precisionControl()
        
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("기본"))
        assertTrue(result.contains("2자리"))
        assertTrue(result.contains("반올림"))
        
        println("\n정밀도 제어 결과:")
        println(result)
    }
    
    @Test
    fun `should perform math operations correctly`() {
        val result = BasicUsageExample.mathOperations()
        
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("덧셈"))
        assertTrue(result.contains("뺄셈"))
        assertTrue(result.contains("곱셈"))
        assertTrue(result.contains("나눗셈"))
        assertTrue(result.contains("비교"))
        
        println("\n수학 연산 결과:")
        println(result)
    }
    
    @Test
    fun `should handle batch conversions`() {
        val result = BasicUsageExample.batchConversions()
        
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("배치"))
        
        println("\n배치 변환 결과:")
        println(result)
    }
    
    @Test
    fun `should convert cooking recipe`() {
        val result = BasicUsageExample.cookingRecipe()
        
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("레시피"))
        
        println("\n요리 레시피 변환 결과:")
        println(result)
    }
    
    @Test
    fun `should demonstrate special features`() {
        val result = BasicUsageExample.specialFeatures()
        
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("범주"))
        
        println("\n특별 기능 결과:")
        println(result)
    }
    
    @Test
    fun `should run all examples successfully`() {
        val result = BasicUsageExample.runAllExamples()
        
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertTrue(result.length > 500) // 전체 예제의 최소 길이
        
        println("\n=== 전체 예제 실행 결과 ===")
        println(result)
    }
}
