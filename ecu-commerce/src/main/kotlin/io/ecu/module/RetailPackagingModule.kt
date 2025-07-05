package io.ecu.module

import io.ecu.UnitCategory
import io.ecu.UnitRegistry

/**
 * 소매 포장 단위 모듈
 * 
 * 일반적인 소매업에서 사용되는 포장 단위들을 제공합니다.
 * 
 * @since 1.1.0
 */
public class RetailPackagingModule : SimpleUnitModule(
    name = "retail-packaging",
    version = "1.0.0"
) {
    override val description: String = "Common retail packaging units"
    
    override fun configure(registry: UnitRegistry) {
        // 6개들이 팩
        registry.unit {
            symbol("6-pack")
            displayName("Six Pack")
            category(UnitCategory.QUANTITY)
            baseRatio(6.0)
            alias("sixpack", "6pack", "6pk")
            
            customConversion("dozen") { value ->
                // 6팩 2개 = 1다스
                value / 2.0
            }
        }
        
        // 12개들이 박스
        registry.unit {
            symbol("box-12")
            displayName("Box of 12")
            category(UnitCategory.QUANTITY)
            baseRatio(12.0)
            alias("12-box", "dozen-box")
            
            customConversion("case") { value ->
                // 보통 1 케이스 = 4 박스 (48개)
                value / 4.0
            }
        }
        
        // 24개들이 박스
        registry.unit {
            symbol("box-24")
            displayName("Box of 24")
            category(UnitCategory.QUANTITY)
            baseRatio(24.0)
            alias("24-box", "case")
            
            customConversion("pallet") { value ->
                // 1 팔레트 = 40 박스 (960개)
                value / 40.0
            }
        }
        
        // 대량 포장 단위
        registry.unit {
            symbol("bulk-pack")
            displayName("Bulk Pack")
            category(UnitCategory.QUANTITY)
            baseRatio(100.0)
            alias("bulk", "100-pack")
        }
        
        // 슈링크 랩 팩
        registry.unit {
            symbol("shrink-wrap")
            displayName("Shrink Wrap Pack")
            category(UnitCategory.QUANTITY)
            baseRatio(4.0)
            alias("shrink", "4-pack")
        }
    }
}

/**
 * 음료 포장 단위 모듈
 * 
 * 음료 산업에서 사용되는 특수 포장 단위들
 * 
 * @since 1.1.0
 */
public class BeveragePackagingModule : SimpleUnitModule(
    name = "beverage-packaging",
    version = "1.0.0"
) {
    override val description: String = "Beverage industry packaging units"
    
    override fun configure(registry: UnitRegistry) {
        // 4팩 (맥주, 에너지 드링크 등)
        registry.unit {
            symbol("4-pack")
            displayName("Four Pack")
            category(UnitCategory.QUANTITY)
            baseRatio(4.0)
            alias("fourpack", "4pk")
        }
        
        // 8팩
        registry.unit {
            symbol("8-pack")
            displayName("Eight Pack")
            category(UnitCategory.QUANTITY)
            baseRatio(8.0)
            alias("eightpack", "8pk")
        }
        
        // 30팩 (대용량)
        registry.unit {
            symbol("30-pack")
            displayName("Thirty Pack")
            category(UnitCategory.QUANTITY)
            baseRatio(30.0)
            alias("thirtypack", "30pk", "cube")
        }
        
        // 슬랩 (24캔, 호주/뉴질랜드 용어)
        registry.unit {
            symbol("slab")
            displayName("Slab")
            category(UnitCategory.QUANTITY)
            baseRatio(24.0)
            alias("carton")
        }
    }
}
