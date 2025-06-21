package io.ecu

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WeightTest {
    
    @Test
    fun `should parse weight from string correctly`() {
        val weight = Weight.parse("5kg")
        assertEquals(5.0, weight.kilograms, 0.001)
        assertEquals("kg", weight.symbol)
        assertEquals("kilogram", weight.displayName)
    }
    
    @Test
    fun `should convert between metric weight units`() {
        val weight = Weight.parse("1kg")
        
        val grams = weight.to("g")
        assertEquals(1000.0, grams.value, 0.001)
        assertEquals("g", grams.symbol)
        
        val mg = weight.to("mg")
        assertEquals(1_000_000.0, mg.value, 0.001)
        assertEquals("mg", mg.symbol)
        
        val tons = weight.to("t")
        assertEquals(0.001, tons.value, 0.000001)
        assertEquals("t", tons.symbol)
    }
    
    @Test
    fun `should convert between imperial weight units`() {
        val weight = Weight.parse("1lb")
        
        val ounces = weight.to("oz")
        assertEquals(16.0, ounces.value, 0.01) // 1 lb = 16 oz
        assertEquals("oz", ounces.symbol)
    }
    
    @Test
    fun `should convert between metric and imperial weights`() {
        val kg = Weight.parse("1kg")
        
        val pounds = kg.to("lb")
        assertEquals(2.20462, pounds.value, 0.001)
        
        val ounces = kg.to("oz")
        assertEquals(35.274, ounces.value, 0.01)
    }
    
    @Test
    fun `should handle precision correctly`() {
        val weight = Weight.parse("1kg").withPrecision(2)
        val pounds = weight.to("lb")
        
        assertEquals("2.20 lb", pounds.format())
    }
    
    @Test
    fun `should support arithmetic operations`() {
        val weight1 = Weight.parse("5kg")
        val weight2 = Weight.parse("3kg")
        
        val sum = weight1 + weight2
        assertEquals(8.0, sum.kilograms, 0.001)
        
        val diff = weight1 - weight2
        assertEquals(2.0, diff.kilograms, 0.001)
        
        val doubled = weight1 * 2.0
        assertEquals(10.0, doubled.kilograms, 0.001)
        
        val halved = weight1 / 2.0
        assertEquals(2.5, halved.kilograms, 0.001)
    }
    
    @Test
    fun `should compare weights correctly`() {
        val weight1 = Weight.parse("5kg")
        val weight2 = Weight.parse("5000g")
        val weight3 = Weight.parse("3kg")
        
        assertEquals(0, weight1.compareTo(weight2))
        assertTrue(weight1 > weight3)
        assertTrue(weight3 < weight1)
    }
    
    @Test
    fun `should work with ECU fluent API`() {
        val weight = ECU.weight("5kg").to("lb").withPrecision(2)
        assertEquals("11.02 lb", weight.format())
    }
    
    @Test
    fun `should handle unit suggestions`() {
        val suggestion = ECU.Auto.suggest("0.5kg")
        assertTrue(suggestion.hasSuggestion())
        assertTrue(suggestion.suggested!!.contains("g"))
    }
    
    @Test
    fun `should handle batch conversions`() {
        val inputs = listOf("1kg", "2lb", "500g")
        val converted = ECU.Batch.convertWeights(inputs, "kg")
        
        assertEquals(3, converted.size)
        assertEquals(1.0, converted[0].kilograms, 0.001)
        assertEquals(0.907, converted[1].kilograms, 0.01) // 2 lb ≈ 0.907 kg
        assertEquals(0.5, converted[2].kilograms, 0.001)
    }
}
