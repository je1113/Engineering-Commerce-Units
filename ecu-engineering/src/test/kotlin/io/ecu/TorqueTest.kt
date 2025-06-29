package io.ecu

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class TorqueTest {
    
    @Test
    fun `test torque parsing from string`() {
        // Test various input formats
        val torque1 = Torque.parse("100Nm")
        assertEquals(100.0, torque1.newtonMeters, 0.001)
        
        val torque2 = Torque.parse("50 ft-lb")
        assertEquals(67.79, torque2.newtonMeters, 0.01)
        
        val torque3 = Torque.parse("25.5 in-lb")
        assertEquals(2.881, torque3.newtonMeters, 0.001)
        
        // Test with scientific notation
        val torque4 = Torque.parse("1.5e2 Nm")
        assertEquals(150.0, torque4.newtonMeters, 0.001)
    }
    
    @Test
    fun `test factory methods`() {
        val nm = Torque.newtonMeters(100.0)
        assertEquals(100.0, nm.newtonMeters, 0.001)
        
        val kNm = Torque.kilonewtonMeters(1.5)
        assertEquals(1500.0, kNm.newtonMeters, 0.001)
        
        val ftLb = Torque.footPounds(50.0)
        assertEquals(67.79, ftLb.newtonMeters, 0.01)
        
        val inLb = Torque.inchPounds(100.0)
        assertEquals(11.298, inLb.newtonMeters, 0.001)
        
        val kgfM = Torque.kilogramForceMeters(10.0)
        assertEquals(98.066, kgfM.newtonMeters, 0.001)
    }
    
    @Test
    fun `test unit conversions`() {
        val torque = Torque.newtonMeters(100.0)
        
        // To foot-pounds
        val ftLb = torque.to("ft-lb")
        assertEquals(73.756, ftLb.value, 0.001)
        
        // To inch-pounds
        val inLb = torque.to("in-lb")
        assertEquals(885.075, inLb.value, 0.001)
        
        // To kilonewton-meters
        val kNm = torque.to("kNm")
        assertEquals(0.1, kNm.value, 0.001)
        
        // Chain conversions
        val converted = Torque.parse("100 ft-lb")
            .to("Nm")
            .to("in-lb")
            .to("ft-lb")
        assertEquals(100.0, converted.value, 0.01)
    }
    
    @Test
    fun `test arithmetic operations`() {
        val torque1 = Torque.newtonMeters(50.0)
        val torque2 = Torque.footPounds(50.0)
        
        // Addition
        val sum = torque1 + torque2
        assertEquals(117.79, sum.newtonMeters, 0.01)
        
        // Subtraction
        val diff = torque2 - torque1
        assertEquals(17.79, diff.newtonMeters, 0.01)
        
        // Multiplication
        val doubled = torque1 * 2.0
        assertEquals(100.0, doubled.newtonMeters, 0.001)
        
        // Division
        val halved = torque1 / 2.0
        assertEquals(25.0, halved.newtonMeters, 0.001)
    }
    
    @Test
    fun `test comparison operators`() {
        val small = Torque.newtonMeters(10.0)
        val medium = Torque.footPounds(20.0)
        val large = Torque.newtonMeters(50.0)
        
        assertTrue(small < medium)
        assertTrue(medium < large)
        assertTrue(large > small)
        
        val equal1 = Torque.newtonMeters(100.0)
        val equal2 = Torque.footPounds(73.756)
        assertTrue(kotlin.math.abs(equal1.newtonMeters - equal2.newtonMeters) < 0.01)
    }
    
    @Test
    fun `test power calculations`() {
        val torque = Torque.newtonMeters(100.0)
        
        // Power at RPM
        val powerAt3000RPM = torque.powerAtRPM(3000.0)
        assertEquals(31415.93, powerAt3000RPM, 0.01) // ~31.4 kW
        
        // Power at angular velocity
        val powerAtRad = torque.powerAtAngularVelocity(100.0) // 100 rad/s
        assertEquals(10000.0, powerAtRad, 0.001) // 10 kW
    }
    
    @Test
    fun `test force and lever arm calculations`() {
        val torque = Torque.newtonMeters(50.0)
        
        // Force at distance
        val force = torque.forceAtDistance(0.5) // 0.5m lever arm
        assertEquals(100.0, force, 0.001) // 100 N
        
        // Distance for force
        val distance = torque.distanceForForce(200.0) // 200 N force
        assertEquals(0.25, distance, 0.001) // 0.25 m
    }
    
    @Test
    fun `test work calculations`() {
        val torque = Torque.newtonMeters(100.0)
        
        // Work over angle
        val work = torque.workOverAngle(Math.PI) // Half revolution
        assertEquals(314.159, work, 0.001) // 314.16 J
        
        // Angle for work
        val angle = torque.angleForWork(628.318) // ~2π * 100
        assertEquals(6.283, angle, 0.001) // ~2π radians
    }
    
    @Test
    fun `test torque scale categorization`() {
        assertEquals(TorqueScale.MICRO, Torque.newtonMeters(0.05).getTorqueScale())
        assertEquals(TorqueScale.SMALL, Torque.newtonMeters(0.5).getTorqueScale())
        assertEquals(TorqueScale.HAND_TOOL, Torque.newtonMeters(5.0).getTorqueScale())
        assertEquals(TorqueScale.POWER_TOOL, Torque.newtonMeters(50.0).getTorqueScale())
        assertEquals(TorqueScale.AUTOMOTIVE, Torque.newtonMeters(500.0).getTorqueScale())
        assertEquals(TorqueScale.INDUSTRIAL, Torque.newtonMeters(5000.0).getTorqueScale())
        assertEquals(TorqueScale.HEAVY_MACHINERY, Torque.newtonMeters(50000.0).getTorqueScale())
        assertEquals(TorqueScale.MASSIVE, Torque.newtonMeters(500000.0).getTorqueScale())
    }
    
    @Test
    fun `test best unit suggestions`() {
        assertEquals("mNm", Torque.newtonMeters(0.0005).suggestBestUnit())
        assertEquals("ozf⋅in", Torque.newtonMeters(0.05).suggestBestUnit())
        assertEquals("in-lb", Torque.newtonMeters(0.5).suggestBestUnit())
        assertEquals("Nm", Torque.newtonMeters(50.0).suggestBestUnit())
        assertEquals("ft-lb", Torque.newtonMeters(500.0).suggestBestUnit())
        assertEquals("kNm", Torque.newtonMeters(5000.0).suggestBestUnit())
    }
    
    @Test
    fun `test formatting methods`() {
        val torque = Torque.newtonMeters(135.5817)
        
        // Automotive format
        assertEquals("100.0 ft-lb", torque.toAutomotiveFormat())
        
        // Engineering format
        assertEquals("135.58 N⋅m", torque.toEngineeringFormat())
        
        val largeTorque = Torque.kilonewtonMeters(5.5)
        assertEquals("5.500 kN⋅m", largeTorque.toEngineeringFormat())
    }
    
    @Test
    fun `test precision and rounding`() {
        val torque = Torque.parse("123.456789 Nm")
        
        val precise2 = torque.withPrecision(2)
        assertEquals("123.46 Nm", precise2.format())
        
        val precise0 = torque.withPrecision(0)
        assertEquals("123 Nm", precise0.format())
        
        // Test different rounding modes
        val value = Torque.parse("10.555 Nm")
        
        val halfUp = value.withPrecision(2).withRounding(RoundingMode.HALF_UP)
        assertEquals("10.56 Nm", halfUp.format())
        
        val halfDown = value.withPrecision(2).withRounding(RoundingMode.HALF_DOWN)
        assertEquals("10.55 Nm", halfDown.format())
    }
    
    @Test
    fun `test alias support`() {
        // Test various aliases
        val nm1 = Torque.parse("100 N⋅m")
        val nm2 = Torque.parse("100 N·m")
        val nm3 = Torque.parse("100 newton meter")
        
        assertEquals(nm1.newtonMeters, nm2.newtonMeters, 0.001)
        assertEquals(nm2.newtonMeters, nm3.newtonMeters, 0.001)
        
        val ftlb1 = Torque.parse("50 ft⋅lb")
        val ftlb2 = Torque.parse("50 ft-lbf")
        assertEquals(ftlb1.newtonMeters, ftlb2.newtonMeters, 0.001)
    }
    
    @Test
    fun `test error handling`() {
        // Invalid format
        assertThrows<IllegalArgumentException> {
            Torque.parse("invalid")
        }
        
        // Unknown unit
        assertThrows<IllegalArgumentException> {
            Torque.parse("100 xyz")
        }
        
        // Wrong category
        assertThrows<IllegalArgumentException> {
            Torque.parse("100 m")
        }
        
        // Division by zero
        assertThrows<IllegalArgumentException> {
            Torque.newtonMeters(100.0) / 0.0
        }
        
        // Negative distance in force calculation
        assertThrows<IllegalArgumentException> {
            Torque.newtonMeters(100.0).forceAtDistance(-1.0)
        }
    }
    
    @Test
    fun `test edge cases`() {
        // Very small values
        val tiny = Torque.parse("1e-10 Nm")
        assertTrue(tiny.newtonMeters > 0)
        
        // Very large values
        val huge = Torque.parse("1e10 Nm")
        assertEquals(TorqueScale.MASSIVE, huge.getTorqueScale())
        
        // Zero torque
        val zero = Torque.newtonMeters(0.0)
        assertEquals(0.0, zero.powerAtRPM(1000.0), 0.001)
    }
    
    @Test
    fun `test ECU integration`() {
        // Test through ECU API
        val torque = ECU.torque("250 ft-lb")
        assertEquals(339.0, torque.newtonMeters, 0.1)
        
        // Test batch conversion
        val torques = ECU.Batch.convertTorques(
            listOf("100 Nm", "50 ft-lb", "200 in-lb"),
            "ft-lb"
        )
        assertEquals(3, torques.size)
        assertEquals(73.756, torques[0].value, 0.001)
        assertEquals(50.0, torques[1].value, 0.001)
        assertEquals(16.667, torques[2].value, 0.001)
    }
    
    @Test
    fun `test real-world scenarios`() {
        // Car engine torque
        val engineTorque = Torque.footPounds(350.0) // Typical V8 engine
        val powerAt5000RPM = engineTorque.powerAtRPM(5000.0) / 745.7 // Convert to HP
        assertEquals(333.0, powerAt5000RPM, 1.0) // ~333 HP
        
        // Torque wrench setting
        val wheelNut = Torque.footPounds(100.0)
        val nmValue = wheelNut.to("Nm")
        assertEquals(135.58, nmValue.value, 0.01)
        
        // Electric motor torque
        val motorTorque = Torque.newtonMeters(2.5) // Small servo motor
        val ozIn = motorTorque.to("ozf⋅in")
        assertEquals(354.0, ozIn.value, 1.0)
    }
}