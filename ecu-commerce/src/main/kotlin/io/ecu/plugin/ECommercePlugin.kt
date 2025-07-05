package io.ecu.plugin

import io.ecu.spi.AbstractUnitPlugin
import io.ecu.spi.LogLevel
import io.ecu.module.UnitModule
import io.ecu.module.RetailPackagingModule
import io.ecu.module.BeveragePackagingModule
import io.ecu.module.SimpleUnitModule
import io.ecu.UnitCategory

/**
 * E-Commerce 단위 플러그인
 * 
 * 전자상거래에서 사용되는 다양한 단위들을 제공합니다.
 * 
 * @since 1.1.0
 */
public class ECommercePlugin : AbstractUnitPlugin(
    name = "ecommerce-units",
    version = "1.0.0",
    description = "E-Commerce specific units for retail and wholesale"
) {
    
    override val priority: Int = 50 // 기본 플러그인보다 우선
    
    override fun onInitialize() {
        log(LogLevel.INFO, "Initializing E-Commerce Units Plugin")
        
        // 설정 확인
        val enableCustomUnits = getConfig("ecommerce.custom.units.enabled", "true")?.toBoolean() ?: true
        
        if (enableCustomUnits) {
            log(LogLevel.DEBUG, "Custom units are enabled")
        }
    }
    
    override fun getModules(): List<UnitModule> {
        return listOf(
            RetailPackagingModule(),
            BeveragePackagingModule(),
            ShippingUnitsModule(),
            InventoryUnitsModule()
        )
    }
}

/**
 * 배송 관련 단위 모듈
 */
private class ShippingUnitsModule : SimpleUnitModule(
    name = "shipping-units",
    version = "1.0.0"
) {
    override fun configure(registry: io.ecu.UnitRegistry) {
        // 배송 박스 크기
        registry.unit {
            symbol("small-box")
            displayName("Small Shipping Box")
            category(UnitCategory.VOLUME)
            baseRatio(10.0) // 10 리터
            alias("sm-box", "small")
        }
        
        registry.unit {
            symbol("medium-box")
            displayName("Medium Shipping Box")
            category(UnitCategory.VOLUME)
            baseRatio(30.0) // 30 리터
            alias("md-box", "medium")
        }
        
        registry.unit {
            symbol("large-box")
            displayName("Large Shipping Box")
            category(UnitCategory.VOLUME)
            baseRatio(60.0) // 60 리터
            alias("lg-box", "large")
        }
        
        // 팔레트 단위
        registry.unit {
            symbol("euro-pallet")
            displayName("Euro Pallet")
            category(UnitCategory.AREA)
            baseRatio(0.96) // 0.8m x 1.2m = 0.96 m²
            alias("EUR", "EUR-pallet")
        }
        
        registry.unit {
            symbol("us-pallet")
            displayName("US Standard Pallet")
            category(UnitCategory.AREA)
            baseRatio(1.1684) // 40" x 48" ≈ 1.1684 m²
            alias("US", "US-pallet")
        }
    }
}

/**
 * 재고 관리 단위 모듈
 */
private class InventoryUnitsModule : SimpleUnitModule(
    name = "inventory-units",
    version = "1.0.0"
) {
    override fun configure(registry: io.ecu.UnitRegistry) {
        // SKU 기반 단위
        registry.unit {
            symbol("sku")
            displayName("Stock Keeping Unit")
            category(UnitCategory.QUANTITY)
            baseRatio(1.0)
            alias("SKU")
        }
        
        // 재고 위치 단위
        registry.unit {
            symbol("bin")
            displayName("Storage Bin")
            category(UnitCategory.QUANTITY)
            baseRatio(1.0)
            alias("storage-bin", "bin-location")
        }
        
        // 로트 단위
        registry.unit {
            symbol("lot")
            displayName("Inventory Lot")
            category(UnitCategory.QUANTITY)
            baseRatio(1.0)
            alias("batch", "lot-number")
        }
        
        // 팩 사이즈 변형
        registry.unit {
            symbol("master-carton")
            displayName("Master Carton")
            category(UnitCategory.QUANTITY)
            baseRatio(48.0) // 일반적으로 48개
            alias("master", "master-pack")
        }
        
        registry.unit {
            symbol("inner-pack")
            displayName("Inner Pack")
            category(UnitCategory.QUANTITY)
            baseRatio(6.0) // 일반적으로 6개
            alias("inner", "inner-carton")
        }
    }
}