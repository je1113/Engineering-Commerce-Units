package io.ecu

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

/**
 * 압력 단위 변환 테스트
 */
class PressureTest {
    
    @Test
    fun `should parse pressure from string`() {
        val pressure1 = Pressure.parse("100Pa")
        assertEquals(100.0, pressure1.pascals, 0.001)
        
        val pressure2 = Pressure.parse("1 bar")
        assertEquals(100000.0, pressure2.pascals, 0.001)
        
        val pressure3 = Pressure.parse("14.7psi")
        assertEquals(101352.972, pressure3.pascals, 1.0)
    }
    
    @Test
    fun `should convert between pressure units`() {
        val pressure = Pressure.parse("1bar")
        
        assertEquals(100000.0, pressure.to("Pa").value, 0.001)
        assertEquals(100.0, pressure.to("kPa").value, 0.001)
        assertEquals(14.504, pressure.to("psi").value, 0.001)
        assertEquals(0.987, pressure.to("atm").value, 0.001)
        assertEquals(750.06, pressure.to("mmHg").value, 0.01)
    }
    
    @Test
    fun `should perform pressure arithmetic`() {
        val p1 = Pressure.parse("50kPa")
        val p2 = Pressure.parse("30kPa")
        
        val sum = p1 + p2
        assertEquals(80.0, sum.kilopascals, 0.001)
        
        val diff = p1 - p2
        assertEquals(20.0, diff.kilopascals, 0.001)
        
        val scaled = p1 * 2.0
        assertEquals(100.0, scaled.kilopascals, 0.001)
        
        val divided = p1 / 2.0
        assertEquals(25.0, divided.kilopascals, 0.001)
    }
    
    @Test
    fun `should compare pressures`() {
        val p1 = Pressure.parse("100kPa")
        val p2 = Pressure.parse("1bar")
        val p3 = Pressure.parse("200kPa")
        
        assertTrue(p2 == p1)  // 100kPa = 1bar
        assertTrue(p3 > p1)
        assertTrue(p1 < p3)
    }
    
    @Test
    fun `should check atmospheric conditions`() {
        val lowPressure = Pressure.parse("90kPa")
        val standardPressure = Pressure.parse("101.325kPa")
        val highPressure = Pressure.parse("110kPa")
        
        assertFalse(lowPressure.isAboveAtmospheric())
        assertFalse(standardPressure.isAboveAtmospheric()) // Exactly at atmospheric
        assertTrue(highPressure.isAboveAtmospheric())
    }
    
    @Test
    fun `should handle gauge and absolute pressure`() {
        val absolute = Pressure.parse("200kPa")
        val gauge = absolute.toGauge()
        
        assertEquals(98.675, gauge.kilopascals, 0.001) // 200 - 101.325
        
        val backToAbsolute = gauge.toAbsolute()
        assertEquals(200.0, backToAbsolute.kilopascals, 0.001)
    }
    
    @Test
    fun `should handle precision and rounding`() {
        val pressure = Pressure.parse("14.7264psi")
            .withPrecision(2)
            .withRounding(RoundingMode.HALF_UP)
        
        assertEquals("14.73 psi", pressure.format())
        
        val pressure2 = Pressure.parse("14.7264psi")
            .withPrecision(1)
            .withRounding(RoundingMode.DOWN)
        
        assertEquals("14.7 psi", pressure2.format())
    }
    
    @Test
    fun `should detect vacuum conditions`() {
        val vacuum = Pressure.parse("-10kPa")
        assertTrue(vacuum.isVacuum())
        
        val positive = Pressure.parse("10kPa")
        assertFalse(positive.isVacuum())
    }
    
    @Test
    fun `should convert industrial pressure values`() {
        // Tire pressure
        val tirePressure = Pressure.parse("32psi")
        assertEquals(220.6, tirePressure.kilopascals, 0.1)
        
        // Hydraulic system
        val hydraulic = Pressure.parse("3000psi")
        assertEquals(206.8, hydraulic.bars, 0.1)
        
        // Vacuum pump
        val vacuum = Pressure.parse("0.1mmHg")
        assertEquals(13.33, vacuum.pascals, 0.01)
    }
    
    @Test
    fun `should handle scientific notation`() {
        val pressure = Pressure.parse("1e5Pa")
        assertEquals(100000.0, pressure.pascals)
        assertEquals(1.0, pressure.bars)
        
        val smallPressure = Pressure.parse("1e-3bar")
        assertEquals(100.0, smallPressure.pascals)
    }
    
    @Test
    fun `should throw exception for invalid units`() {
        assertFailsWith<IllegalArgumentException> {
            Pressure.parse("100m") // Not a pressure unit
        }
        
        assertFailsWith<IllegalArgumentException> {
            Pressure.parse("invalidformat")
        }
    }
    
    @Test
    fun `should create pressure using factory methods`() {
        val p1 = Pressure.pascals(1000.0)
        assertEquals(1000.0, p1.pascals)
        
        val p2 = Pressure.kilopascals(1.0)
        assertEquals(1000.0, p2.pascals)
        
        val p3 = Pressure.bars(0.01)
        assertEquals(1000.0, p3.pascals)
        
        val p4 = Pressure.psi(0.145)
        assertEquals(1000.0, p4.pascals, 1.0)
        
        val p5 = Pressure.atmospheres(0.00987)
        assertEquals(1000.0, p5.pascals, 1.0)
    }
    
    @Test
    fun `should handle edge cases`() {
        val zero = Pressure.parse("0Pa")
        assertEquals(0.0, zero.pascals)
        assertFalse(zero.isVacuum())
        assertFalse(zero.isAboveAtmospheric())
        
        val veryHigh = Pressure.parse("1e9Pa")
        assertEquals(10000.0, veryHigh.bars)
        assertTrue(veryHigh.isAboveAtmospheric())
        
        val veryLow = Pressure.parse("1e-6bar")
        assertEquals(0.1, veryLow.pascals, 0.00001)
    }
}
