package io.ecu

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class EnergyTest {

    companion object {
        private const val DELTA = 1e-10 // 부동소수점 비교를 위한 오차 허용 범위
    }

    @Test
    fun `parse energy from string correctly`() {
        val energy1 = Energy.parse("100J")
        assertEquals("J", energy1.symbol)
        assertEquals(100.0, energy1.value, DELTA)

        val energy2 = Energy.parse("50 kWh")
        assertEquals("kWh", energy2.symbol)
        assertEquals(50.0, energy2.value, DELTA)

        val energy3 = Energy.parse("2000cal")
        assertEquals("cal", energy3.symbol)
        assertEquals(2000.0, energy3.value, DELTA)

        val energy4 = Energy.parse("100kcal")  // 대문자 Cal은 kcal로 파싱되어야 함
        assertEquals("kcal", energy4.symbol)
        assertEquals(100.0, energy4.value, DELTA)
    }

    @Test
    fun `create energy with factory methods`() {
        val energy1 = Energy.joules(100.0)
        assertEquals(100.0, energy1.joules, DELTA)

        val energy2 = Energy.kilojoules(1.0)
        assertEquals(1000.0, energy2.joules, DELTA)

        val energy3 = Energy.kilowattHours(1.0)
        assertEquals(3_600_000.0, energy3.joules, DELTA)

        val energy4 = Energy.calories(1000.0)
        assertEquals(4184.0, energy4.joules, DELTA)

        val energy5 = Energy.kilocalories(1.0)
        assertEquals(4184.0, energy5.joules, DELTA)

        val energy6 = Energy.btu(1.0)
        assertEquals(1055.06, energy6.joules, DELTA)
    }

    @Test
    fun `convert between different energy units`() {
        val energy = Energy.joules(1000.0)

        // J to kJ
        val kj = energy.to("kJ")
        assertEquals(1.0, kj.value, DELTA)
        assertEquals("kJ", kj.symbol)

        // J to kWh
        val kwh = energy.to("kWh")
        assertEquals(1000.0 / 3_600_000.0, kwh.value, DELTA)

        // J to cal
        val cal = energy.to("cal")
        assertEquals(239.0057361376673, cal.value, DELTA)

        // J to BTU
        val btu = energy.to("BTU")
        assertEquals(1000.0 / 1055.06, btu.value, DELTA)
    }

    @Test
    fun `property getters return correct values`() {
        val energy = Energy.kilojoules(1.0) // 1 kJ = 1000 J

        assertEquals(1000.0, energy.joules, DELTA)
        assertEquals(1.0, energy.kilojoules, DELTA)
        assertEquals(0.001, energy.megajoules, DELTA)
        assertEquals(0.000001, energy.gigajoules, DELTA)
        assertEquals(1_000_000.0, energy.millijoules, DELTA)
        assertEquals(1000.0 / 3600.0, energy.wattHours, DELTA)
        assertEquals(1000.0 / 3_600_000.0, energy.kilowattHours, DELTA)
        assertEquals(1000.0 / 4.184, energy.calories, DELTA)
        assertEquals(1000.0 / 4184.0, energy.kilocalories, DELTA)
        assertEquals(1000.0 / 1055.06, energy.btu, DELTA)
    }

    @Test
    fun `arithmetic operations work correctly`() {
        val energy1 = Energy.joules(100.0)
        val energy2 = Energy.joules(50.0)

        // Addition
        val sum = energy1 + energy2
        assertEquals(150.0, sum.joules, DELTA)
        assertEquals("J", sum.symbol)

        // Subtraction
        val diff = energy1 - energy2
        assertEquals(50.0, diff.joules, DELTA)

        // Multiplication
        val doubled = energy1 * 2.0
        assertEquals(200.0, doubled.joules, DELTA)

        // Division
        val halved = energy1 / 2.0
        assertEquals(50.0, halved.joules, DELTA)
    }

    @Test
    fun `division by zero throws exception`() {
        val energy = Energy.joules(100.0)
        assertThrows<IllegalArgumentException> {
            energy / 0.0
        }
    }

    @Test
    fun `comparison operations work correctly`() {
        val energy1 = Energy.joules(100.0)
        val energy2 = Energy.kilojoules(0.1) // Same as 100 J
        val energy3 = Energy.joules(150.0)

        assertTrue(energy1 >= energy2)
        assertTrue(energy1 <= energy2)
        assertEquals(0, energy1.compareTo(energy2))

        assertTrue(energy3 > energy1)
        assertTrue(energy1 < energy3)
    }

    @Test
    fun `precision and rounding work correctly`() {
        val energy = Energy.joules(10.123456789)

        val precise2 = energy.withPrecision(2)
        assertEquals("10.12 J", precise2.format())

        val precise0 = energy.withPrecision(0)
        assertEquals("10 J", precise0.format())

        val withRounding = energy.withRounding(RoundingMode.UP).withPrecision(2)
        assertEquals("10.13 J", withRounding.format())
    }

    @Test
    fun `energy scale classification works correctly`() {
        assertEquals(EnergyScale.ATOMIC, Energy.joules(1e-15).getEnergyScale())
        assertEquals(EnergyScale.MICROSCOPIC, Energy.joules(1e-9).getEnergyScale())
        assertEquals(EnergyScale.TINY, Energy.joules(0.1).getEnergyScale())
        assertEquals(EnergyScale.SMALL, Energy.joules(100.0).getEnergyScale())
        assertEquals(EnergyScale.MODERATE, Energy.joules(10_000.0).getEnergyScale())
        assertEquals(EnergyScale.LARGE, Energy.joules(10_000_000.0).getEnergyScale())
        assertEquals(EnergyScale.VERY_LARGE, Energy.joules(1e10).getEnergyScale())
        assertEquals(EnergyScale.HUGE, Energy.joules(1e13).getEnergyScale())
        assertEquals(EnergyScale.ASTRONOMICAL, Energy.joules(1e16).getEnergyScale())
    }

    @Test
    fun `power calculations work correctly`() {
        val energy = Energy.joules(3600.0)

        // Time at 100W power
        assertEquals(36.0, energy.timeAtPower(100.0), DELTA) // 3600 J / 100 W = 36 s

        // Power over 10 seconds
        assertEquals(360.0, energy.powerOverTime(10.0), DELTA) // 3600 J / 10 s = 360 W

        // Invalid inputs
        assertThrows<IllegalArgumentException> {
            energy.timeAtPower(0.0)
        }

        assertThrows<IllegalArgumentException> {
            energy.powerOverTime(0.0)
        }
    }

    @Test
    fun `water temperature rise calculation works correctly`() {
        val energy = Energy.kilojoules(41.84) // Energy to heat 10 kg water by 1°C

        assertEquals(1.0, energy.waterTemperatureRise(10.0), 0.01)
        assertEquals(10.0, energy.waterTemperatureRise(1.0), 0.01)

        assertThrows<IllegalArgumentException> {
            energy.waterTemperatureRise(0.0)
        }
    }

    @Test
    fun `lift height calculation works correctly`() {
        val energy = Energy.joules(981.0) // Energy to lift 10 kg by 10 m

        assertEquals(10.0, energy.liftHeight(10.0), 0.01)
        assertEquals(100.0, energy.liftHeight(1.0), 0.01)

        assertThrows<IllegalArgumentException> {
            energy.liftHeight(0.0)
        }
    }

    @Test
    fun `velocity from kinetic energy works correctly`() {
        val energy = Energy.joules(100.0) // KE = 0.5 * m * v²

        // v = sqrt(2 * KE / m)
        assertEquals(10.0, energy.velocityFromKineticEnergy(2.0), DELTA) // sqrt(2 * 100 / 2) = 10
        assertEquals(14.142135623730951, energy.velocityFromKineticEnergy(1.0), DELTA) // sqrt(200)

        assertThrows<IllegalArgumentException> {
            energy.velocityFromKineticEnergy(0.0)
        }

        assertThrows<IllegalArgumentException> {
            Energy.joules(-100.0).velocityFromKineticEnergy(1.0)
        }
    }

    @Test
    fun `suggest best unit works correctly`() {
        // Atomic scale -> eV
        val atomic = Energy.joules(1e-18)
        val suggestedAtomic = atomic.suggestBestUnit()
        assertEquals("eV", suggestedAtomic.symbol)

        // Small energy -> mJ
        val small = Energy.joules(0.01)
        val suggestedSmall = small.suggestBestUnit()
        assertEquals("mJ", suggestedSmall.symbol)

        // Medium energy -> J
        val medium = Energy.joules(100.0)
        val suggestedMedium = medium.suggestBestUnit()
        assertEquals("J", suggestedMedium.symbol)

        // Large energy -> kJ
        val large = Energy.joules(10_000.0)
        val suggestedLarge = large.suggestBestUnit()
        assertEquals("kJ", suggestedLarge.symbol)

        // Electrical energy -> kWh
        val electrical = Energy.joules(10_000_000.0)
        val suggestedElectrical = electrical.suggestBestUnit()
        assertEquals("kWh", suggestedElectrical.symbol)

        // Explosive energy -> tTNT
        val explosive = Energy.joules(1e13)
        val suggestedExplosive = explosive.suggestBestUnit()
        assertEquals("tTNT", suggestedExplosive.symbol)
    }

    @Test
    fun `formatting methods work correctly`() {
        // Food calories
        val meal = Energy.kilocalories(500.0)
        assertEquals("500.0 kcal", meal.toFoodCalories())

        // Electrical energy
        val household = Energy.kilowattHours(350.5)
        assertEquals("350.500 kWh", household.toElectricalEnergy())

        val smallElectrical = Energy.wattHours(500.0)
        assertEquals("500.0 Wh", smallElectrical.toElectricalEnergy())

        val largeElectrical = Energy.megawattHours(5.5)
        assertEquals("5.500 MWh", largeElectrical.toElectricalEnergy())

        // Explosive energy
        val smallExplosive = Energy.tonTNT(0.0001)
        assertEquals("0.1 kg TNT", smallExplosive.toExplosiveEnergy())

        val mediumExplosive = Energy.tonTNT(0.5)
        assertEquals("0.500 t TNT", mediumExplosive.toExplosiveEnergy())

        val largeExplosive = Energy.tonTNT(5000.0)
        assertEquals("5.0 kt TNT", largeExplosive.toExplosiveEnergy())
    }

    @Test
    fun `validation methods work correctly`() {
        val validEnergy = Energy.joules(100.0)
        assertTrue(validEnergy.isValid())
        assertTrue(validEnergy.isWithinRange(50.0..150.0))
        assertFalse(validEnergy.isWithinRange(200.0..300.0))

        val infiniteEnergy = Energy.joules(Double.POSITIVE_INFINITY)
        assertFalse(infiniteEnergy.isValid())

        val nanEnergy = Energy.joules(Double.NaN)
        assertFalse(nanEnergy.isValid())
    }

    @Test
    fun `equals and hashCode work correctly`() {
        val energy1 = Energy.joules(100.0)
        val energy2 = Energy.kilojoules(0.1) // Same as 100 J
        val energy3 = Energy.joules(150.0)

        assertEquals(energy1, energy2)
        assertEquals(energy1.hashCode(), energy2.hashCode())
        assertFalse(energy1.equals(energy3))
        assertFalse(energy1.hashCode() == energy3.hashCode())
    }

    @Test
    fun `toString returns formatted string`() {
        val energy = Energy.joules(10.5)
        assertEquals("10.5 J", energy.toString())

        val preciseEnergy = Energy.joules(10.123456).withPrecision(2)
        assertEquals("10.12 J", preciseEnergy.toString())
    }

    @Test
    fun `parse invalid input throws exception`() {
        assertThrows<IllegalArgumentException> {
            Energy.parse("invalid")
        }

        assertThrows<IllegalArgumentException> {
            Energy.parse("100")
        }

        assertThrows<IllegalArgumentException> {
            Energy.parse("ten joules")
        }

        assertThrows<IllegalArgumentException> {
            Energy.parse("100 xyz")
        }
    }

    @Test
    fun `convert to invalid unit throws exception`() {
        val energy = Energy.joules(100.0)

        assertThrows<IllegalArgumentException> {
            energy.to("kg") // Wrong category
        }

        assertThrows<IllegalArgumentException> {
            energy.to("xyz") // Unknown unit
        }
    }

    @Test
    fun `complex conversion scenarios`() {
        // Test chain conversions
        val original = Energy.kilowattHours(1.0)
        val converted = original.to("J").to("cal").to("BTU")

        // Should maintain value through conversions
        assertEquals(original.joules, converted.joules, 1e-6)

        // Test very small energies (atomic scale)
        val electronEnergy = Energy.electronVolts(1.0)
        assertEquals(1.602176634e-19, electronEnergy.joules, 1e-25)

        // Test very large energies (explosive scale)
        val nuclearEnergy = Energy.tonTNT(1.0)
        assertEquals(4.184e9, nuclearEnergy.joules, 1e3)
    }

    @Test
    fun `edge cases and boundary conditions`() {
        // Zero energy
        val zeroEnergy = Energy.joules(0.0)
        assertEquals(0.0, zeroEnergy.kilojoules, DELTA)
        assertEquals(0.0, zeroEnergy.kilowattHours, DELTA)
        assertEquals(0.0, zeroEnergy.calories, DELTA)

        // Very large energy
        val cosmicEnergy = Energy.joules(1e20) // Cosmic scale
        val cosmicScale = cosmicEnergy.getEnergyScale()
        assertEquals(EnergyScale.ASTRONOMICAL, cosmicScale)

        // Negative energy (valid for some physics calculations)
        val negativeEnergy = Energy.joules(-100.0)
        assertTrue(negativeEnergy.isValid())
        assertEquals(-100.0, negativeEnergy.joules, DELTA)
    }

    @Test
    fun `precision edge cases`() {
        val energy = Energy.joules(1.0/3.0) // 0.333...

        // Test different precision levels
        val p0 = energy.withPrecision(0)
        assertTrue(p0.format().matches(Regex("\\d+ J")))

        val p1 = energy.withPrecision(1)
        assertTrue(p1.format().matches(Regex("\\d+\\.\\d J")))

        val p5 = energy.withPrecision(5)
        assertTrue(p5.format().matches(Regex("\\d+\\.\\d{5} J")))

        // Test invalid precision
        assertThrows<IllegalArgumentException> {
            energy.withPrecision(-1)
        }
    }

    @Test
    fun `rounding modes work correctly`() {
        val energy = Energy.joules(1.235)

        val halfUp = energy.withRounding(RoundingMode.HALF_UP).withPrecision(2)
        assertEquals("1.24 J", halfUp.format())

        val halfDown = energy.withRounding(RoundingMode.HALF_DOWN).withPrecision(2)
        assertEquals("1.24 J", halfDown.format())

        val up = energy.withRounding(RoundingMode.UP).withPrecision(2)
        assertEquals("1.24 J", up.format())

        val down = energy.withRounding(RoundingMode.DOWN).withPrecision(2)
        assertEquals("1.23 J", down.format())
    }

    @Test
    fun `unit aliases work correctly`() {
        // Test various aliases for kilocalories
        val energy1 = Energy.parse("100kcal")
        val energy3 = Energy.parse("100 kilocalories")

        assertEquals(energy1.joules, energy3.joules, DELTA)

        // Test BTU aliases
        val btu1 = Energy.parse("100BTU")
        val btu2 = Energy.parse("100btu")
        val btu3 = Energy.parse("100 British thermal units")

        assertEquals(btu1.joules, btu2.joules, DELTA)
        assertEquals(btu1.joules, btu3.joules, DELTA)
    }

    @Test
    fun `realistic usage scenarios`() {
        // Daily food intake
        val dailyCalories = Energy.kilocalories(2000.0)
        assertEquals(EnergyScale.LARGE, dailyCalories.getEnergyScale())
        assertEquals("2000.0 kcal", dailyCalories.toFoodCalories())

        // Household monthly electricity
        val monthlyElectricity = Energy.kilowattHours(500.0)
        assertEquals(1.8e9, monthlyElectricity.joules, 1e6)
        assertEquals("500.000 kWh", monthlyElectricity.toElectricalEnergy())

        // Car battery
        val carBattery = Energy.kilowattHours(75.0) // Tesla Model 3 battery
        assertEquals(2.7e8, carBattery.joules, 1e6)

        // AA battery
        val aaBattery = Energy.wattHours(2.5)
        assertEquals(9000.0, aaBattery.joules, DELTA)

        // Nuclear bomb (Hiroshima)
        val hiroshima = Energy.tonTNT(15000.0) // 15 kilotons
        assertEquals("15.0 kt TNT", hiroshima.toExplosiveEnergy())
    }
}