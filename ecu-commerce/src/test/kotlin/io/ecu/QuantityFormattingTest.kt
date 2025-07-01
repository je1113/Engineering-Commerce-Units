package io.ecu

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * 수량 포맷팅 실용적 테스트
 * 
 * 실제 비즈니스에서 사용되는 다양한 포맷팅 요구사항을 테스트합니다:
 * - 지역별 숫자 형식
 * - 다양한 단위 표시 스타일
 * - 비즈니스 상황별 포맷팅
 */
class QuantityFormattingTest {

    @Test
    fun `simple quantity formatting without compound units`() {
        // 복합 단위 없이 간단한 포맷팅 테스트 (원래 실패했던 테스트)
        val quantity = ECU.quantity("1234.56 pieces")
        
        val formatter = QuantityFormatter(
            locale = QuantityFormatter.Locale.US,
            options = QuantityFormatter.FormattingOptions(
                useThousandsSeparator = true,
                decimalPlaces = 2,
                useCompoundUnits = false, // 복합 단위 비활성화가 핵심
                unitStyle = QuantityFormatter.UnitStyle.ABBREVIATED
            )
        )
        
        val formatted = formatter.format(quantity)
        
        // 간단한 형식이어야 함: "1,234.56 pcs"
        assertTrue(formatted.contains("1,234.56"))
        assertTrue(formatted.contains("pcs"))
        assertEquals("1,234.56 pcs", formatted)
        
        // EU 형식 테스트
        val euFormatter = QuantityFormatter(
            locale = QuantityFormatter.Locale.EU,
            options = QuantityFormatter.FormattingOptions(
                useThousandsSeparator = true,
                decimalPlaces = 2,
                useCompoundUnits = false,
                unitStyle = QuantityFormatter.UnitStyle.ABBREVIATED
            )
        )
        
        val euFormatted = euFormatter.format(quantity)
        assertTrue(euFormatted.contains("1.234,56"))
        assertTrue(euFormatted.contains("pcs"))
        assertEquals("1.234,56 pcs", euFormatted)
    }

    @Test
    fun `regional number formatting differences`() {
        val quantity = ECU.quantity("12345.67 pieces")
        
        // 미국 형식: 1,234.56
        val usFormatter = QuantityFormatter(
            locale = QuantityFormatter.Locale.US,
            options = QuantityFormatter.FormattingOptions(
                useThousandsSeparator = true,
                decimalPlaces = 2,
                useCompoundUnits = false
            )
        )
        val usFormatted = usFormatter.format(quantity)
        assertTrue(usFormatted.contains("12,345.67"))
        
        // 유럽 형식: 1.234,56
        val euFormatter = QuantityFormatter(
            locale = QuantityFormatter.Locale.EU,
            options = QuantityFormatter.FormattingOptions(
                useThousandsSeparator = true,
                decimalPlaces = 2,
                useCompoundUnits = false
            )
        )
        val euFormatted = euFormatter.format(quantity)
        assertTrue(euFormatted.contains("12.345,67"))
        
        // 한국 형식: 1,234.56 (미국과 동일)
        val krFormatter = QuantityFormatter(
            locale = QuantityFormatter.Locale.KR,
            options = QuantityFormatter.FormattingOptions(
                useThousandsSeparator = true,
                decimalPlaces = 2,
                useCompoundUnits = false
            )
        )
        val krFormatted = krFormatter.format(quantity)
        assertTrue(krFormatted.contains("12,345.67"))
    }

    @Test
    fun `unit style variations for business contexts`() {
        val quantity = ECU.quantity("144 pieces")
        
        // 약어 형식 (재고 시스템용)
        val abbreviatedFormatter = QuantityFormatter(
            options = QuantityFormatter.FormattingOptions(
                unitStyle = QuantityFormatter.UnitStyle.ABBREVIATED,
                useCompoundUnits = false
            )
        )
        val abbreviated = abbreviatedFormatter.format(quantity)
        assertTrue(abbreviated.contains("pcs"))
        
        // 전체 단위명 (고객 대면용)
        val fullFormatter = QuantityFormatter(
            options = QuantityFormatter.FormattingOptions(
                unitStyle = QuantityFormatter.UnitStyle.FULL,
                useCompoundUnits = false
            )
        )
        val full = fullFormatter.format(quantity)
        assertTrue(full.contains("pieces"))
        
        // 심볼 형식 (컴팩트 디스플레이용)
        val symbolFormatter = QuantityFormatter(
            options = QuantityFormatter.FormattingOptions(
                unitStyle = QuantityFormatter.UnitStyle.SYMBOL,
                useCompoundUnits = false
            )
        )
        val symbol = symbolFormatter.format(quantity)
        assertTrue(symbol.contains("#") || symbol.contains("pcs")) // 심볼 또는 대체 약어
    }

    @Test
    fun `decimal places control for different business needs`() {
        val quantity = ECU.quantity("100.123456 pieces")
        
        // 정수만 표시 (재고 카운트용)
        val integerFormatter = QuantityFormatter(
            options = QuantityFormatter.FormattingOptions(
                decimalPlaces = 0,
                useCompoundUnits = false
            )
        )
        val integerFormatted = integerFormatter.format(quantity)
        assertTrue(integerFormatted.contains("100"))
        assertTrue(!integerFormatted.contains("."))
        
        // 2자리 소수점 (회계용)
        val accountingFormatter = QuantityFormatter(
            options = QuantityFormatter.FormattingOptions(
                decimalPlaces = 2,
                useCompoundUnits = false
            )
        )
        val accountingFormatted = accountingFormatter.format(quantity)
        assertTrue(accountingFormatted.contains("100.12"))
        
        // 4자리 소수점 (정밀 계산용)
        val precisionFormatter = QuantityFormatter(
            options = QuantityFormatter.FormattingOptions(
                decimalPlaces = 4,
                useCompoundUnits = false
            )
        )
        val precisionFormatted = precisionFormatter.format(quantity)
        assertTrue(precisionFormatted.contains("100.1235"))
    }

    @Test
    fun `thousands separator control`() {
        val quantity = ECU.quantity("12345 pieces")
        
        // 천단위 구분자 사용
        val withSeparator = QuantityFormatter(
            options = QuantityFormatter.FormattingOptions(
                useThousandsSeparator = true,
                useCompoundUnits = false
            )
        )
        val formatted = withSeparator.format(quantity)
        assertTrue(formatted.contains("12,345"))
        
        // 천단위 구분자 미사용
        val withoutSeparator = QuantityFormatter(
            options = QuantityFormatter.FormattingOptions(
                useThousandsSeparator = false,
                useCompoundUnits = false
            )
        )
        val unformatted = withoutSeparator.format(quantity)
        assertTrue(unformatted.contains("12345"))
        assertTrue(!unformatted.contains(","))
    }

    @Test
    fun `negative quantity formatting`() {
        val negativeQuantity = ECU.quantity("-50 pieces")
        
        // 마이너스 기호 형식
        val minusFormatter = QuantityFormatter(
            options = QuantityFormatter.FormattingOptions(
                negativeFormat = QuantityFormatter.NegativeFormat.MINUS_SIGN,
                useCompoundUnits = false
            )
        )
        val minusFormatted = minusFormatter.format(negativeQuantity)
        assertTrue(minusFormatted.startsWith("-"))
        assertEquals("-50 pcs", minusFormatted)
        
        // 괄호 형식 (회계용) - 단위도 괄호 안에 포함되어야 함
        val parenthesesFormatter = QuantityFormatter(
            options = QuantityFormatter.FormattingOptions(
                negativeFormat = QuantityFormatter.NegativeFormat.PARENTHESES,
                useCompoundUnits = false
            )
        )
        val parenthesesFormatted = parenthesesFormatter.format(negativeQuantity)
        assertTrue(parenthesesFormatted.startsWith("("))
        assertTrue(parenthesesFormatted.endsWith(")"))
        assertEquals("(50 pcs)", parenthesesFormatted)
    }

    @Test
    fun `preset formatters for common business scenarios`() {
        val quantity = ECU.quantity("1234.56 pieces")
        
        // 미국 표준 포맷터
        val usStandard = QuantityFormatter.US_STANDARD.format(quantity)
        assertNotNull(usStandard)
        
        // 유럽 표준 포맷터
        val euStandard = QuantityFormatter.EU_STANDARD.format(quantity)
        assertNotNull(euStandard)
        
        // 컴팩트 포맷터
        val compact = QuantityFormatter.COMPACT.format(quantity)
        assertNotNull(compact)
        
        // 상세 포맷터 (하지만 복합 단위는 비활성화되어야 함)
        val verbose = QuantityFormatter.VERBOSE.format(quantity)
        assertNotNull(verbose)
        
        // 회계 포맷터
        val accounting = QuantityFormatter.ACCOUNTING.format(quantity)
        assertNotNull(accounting)
    }

    @Test
    fun `extension functions for quick formatting`() {
        val quantity = ECU.quantity("500 pieces")
        
        // 기본 포맷팅
        val defaultFormat = quantity.format()
        assertNotNull(defaultFormat)
        
        // 컴팩트 포맷팅
        val compactFormat = quantity.toCompactString()
        assertNotNull(compactFormat)
        
        // 상세 포맷팅
        val verboseFormat = quantity.toVerboseString()
        assertNotNull(verboseFormat)
        
        // 회계 포맷팅
        val accountingFormat = quantity.toAccountingString()
        assertNotNull(accountingFormat)
    }

    @Test
    fun `inventory label formatting for warehouse systems`() {
        // 창고 시스템에서 사용하는 간결한 라벨 형식
        val quantities = listOf(
            ECU.quantity("12 pieces"),
            ECU.quantity("144 pieces"), // 1 gross
            ECU.quantity("500 pieces")  // 1 ream equivalent
        )
        
        val warehouseFormatter = QuantityFormatter(
            options = QuantityFormatter.FormattingOptions(
                unitStyle = QuantityFormatter.UnitStyle.ABBREVIATED,
                decimalPlaces = 0,
                useThousandsSeparator = false,
                useCompoundUnits = false
            )
        )
        
        val labels = quantities.map { warehouseFormatter.format(it) }
        
        assertEquals("12 pcs", labels[0])
        assertEquals("144 pcs", labels[1])
        assertEquals("500 pcs", labels[2])
    }

    @Test
    fun `customer invoice formatting`() {
        // 고객 청구서에 표시되는 친숙한 형식
        val orderItems = listOf(
            ECU.quantity("24 pieces"),   // 2 dozen
            ECU.quantity("36 pieces"),   // 3 dozen
            ECU.quantity("100 pieces")   // bulk
        )
        
        val invoiceFormatter = QuantityFormatter(
            locale = QuantityFormatter.Locale.US,
            options = QuantityFormatter.FormattingOptions(
                unitStyle = QuantityFormatter.UnitStyle.FULL,
                decimalPlaces = 0,
                useThousandsSeparator = true,
                useCompoundUnits = false
            )
        )
        
        val invoiceLines = orderItems.map { invoiceFormatter.format(it) }
        
        assertTrue(invoiceLines[0].contains("pieces"))
        assertTrue(invoiceLines[1].contains("pieces"))
        assertTrue(invoiceLines[2].contains("pieces"))
        
        // 수량이 1이 아닐 때 복수형 사용 확인
        assertTrue(invoiceLines.all { it.contains("pieces") }) // 모두 복수
    }

    @Test
    fun `mobile app compact display formatting`() {
        // 모바일 앱의 제한된 화면 공간을 위한 초간결 형식
        val quantities = listOf(
            ECU.quantity("1500 pieces"),
            ECU.quantity("25000 pieces"),
            ECU.quantity("100.5 pieces")
        )
        
        val mobileFormatter = QuantityFormatter(
            options = QuantityFormatter.FormattingOptions(
                unitStyle = QuantityFormatter.UnitStyle.SYMBOL,
                decimalPlaces = 1,
                useThousandsSeparator = true,
                useCompoundUnits = false
            )
        )
        
        val mobileDisplays = quantities.map { mobileFormatter.format(it) }
        
        // 심볼 또는 매우 짧은 약어 사용
        mobileDisplays.forEach { display ->
            assertTrue(display.length < 15) // 화면 공간 제약
            assertTrue(display.contains("#") || display.contains("pcs"))
        }
    }

    @Test
    fun `API response formatting for different clients`() {
        val quantity = ECU.quantity("2500.75 pieces")
        
        // REST API용 - 구조화된 응답
        val apiFormatter = QuantityFormatter(
            options = QuantityFormatter.FormattingOptions(
                unitStyle = QuantityFormatter.UnitStyle.ABBREVIATED,
                decimalPlaces = 2,
                useThousandsSeparator = false, // JSON에서는 숫자로 처리
                useCompoundUnits = false
            )
        )
        
        val apiResponse = apiFormatter.format(quantity)
        assertEquals("2500.75 pcs", apiResponse)
        
        // 사용자 인터페이스용 - 읽기 쉬운 형식
        val uiFormatter = QuantityFormatter(
            locale = QuantityFormatter.Locale.US,
            options = QuantityFormatter.FormattingOptions(
                unitStyle = QuantityFormatter.UnitStyle.FULL,
                decimalPlaces = 2,
                useThousandsSeparator = true,
                useCompoundUnits = false
            )
        )
        
        val uiDisplay = uiFormatter.format(quantity)
        assertTrue(uiDisplay.contains("2,500.75"))
        assertTrue(uiDisplay.contains("pieces"))
    }
}
