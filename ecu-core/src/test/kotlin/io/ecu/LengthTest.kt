package io.ecu

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LengthTest {
    
    @Test
    fun `should parse length from string correctly`() {
        val length = Length.parse("5m")
        assertEquals(5.0, length.meters, 0.001)
        assertEquals("m", length.symbol)
        assertEquals("meter", length.displayName)
    }
    
    @Test
    fun `should parse length with decimal values`() {
        val length = Length.parse("10.5cm")
        assertEquals(0.105, length.meters, 0.001)
        assertEquals(10.5, length.centimeters, 0.001)
    }
    
    @Test
    fun `should handle spaces in input`() {
        val length = Length.parse("  15.5   ft  ")
        assertEquals(15.5, length.feet, 0.001)
        assertEquals("ft", length.symbol)
    }
    
    @Test
    fun `should convert between metric units correctly`() {
        val length = Length.parse("1m")
        
        val cm = length.to("cm")
        assertEquals(100.0, cm.value, 0.001)
        assertEquals("cm", cm.symbol)
        
        val mm = length.to("mm")
        assertEquals(1000.0, mm.value, 0.001)
        assertEquals("mm", mm.symbol)
        
        val km = length.to("km")
        assertEquals(0.001, km.value, 0.000001)
        assertEquals("km", km.symbol)
    }
    
    @Test
    fun `should convert between imperial units correctly`() {
        val length = Length.parse("1ft")
        
        val inches = length.to("in")
        assertEquals(12.0, inches.value, 0.001)
        assertEquals("in", inches.symbol)
        
        val yards = length.to("yd")
        assertEquals(0.333333, yards.value, 0.001)
        assertEquals("yd", yards.symbol)
    }
    
    @Test
    fun `should convert between metric and imperial units`() {
        val meter = Length.parse("1m")
        
        val feet = meter.to("ft")
        assertEquals(3.28084, feet.value, 0.001)
        
        val inches = meter.to("in")
        assertEquals(39.3701, inches.value, 0.001)
    }
    
    @Test
    fun `should handle precision correctly`() {
        val length = Length.parse("1m").withPrecision(2)
        val feet = length.to("ft")
        
        assertEquals("3.28 ft", feet.format())
    }
    
    @Test
    fun `should support arithmetic operations`() {
        val length1 = Length.parse("5m")
        val length2 = Length.parse("3m")
        
        val sum = length1 + length2
        assertEquals(8.0, sum.meters, 0.001)
        
        val diff = length1 - length2
        assertEquals(2.0, diff.meters, 0.001)
        
        val doubled = length1 * 2.0
        assertEquals(10.0, doubled.meters, 0.001)
        
        val halved = length1 / 2.0
        assertEquals(2.5, halved.meters, 0.001)
    }
    
    @Test
    fun `should compare lengths correctly`() {
        val length1 = Length.parse("5m")
        val length2 = Length.parse("500cm")
        val length3 = Length.parse("3m")
        
        assertEquals(0, length1.compareTo(length2))
        assertTrue(length1 > length3)
        assertTrue(length3 < length1)
    }
    
    @Test
    fun `should throw exception for invalid unit`() {
        assertFailsWith<IllegalArgumentException> {
            Length.parse("5xyz")
        }
    }
    
    @Test
    fun `should throw exception for invalid format`() {
        assertFailsWith<IllegalArgumentException> {
            Length.parse("invalid")
        }
    }
    
    @Test
    fun `should validate unit category`() {
        assertFailsWith<IllegalArgumentException> {
            Length.parse("5kg") // kg는 무게 단위
        }
    }
    
    @Test
    fun `should handle scientific notation`() {
        val length = Length.parse("1.5e-3m")
        assertEquals(0.0015, length.meters, 0.000001)
    }
    
    @Test
    fun `should work with ECU fluent API`() {
        val length = ECU.length("5m").to("ft").withPrecision(2)
        assertEquals("16.40 ft", length.format())
    }

    
    @Test
    fun `should maintain precision through conversions`() {
        val original = Length.parse("1in").withPrecision(4)
        val converted = original.to("cm")
        
        assertEquals("2.5400 cm", converted.format())
    }
}
