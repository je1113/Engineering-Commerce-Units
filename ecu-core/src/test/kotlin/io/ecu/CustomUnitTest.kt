package io.ecu

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class CustomUnitTest {
    
    @BeforeEach
    fun setUp() {
        // 레지스트리 초기화
        UnitRegistry.reset()
        CustomConversionRegistry.reset()
    }
    
    @Test
    fun `should register custom unit using DSL`() {
        // Given
        UnitRegistry.registerCustomUnit {
            symbol("widget")
            displayName("Widget")
            category(UnitCategory.QUANTITY)
            baseRatio(1.0)
            alias("widgets", "wgt")
        }
        
        // When
        val definition = UnitRegistry.getDefinition("widget")
        
        // Then
        assertEquals("widget", definition?.symbol)
        assertEquals("Widget", definition?.displayName)
        assertEquals(UnitCategory.QUANTITY, definition?.category)
        assertEquals(1.0, definition?.baseRatio)
        assertTrue(definition?.aliases?.contains("widgets") == true)
        assertTrue(definition?.aliases?.contains("wgt") == true)
    }
    
    @Test
    fun `should register custom unit with custom conversions`() {
        // Given
        UnitRegistry.registerCustomUnit {
            symbol("box24")
            displayName("Box of 24")
            category(UnitCategory.QUANTITY)
            baseRatio(24.0)
            
            customConversion("dozen") { value ->
                // 24개 박스를 더즌(12개)으로 변환
                value * 2.0
            }
        }
        
        // When
        val definition = UnitRegistry.getDefinition("box24")
        val converter = CustomConversionRegistry.getCustomConverter("box24", "dozen")
        
        // Then
        assertNotNull(definition)
        assertNotNull(converter)
        assertEquals(2.0, converter?.invoke(1.0))
        assertEquals(10.0, converter?.invoke(5.0))
    }
    
    @Test
    fun `should throw exception when required fields are missing`() {
        assertThrows<IllegalArgumentException> {
            customUnit {
                // symbol is missing
                displayName("Test Unit")
                category(UnitCategory.LENGTH)
            }
        }
        
        assertThrows<IllegalArgumentException> {
            customUnit {
                symbol("test")
                // displayName is missing
                category(UnitCategory.LENGTH)
            }
        }
        
        assertThrows<IllegalArgumentException> {
            customUnit {
                symbol("test")
                displayName("Test Unit")
                // category is missing
            }
        }
    }
    
    @Test
    fun `should register alias and find unit by alias`() {
        // Given
        UnitRegistry.registerCustomUnit {
            symbol("pkt")
            displayName("Packet")
            category(UnitCategory.QUANTITY)
            baseRatio(10.0)
            alias("packet", "packets", "pk")
        }
        
        // When
        val bySymbol = UnitRegistry.getDefinition("pkt")
        val byAlias1 = UnitRegistry.getDefinition("packet")
        val byAlias2 = UnitRegistry.getDefinition("pk")
        
        // Then
        assertEquals(bySymbol, byAlias1)
        assertEquals(bySymbol, byAlias2)
        assertEquals("pkt", byAlias1?.symbol)
    }
    
    @Test
    fun `should validate base ratio is positive`() {
        assertThrows<IllegalArgumentException> {
            customUnit {
                symbol("invalid")
                displayName("Invalid Unit")
                category(UnitCategory.LENGTH)
                baseRatio(-1.0) // Invalid: negative ratio
            }
        }
        
        assertThrows<IllegalArgumentException> {
            customUnit {
                symbol("invalid")
                displayName("Invalid Unit")
                category(UnitCategory.LENGTH)
                baseRatio(0.0) // Invalid: zero ratio
            }
        }
    }
    
    @Test
    fun `should perform custom conversion`() {
        // Given
        UnitRegistry.registerCustomUnit {
            symbol("box12")
            displayName("Box of 12")
            category(UnitCategory.QUANTITY)
            baseRatio(12.0)
            
            customConversion("pallet") { value ->
                // 1 팔레트 = 40 박스
                value / 40.0
            }
            
            customConversion("container") { value ->
                // 1 컨테이너 = 1000 박스
                value / 1000.0
            }
        }
        
        // When
        val toPallet = CustomConversionRegistry.convertCustom(80.0, "box12", "pallet")
        val toContainer = CustomConversionRegistry.convertCustom(2000.0, "box12", "container")
        
        // Then
        assertEquals(2.0, toPallet)
        assertEquals(2.0, toContainer)
    }
}
