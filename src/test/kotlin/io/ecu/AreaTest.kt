package io.ecu

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AreaTest {
    
    @Test
    fun `should parse area from string correctly`() {
        val area = Area.parse("100m²")
        assertEquals(100.0, area.squareMeters, 0.001)
        assertEquals("m²", area.symbol)
        assertEquals("square meter", area.displayName)
    }
    
    @Test
    fun `should convert between metric area units`() {
        val area = Area.parse("1m²")
        
        val cm2 = area.to("cm²")
        assertEquals(10000.0, cm2.value, 0.001)
        assertEquals("cm²", cm2.symbol)
        
        val km2 = area.to("km²")
        assertEquals(0.000001, km2.value, 0.000000001)
        assertEquals("km²", km2.symbol)
        
        val hectares = area.to("ha")
        assertEquals(0.0001, hectares.value, 0.000001)
        assertEquals("ha", hectares.symbol)
    }
    
    @Test
    fun `should convert between imperial area units`() {
        val area = Area.parse("1ft²")
        
        val inches = area.to("in²")
        assertEquals(144.0, inches.value, 0.01) // 1 ft² = 144 in²
        assertEquals("in²", inches.symbol)
        
        val yards = area.to("yd²")
        assertEquals(0.111111, yards.value, 0.001) // 1 ft² ≈ 0.111 yd²
        assertEquals("yd²", yards.symbol)
    }
    
    @Test
    fun `should convert between metric and imperial areas`() {
        val sqm = Area.parse("1m²")
        
        val sqft = sqm.to("ft²")
        assertEquals(10.7639, sqft.value, 0.001)
        
        val sqin = sqm.to("in²")
        assertEquals(1550.0, sqin.value, 1.0)
    }
    
    @Test
    fun `should handle large area units`() {
        val hectare = Area.parse("1ha")
        assertEquals(10000.0, hectare.squareMeters, 0.001)
        
        val acre = Area.parse("1acre")
        assertEquals(4046.86, acre.squareMeters, 0.01)
        
        // Convert hectare to acres
        val acreFromHa = hectare.to("acre")
        assertEquals(2.471, acreFromHa.value, 0.01) // 1 ha ≈ 2.471 acres
    }
    
    @Test
    fun `should support arithmetic operations`() {
        val area1 = Area.parse("50m²")
        val area2 = Area.parse("30m²")
        
        val sum = area1 + area2
        assertEquals(80.0, sum.squareMeters, 0.001)
        
        val diff = area1 - area2
        assertEquals(20.0, diff.squareMeters, 0.001)
        
        val doubled = area1 * 2.0
        assertEquals(100.0, doubled.squareMeters, 0.001)
        
        val halved = area1 / 2.0
        assertEquals(25.0, halved.squareMeters, 0.001)
    }
    
    @Test
    fun `should compare areas correctly`() {
        val area1 = Area.parse("100m²")
        val area2 = Area.parse("1000000cm²") // same as 100m²
        val area3 = Area.parse("50m²")
        
        assertEquals(0, area1.compareTo(area2))
        assertTrue(area1 > area3)
        assertTrue(area3 < area1)
    }
    
    @Test
    fun `should calculate square side length`() {
        val area = Area.parse("100m²")
        val side = area.toSquareSide()
        assertEquals(10.0, side.meters, 0.001) // √100 = 10
    }
    
    @Test
    fun `should calculate circle radius`() {
        val area = Area.parse("314.159m²") // π × 10²
        val radius = area.toCircleRadius()
        assertEquals(10.0, radius.meters, 0.01) // radius ≈ 10m
    }
    
    @Test
    fun `should categorize areas correctly`() {
        assertEquals(AreaCategory.TINY, Area.parse("5m²").getAreaCategory())
        assertEquals(AreaCategory.SMALL, Area.parse("25m²").getAreaCategory())
        assertEquals(AreaCategory.MEDIUM, Area.parse("100m²").getAreaCategory())
        assertEquals(AreaCategory.LARGE, Area.parse("500m²").getAreaCategory())
        assertEquals(AreaCategory.VERY_LARGE, Area.parse("5000m²").getAreaCategory())
        assertEquals(AreaCategory.MASSIVE, Area.parse("50000m²").getAreaCategory())
    }
    
    @Test
    fun `should work with ECU fluent API`() {
        val area = ECU.area("100m²").to("ft²").withPrecision(1)
        assertEquals("1076.4 ft²", area.format())
    }
    
    @Test
    fun `should handle batch conversions`() {
        val inputs = listOf("100m²", "1000ft²", "1acre")
        val converted = ECU.Batch.convertAreas(inputs, "m²")
        
        assertEquals(3, converted.size)
        assertEquals(100.0, converted[0].squareMeters, 0.001)
        assertEquals(92.903, converted[1].squareMeters, 0.01) // 1000 ft² ≈ 92.903 m²
        assertEquals(4046.86, converted[2].squareMeters, 0.01) // 1 acre ≈ 4046.86 m²
    }
    
    @Test
    fun `should suggest better area units`() {
        val smallArea = ECU.Auto.suggest("0.005m²")
        assertTrue(smallArea.hasSuggestion())
        assertTrue(smallArea.suggested!!.contains("cm²"))
        
        val largeArea = ECU.Auto.suggest("50000m²")
        assertTrue(largeArea.hasSuggestion())
        assertTrue(largeArea.suggested!!.contains("ha"))
    }
    
    @Test
    fun `should handle alternative unit formats`() {
        val area1 = Area.parse("100m2") // without superscript
        val area2 = Area.parse("100 square meters")
        
        assertEquals(100.0, area1.squareMeters, 0.001)
        assertEquals(100.0, area2.squareMeters, 0.001)
    }
    
    @Test
    fun `should format with precision`() {
        val area = Area.parse("100.12345m²").withPrecision(2)
        assertEquals("100.12 m²", area.format())
    }
}
