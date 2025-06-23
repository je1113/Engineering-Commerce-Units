package io.ecu

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class SpeedTest {
    
    companion object {
        private const val DELTA = 1e-10 // 부동소수점 비교를 위한 오차 허용 범위
    }
    
    @Test
    fun `parse speed from string correctly`() {
        val speed1 = Speed.parse("100km/h")
        assertEquals("km/h", speed1.symbol)
        assertEquals(100.0, speed1.value, DELTA)
        
        val speed2 = Speed.parse("25 mph")
        assertEquals("mph", speed2.symbol)
        assertEquals(25.0, speed2.value, DELTA)
        
        val speed3 = Speed.parse("10m/s")
        assertEquals("m/s", speed3.symbol)
        assertEquals(10.0, speed3.value, DELTA)
    }
    
    @Test
    fun `create speed with factory methods`() {
        val speed1 = Speed.metersPerSecond(10.0)
        assertEquals(10.0, speed1.metersPerSecond, DELTA)
        
        val speed2 = Speed.kilometersPerHour(36.0)
        assertEquals(36.0, speed2.kilometersPerHour, DELTA)
        assertEquals(10.0, speed2.metersPerSecond, DELTA) // 36 km/h = 10 m/s
        
        val speed3 = Speed.milesPerHour(60.0)
        assertEquals(60.0, speed3.milesPerHour, DELTA)
        
        val speed4 = Speed.knots(10.0)
        assertEquals(10.0, speed4.knots, DELTA)
        
        val speed5 = Speed.mach(1.0)
        assertEquals(1.0, speed5.mach, DELTA)
        assertEquals(343.0, speed5.metersPerSecond, DELTA)
    }
    
    @Test
    fun `convert between different speed units`() {
        val speed = Speed.metersPerSecond(10.0)
        
        // m/s to km/h: 10 m/s = 36 km/h
        val kmh = speed.to("km/h")
        assertEquals(36.0, kmh.value, DELTA)
        assertEquals("km/h", kmh.symbol)
        
        // m/s to mph: 10 m/s ≈ 22.369 mph
        val mph = speed.to("mph")
        assertEquals(22.369362920544025, mph.value, DELTA)
        
        // m/s to knots: 10 m/s ≈ 19.438 knots
        val knots = speed.to("kn")
        assertEquals(19.438444924406046, knots.value, DELTA)
        
        // m/s to ft/s: 10 m/s ≈ 32.808 ft/s
        val fts = speed.to("ft/s")
        assertEquals(32.80839895013123, fts.value, DELTA)
    }
    
    @Test
    fun `property getters return correct values`() {
        val speed = Speed.kilometersPerHour(72.0) // 72 km/h = 20 m/s
        
        assertEquals(20.0, speed.metersPerSecond, DELTA)
        assertEquals(72.0, speed.kilometersPerHour, DELTA)
        assertEquals(44.738725841088056, speed.milesPerHour, DELTA)
        assertEquals(65.61679790026247, speed.feetPerSecond, DELTA)
        assertEquals(38.876889848812095, speed.knots, DELTA)
        assertEquals(0.05831903945, speed.mach, 1e-8)
        assertEquals(2000.0, speed.centimetersPerSecond, DELTA)
    }
    
    @Test
    fun `arithmetic operations work correctly`() {
        val speed1 = Speed.metersPerSecond(10.0)
        val speed2 = Speed.metersPerSecond(5.0)
        
        // Addition
        val sum = speed1 + speed2
        assertEquals(15.0, sum.metersPerSecond, DELTA)
        assertEquals("m/s", sum.symbol)
        
        // Subtraction
        val diff = speed1 - speed2
        assertEquals(5.0, diff.metersPerSecond, DELTA)
        
        // Multiplication
        val doubled = speed1 * 2.0
        assertEquals(20.0, doubled.metersPerSecond, DELTA)
        
        // Division
        val halved = speed1 / 2.0
        assertEquals(5.0, halved.metersPerSecond, DELTA)
    }
    
    @Test
    fun `division by zero throws exception`() {
        val speed = Speed.metersPerSecond(10.0)
        assertThrows<IllegalArgumentException> {
            speed / 0.0
        }
    }
    
    @Test
    fun `comparison operations work correctly`() {
        val speed1 = Speed.metersPerSecond(10.0)
        val speed2 = Speed.kilometersPerHour(36.0) // Same as 10 m/s
        val speed3 = Speed.metersPerSecond(15.0)
        
        assertTrue(speed1 >= speed2)
        assertTrue(speed1 <= speed2)
        assertEquals(0, speed1.compareTo(speed2))
        
        assertTrue(speed3 > speed1)
        assertTrue(speed1 < speed3)
    }
    
    @Test
    fun `precision and rounding work correctly`() {
        val speed = Speed.metersPerSecond(10.123456789)
        
        val precise2 = speed.withPrecision(2)
        assertEquals("10.12 m/s", precise2.format())
        
        val precise0 = speed.withPrecision(0)
        assertEquals("10 m/s", precise0.format())
        
        val withRounding = speed.withRounding(RoundingMode.UP).withPrecision(2)
        assertEquals("10.13 m/s", withRounding.format())
    }
    
    @Test
    fun `speed category classification works correctly`() {
        assertEquals(SpeedCategory.VERY_SLOW, Speed.metersPerSecond(0.5).getSpeedCategory())
        assertEquals(SpeedCategory.SLOW, Speed.metersPerSecond(5.0).getSpeedCategory())
        assertEquals(SpeedCategory.MODERATE, Speed.metersPerSecond(25.0).getSpeedCategory())
        assertEquals(SpeedCategory.FAST, Speed.metersPerSecond(80.0).getSpeedCategory())
        assertEquals(SpeedCategory.VERY_FAST, Speed.metersPerSecond(200.0).getSpeedCategory())
        assertEquals(SpeedCategory.SUPERSONIC, Speed.metersPerSecond(400.0).getSpeedCategory())
    }
    
    @Test
    fun `distance calculation works correctly`() {
        val speed = Speed.metersPerSecond(10.0)
        
        // 10 m/s for 5 seconds = 50 meters
        assertEquals(50.0, speed.distanceInTime(5.0), DELTA)
        
        // Negative time should throw exception
        assertThrows<IllegalArgumentException> {
            speed.distanceInTime(-1.0)
        }
    }
    
    @Test
    fun `time calculation works correctly`() {
        val speed = Speed.metersPerSecond(10.0)
        
        // 100 meters at 10 m/s = 10 seconds
        assertEquals(10.0, speed.timeForDistance(100.0), DELTA)
        
        // Negative distance should throw exception
        assertThrows<IllegalArgumentException> {
            speed.timeForDistance(-1.0)
        }
        
        // Zero speed should throw exception
        val zeroSpeed = Speed.metersPerSecond(0.0)
        assertThrows<IllegalArgumentException> {
            zeroSpeed.timeForDistance(100.0)
        }
    }
    
    @Test
    fun `kinetic energy calculation works correctly`() {
        val speed = Speed.metersPerSecond(10.0)
        val mass = 2.0 // kg
        
        // KE = 0.5 * m * v² = 0.5 * 2 * 10² = 100 J
        assertEquals(100.0, speed.kineticEnergy(mass), DELTA)
        
        // Negative mass should throw exception
        assertThrows<IllegalArgumentException> {
            speed.kineticEnergy(-1.0)
        }
        
        // Zero mass should throw exception
        assertThrows<IllegalArgumentException> {
            speed.kineticEnergy(0.0)
        }
    }
    
    @Test
    fun `suggest best unit works correctly`() {
        // Very slow speed -> cm/s
        val slowSpeed = Speed.metersPerSecond(0.05)
        val suggestedSlow = slowSpeed.suggestBestUnit()
        assertEquals("cm/s", suggestedSlow.symbol)
        
        // Moderate speed -> km/h
        val moderateSpeed = Speed.metersPerSecond(20.0)
        val suggestedModerate = moderateSpeed.suggestBestUnit()
        assertEquals("km/h", suggestedModerate.symbol)
        
        // Fast speed -> mph
        val fastSpeed = Speed.metersPerSecond(80.0)
        val suggestedFast = fastSpeed.suggestBestUnit()
        assertEquals("mph", suggestedFast.symbol)
        
        // Supersonic speed -> Mach
        val supersonicSpeed = Speed.metersPerSecond(400.0)
        val suggestedSupersonic = supersonicSpeed.suggestBestUnit()
        assertEquals("Ma", suggestedSupersonic.symbol)
        
        // Default case -> m/s
        val defaultSpeed = Speed.metersPerSecond(150.0)
        val suggestedDefault = defaultSpeed.suggestBestUnit()
        assertEquals("m/s", suggestedDefault.symbol)
    }
    
    @Test
    fun `validation methods work correctly`() {
        val validSpeed = Speed.metersPerSecond(10.0)
        assertTrue(validSpeed.isValid())
        assertTrue(validSpeed.isWithinRange(5.0..15.0))
        assertFalse(validSpeed.isWithinRange(20.0..30.0))
        
        val infiniteSpeed = Speed.metersPerSecond(Double.POSITIVE_INFINITY)
        assertFalse(infiniteSpeed.isValid())
        
        val nanSpeed = Speed.metersPerSecond(Double.NaN)
        assertFalse(nanSpeed.isValid())
    }
    
    @Test
    fun `equals and hashCode work correctly`() {
        val speed1 = Speed.metersPerSecond(10.0)
        val speed2 = Speed.kilometersPerHour(36.0) // Same as 10 m/s
        val speed3 = Speed.metersPerSecond(15.0)
        
        assertEquals(speed1, speed2)
        assertEquals(speed1.hashCode(), speed2.hashCode())
        
        assertFalse(speed1.equals(speed3))
        assertFalse(speed1.hashCode() == speed3.hashCode())
    }
    
    @Test
    fun `toString returns formatted string`() {
        val speed = Speed.metersPerSecond(10.5)
        assertEquals("10.5 m/s", speed.toString())
        
        val preciseSpeed = Speed.metersPerSecond(10.123456).withPrecision(2)
        assertEquals("10.12 m/s", preciseSpeed.toString())
    }
    
    @Test
    fun `parse invalid input throws exception`() {
        assertThrows<IllegalArgumentException> {
            Speed.parse("invalid")
        }
        
        assertThrows<IllegalArgumentException> {
            Speed.parse("10")
        }
        
        assertThrows<IllegalArgumentException> {
            Speed.parse("ten km/h")
        }
        
        assertThrows<IllegalArgumentException> {
            Speed.parse("10 xyz")
        }
    }
    
    @Test
    fun `convert to invalid unit throws exception`() {
        val speed = Speed.metersPerSecond(10.0)
        
        assertThrows<IllegalArgumentException> {
            speed.to("kg") // Wrong category
        }
        
        assertThrows<IllegalArgumentException> {
            speed.to("xyz") // Unknown unit
        }
    }
    
    @Test
    fun `complex conversion scenarios`() {
        // Test chain conversions
        val original = Speed.kilometersPerHour(100.0)
        val converted = original.to("mph").to("kn").to("m/s")
        
        // Should be approximately equal to original base value
        assertEquals(original.metersPerSecond, converted.metersPerSecond, 1e-6)
        
        // Test aviation units
        val aviationSpeed = Speed.knots(500.0)
        val machNumber = aviationSpeed.to("Ma")
        assertTrue(machNumber.value < 1.0) // 500 knots is subsonic
        
        // Test very small speeds
        val crawlSpeed = Speed.centimetersPerSecond(1.0)
        assertEquals(0.01, crawlSpeed.metersPerSecond, DELTA)
        assertEquals(0.036, crawlSpeed.kilometersPerHour, DELTA)
    }
    
    @Test
    fun `edge cases and boundary conditions`() {
        // Zero speed
        val zeroSpeed = Speed.metersPerSecond(0.0)
        assertEquals(0.0, zeroSpeed.kilometersPerHour, DELTA)
        assertEquals(0.0, zeroSpeed.milesPerHour, DELTA)
        
        // Very large speed
        val lightSpeed = Speed.metersPerSecond(299_792_458.0) // Speed of light
        val lightSpeedMach = lightSpeed.mach
        assertTrue(lightSpeedMach > 1_000_000) // Much faster than Mach 1
        
        // Negative speed (should be valid for relative calculations)
        val negativeSpeed = Speed.metersPerSecond(-10.0)
        assertTrue(negativeSpeed.isValid())
        assertEquals(-36.0, negativeSpeed.kilometersPerHour, DELTA)
    }
    
    @Test
    fun `precision edge cases`() {
        val speed = Speed.metersPerSecond(1.0/3.0) // 0.333...
        
        // Test different precision levels
        val p0 = speed.withPrecision(0)
        assertTrue(p0.format().matches(Regex("\\d+ m/s")))
        
        val p1 = speed.withPrecision(1)
        assertTrue(p1.format().matches(Regex("\\d+\\.\\d m/s")))
        
        val p5 = speed.withPrecision(5)
        assertTrue(p5.format().matches(Regex("\\d+\\.\\d{5} m/s")))
        
        // Test invalid precision
        assertThrows<IllegalArgumentException> {
            speed.withPrecision(-1)
        }
    }
    
    @Test
    fun `rounding modes work correctly`() {
        val speed = Speed.metersPerSecond(1.235)
        
        val halfUp = speed.withRounding(RoundingMode.HALF_UP).withPrecision(2)
        assertEquals("1.24 m/s", halfUp.format())
        
        val halfDown = speed.withRounding(RoundingMode.HALF_DOWN).withPrecision(2)
        assertEquals("1.23 m/s", halfDown.format())
        
        val up = speed.withRounding(RoundingMode.UP).withPrecision(2)
        assertEquals("1.24 m/s", up.format())
        
        val down = speed.withRounding(RoundingMode.DOWN).withPrecision(2)
        assertEquals("1.23 m/s", down.format())
    }
    
    @Test
    fun `unit aliases work correctly`() {
        // Test various aliases for km/h
        val speed1 = Speed.parse("100kmh")
        val speed2 = Speed.parse("100kph")
        val speed3 = Speed.parse("100km/h")
        
        assertEquals(speed1.metersPerSecond, speed2.metersPerSecond, DELTA)
        assertEquals(speed1.metersPerSecond, speed3.metersPerSecond, DELTA)
        
        // Test knot aliases
        val knot1 = Speed.parse("10kn")
        val knot2 = Speed.parse("10knot")
        val knot3 = Speed.parse("10knots")
        
        assertEquals(knot1.metersPerSecond, knot2.metersPerSecond, DELTA)
        assertEquals(knot1.metersPerSecond, knot3.metersPerSecond, DELTA)
    }
    
    @Test
    fun `realistic usage scenarios`() {
        // Walking speed
        val walkingSpeed = Speed.kilometersPerHour(5.0)
        assertEquals(SpeedCategory.SLOW, walkingSpeed.getSpeedCategory())
        assertTrue(walkingSpeed.metersPerSecond < 2.0)
        
        // Car highway speed
        val highwaySpeed = Speed.milesPerHour(70.0)
        assertEquals(SpeedCategory.FAST, highwaySpeed.getSpeedCategory())
        assertTrue(highwaySpeed.kilometersPerHour > 100.0)
        
        // Aircraft cruising speed
        val aircraftSpeed = Speed.knots(450.0)
        assertEquals(SpeedCategory.VERY_FAST, aircraftSpeed.getSpeedCategory())
        assertTrue(aircraftSpeed.mach < 1.0) // Subsonic
        
        // Sound barrier
        val soundSpeed = Speed.mach(1.0)
        assertEquals(SpeedCategory.SUPERSONIC, soundSpeed.getSpeedCategory())
        assertEquals(343.0, soundSpeed.metersPerSecond, DELTA)
    }
}
