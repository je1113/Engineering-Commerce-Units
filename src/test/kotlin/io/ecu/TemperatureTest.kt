package io.ecu

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class TemperatureTest {
    
    @Test
    fun `should parse temperature from string correctly`() {
        val temp = Temperature.parse("25°C")
        assertEquals(25.0, temp.celsius, 0.001)
        assertEquals("°C", temp.symbol)
        assertEquals("Celsius", temp.displayName)
    }
    
    @Test
    fun `should convert between temperature units correctly`() {
        // 물의 어는점 테스트
        val freezing = Temperature.parse("0°C")
        
        val kelvin = freezing.to("K")
        assertEquals(273.15, kelvin.value, 0.001)
        
        val fahrenheit = freezing.to("°F")
        assertEquals(32.0, fahrenheit.value, 0.001)
        
        // 물의 끓는점 테스트
        val boiling = Temperature.parse("100°C")
        val boilingF = boiling.to("°F")
        assertEquals(212.0, boilingF.value, 0.001)
        
        val boilingK = boiling.to("K")
        assertEquals(373.15, boilingK.value, 0.001)
    }
    
    @Test
    fun `should handle different input formats`() {
        val temp1 = Temperature.parse("25°C")
        val temp2 = Temperature.parse("25C")
        val temp3 = Temperature.parse("25 celsius")
        
        assertEquals(temp1.celsius, temp2.celsius, 0.001)
        assertEquals(temp1.celsius, temp3.celsius, 0.001)
    }
    
    @Test
    fun `should convert fahrenheit correctly`() {
        val roomTemp = Temperature.parse("77°F")
        val celsius = roomTemp.to("°C")
        assertEquals(25.0, celsius.value, 0.001)
        
        val kelvin = roomTemp.to("K")
        assertEquals(298.15, kelvin.value, 0.001)
    }
    
    @Test
    fun `should handle negative temperatures`() {
        val cold = Temperature.parse("-40°C")
        val fahrenheit = cold.to("°F")
        assertEquals(-40.0, fahrenheit.value, 0.001) // -40°C = -40°F
        
        val kelvin = cold.to("K")
        assertEquals(233.15, kelvin.value, 0.001)
    }
    
    @Test
    fun `should calculate temperature differences`() {
        val temp1 = Temperature.parse("25°C")
        val temp2 = Temperature.parse("20°C")
        
        val diff = temp1 - temp2
        assertEquals(5.0, diff, 0.001) // 5K difference
    }
    
    @Test
    fun `should compare temperatures correctly`() {
        val temp1 = Temperature.parse("25°C")
        val temp2 = Temperature.parse("77°F") // same as 25°C
        val temp3 = Temperature.parse("20°C")
        
        assertEquals(0, temp1.compareTo(temp2))
        assertTrue(temp1 > temp3)
        assertTrue(temp3 < temp1)
    }
    
    @Test
    fun `should check absolute zero`() {
        val absoluteZero = Temperature.parse("0K")
        assertTrue(absoluteZero.isAboveAbsoluteZero())
        
        val belowZero = Temperature.parse("-1K")
        assertFalse(belowZero.isAboveAbsoluteZero())
    }
    
    @Test
    fun `should identify freezing and boiling points`() {
        val freezing = Temperature.parse("0°C")
        assertTrue(freezing.isFreezingPoint())
        
        val boiling = Temperature.parse("100°C")
        assertTrue(boiling.isBoilingPoint())
        
        val room = Temperature.parse("25°C")
        assertFalse(room.isFreezingPoint())
        assertFalse(room.isBoilingPoint())
    }
    
    @Test
    fun `should categorize temperatures`() {
        assertEquals(TemperatureCategory.EXTREME_COLD, Temperature.parse("-50°C").getTemperatureCategory())
        assertEquals(TemperatureCategory.FREEZING, Temperature.parse("-5°C").getTemperatureCategory())
        assertEquals(TemperatureCategory.COLD, Temperature.parse("5°C").getTemperatureCategory())
        assertEquals(TemperatureCategory.COOL, Temperature.parse("15°C").getTemperatureCategory())
        assertEquals(TemperatureCategory.WARM, Temperature.parse("25°C").getTemperatureCategory())
        assertEquals(TemperatureCategory.HOT, Temperature.parse("35°C").getTemperatureCategory())
        assertEquals(TemperatureCategory.EXTREME_HOT, Temperature.parse("45°C").getTemperatureCategory())
    }
    
    @Test
    fun `should work with ECU fluent API`() {
        val temp = ECU.temperature("25°C").to("°F").withPrecision(1)
        assertEquals("77.0°F", temp.format())
    }
    
    @Test
    fun `should handle batch conversions`() {
        val inputs = listOf("0°C", "32°F", "273K")
        val converted = ECU.Batch.convertTemperatures(inputs, "°C")
        
        assertEquals(3, converted.size)
        assertEquals(0.0, converted[0].celsius, 0.001)
        assertEquals(0.0, converted[1].celsius, 0.001) // 32°F = 0°C
        assertEquals(-0.15, converted[2].celsius, 0.01) // 273K ≈ -0.15°C
    }
    
    @Test
    fun `should format temperature correctly`() {
        val temp = Temperature.parse("25.5°C").withPrecision(1)
        assertEquals("25.5°C", temp.format())
        
        // Note: No space between number and temperature symbol
        val fahrenheit = temp.to("°F")
        assertEquals("77.9°F", fahrenheit.format())
    }
    
    @Test
    fun `should handle scientific notation`() {
        val temp = Temperature.parse("2.5e2K") // 250K
        assertEquals(250.0, temp.kelvin, 0.001)
        assertEquals(-23.15, temp.celsius, 0.01)
    }
    
    @Test
    fun `should suggest better temperature units`() {
        val suggestion = ECU.Auto.suggest("250K")
        assertTrue(suggestion.hasSuggestion())
        assertTrue(suggestion.suggested!!.contains("°C"))
    }
}
