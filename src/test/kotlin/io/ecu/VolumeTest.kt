package io.ecu

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class VolumeTest {
    
    @Test
    fun `should parse volume from string correctly`() {
        val volume = Volume.parse("5l")
        assertEquals(5.0, volume.liters, 0.001)
        assertEquals("l", volume.symbol)
        assertEquals("liter", volume.displayName)
    }
    
    @Test
    fun `should convert between metric volume units`() {
        val volume = Volume.parse("1l")
        
        val ml = volume.to("ml")
        assertEquals(1000.0, ml.value, 0.001)
        assertEquals("ml", ml.symbol)
        
        val m3 = volume.to("m³")
        assertEquals(0.001, m3.value, 0.000001)
        assertEquals("m³", m3.symbol)
    }
    
    @Test
    fun `should convert between imperial volume units`() {
        val volume = Volume.parse("1gal")
        
        val quarts = volume.to("qt")
        assertEquals(4.0, quarts.value, 0.01)
        assertEquals("qt", quarts.symbol)
        
        val pints = volume.to("pt")
        assertEquals(8.0, pints.value, 0.01)
        assertEquals("pt", pints.symbol)
    }
    
    @Test
    fun `should convert between metric and imperial volumes`() {
        val liter = Volume.parse("1l")
        
        val gallons = liter.to("gal")
        assertEquals(0.264172, gallons.value, 0.001)
        
        val flOz = liter.to("fl oz")
        assertEquals(33.814, flOz.value, 0.01)
    }
    
    @Test
    fun `should support arithmetic operations`() {
        val vol1 = Volume.parse("5l")
        val vol2 = Volume.parse("3l")
        
        val sum = vol1 + vol2
        assertEquals(8.0, sum.liters, 0.001)
        
        val diff = vol1 - vol2
        assertEquals(2.0, diff.liters, 0.001)
        
        val doubled = vol1 * 2.0
        assertEquals(10.0, doubled.liters, 0.001)
        
        val halved = vol1 / 2.0
        assertEquals(2.5, halved.liters, 0.001)
    }
    
    @Test
    fun `should compare volumes correctly`() {
        val vol1 = Volume.parse("5l")
        val vol2 = Volume.parse("5000ml")
        val vol3 = Volume.parse("3l")
        
        assertEquals(0, vol1.compareTo(vol2))
        assertTrue(vol1 > vol3)
        assertTrue(vol3 < vol1)
    }
    
    @Test
    fun `should work with ECU fluent API`() {
        val volume = ECU.volume("5l").to("gal").withPrecision(2)
        assertEquals("1.32 gal", volume.format())
    }
    
    @Test
    fun `should handle batch conversions`() {
        val inputs = listOf("1l", "2gal", "500ml")
        val converted = ECU.Batch.convertVolumes(inputs, "l")
        
        assertEquals(3, converted.size)
        assertEquals(1.0, converted[0].liters, 0.001)
        assertEquals(7.571, converted[1].liters, 0.01) // 2 gal ≈ 7.571 l
        assertEquals(0.5, converted[2].liters, 0.001)
    }
    
    @Test
    fun `should suggest better volume units`() {
        val suggestion = ECU.Auto.suggest("0.0005l") // 0.5ml로 더 작은 값
        assertTrue(suggestion.hasSuggestion())
        assertTrue(suggestion.suggested!!.contains("ml"))
    }
}
