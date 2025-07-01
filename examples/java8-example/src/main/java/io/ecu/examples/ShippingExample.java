package io.ecu.examples;

import io.ecu.ECU;
import io.ecu.Length;
import io.ecu.Weight;
import io.ecu.Volume;
import io.ecu.UnitDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * ECU Core Library 사용 예제 - 배송 계산
 * 
 * Java 8 호환성을 보여주는 예제입니다.
 */
public class ShippingExample {
    
    public static void main(String[] args) {
        System.out.println("=== ECU Core Library - Shipping Example ===\n");
        
        // 1. 기본 사용법
        basicUsage();
        
        // 2. 배송 박스 크기 계산
        shippingBoxExample();
        
        // 3. 무게 제한 확인
        weightLimitExample();
        
        // 4. 배치 변환
        batchConversionExample();
        
        // 5. 단위 정보 조회
        unitInfoExample();
    }
    
    private static void basicUsage() {
        System.out.println("1. 기본 사용법");
        System.out.println("--------------");
        
        // 문자열 파싱
        Length length = ECU.length("150 cm");
        System.out.println("원본: " + length);
        System.out.println("미터로 변환: " + length.to("m"));
        System.out.println("인치로 변환: " + length.to("in"));
        
        // 직접 값 생성
        Weight weight = ECU.weight(2.5, "kg");
        System.out.println("\n무게: " + weight);
        System.out.println("파운드로 변환: " + weight.to("lb"));
        
        System.out.println();
    }
    
    private static void shippingBoxExample() {
        System.out.println("2. 배송 박스 크기 계산");
        System.out.println("--------------------");
        
        // 박스 크기 (cm)
        Length boxLength = ECU.length("60 cm");
        Length boxWidth = ECU.length("40 cm");
        Length boxHeight = ECU.length("30 cm");
        
        // 부피 계산 (리터)
        double volumeInCm3 = boxLength.getValue() * boxWidth.getValue() * boxHeight.getValue();
        Volume boxVolume = ECU.volume(volumeInCm3 / 1000.0, "l");
        
        System.out.println("박스 크기: " + boxLength + " x " + boxWidth + " x " + boxHeight);
        System.out.println("박스 부피: " + boxVolume);
        System.out.println("갤런으로: " + boxVolume.to("gal"));
        
        // 국제 배송 기준 (인치)
        System.out.println("\n국제 배송 기준 (인치):");
        System.out.println("길이: " + boxLength.to("in"));
        System.out.println("너비: " + boxWidth.to("in"));
        System.out.println("높이: " + boxHeight.to("in"));
        
        System.out.println();
    }
    
    private static void weightLimitExample() {
        System.out.println("3. 무게 제한 확인");
        System.out.println("----------------");
        
        // 항공 배송 무게 제한
        Weight airShipLimit = ECU.weight(30, "kg");
        Weight packageWeight = ECU.weight("65 lb");
        
        System.out.println("항공 배송 제한: " + airShipLimit);
        System.out.println("패키지 무게: " + packageWeight);
        System.out.println("패키지 무게 (kg): " + packageWeight.to("kg"));
        
        // 제한 확인
        boolean isOverLimit = packageWeight.getKilograms() > airShipLimit.getKilograms();
        System.out.println("무게 초과: " + (isOverLimit ? "예" : "아니오"));
        
        if (isOverLimit) {
            Weight overWeight = ECU.weight(
                packageWeight.getKilograms() - airShipLimit.getKilograms(), "kg"
            );
            System.out.println("초과 무게: " + overWeight);
        }
        
        System.out.println();
    }
    
    private static void batchConversionExample() {
        System.out.println("4. 배치 변환");
        System.out.println("-----------");
        
        // 여러 상품의 무게
        List<String> productWeights = Arrays.asList(
            "500 g",
            "2.5 kg",
            "750 g",
            "1.2 kg",
            "300 g"
        );
        
        System.out.println("상품 무게 목록:");
        productWeights.forEach(w -> System.out.println("  - " + w));
        
        // 모두 킬로그램으로 변환
        List<Weight> weightsInKg = ECU.Batch.convertWeights(productWeights, "kg");
        
        System.out.println("\n킬로그램으로 변환:");
        weightsInKg.forEach(w -> System.out.println("  - " + w));
        
        // 총 무게 계산
        double totalKg = weightsInKg.stream()
            .mapToDouble(Weight::getKilograms)
            .sum();
        
        Weight totalWeight = ECU.weight(totalKg, "kg");
        System.out.println("\n총 무게: " + totalWeight);
        System.out.println("총 무게 (lb): " + totalWeight.to("lb"));
        
        System.out.println();
    }
    
    private static void unitInfoExample() {
        System.out.println("5. 단위 정보 조회");
        System.out.println("----------------");
        
        // 지원되는 단위 조회
        Set<String> lengthUnits = ECU.Info.getSupportedLengthUnits();
        System.out.println("지원되는 길이 단위: " + lengthUnits);
        
        Set<String> weightUnits = ECU.Info.getSupportedWeightUnits();
        System.out.println("지원되는 무게 단위: " + weightUnits);
        
        // 단위 정보 상세 조회
        System.out.println("\n단위 상세 정보:");
        UnitDefinition kgDef = ECU.Info.getUnitInfo("kg");
        if (kgDef != null) {
            System.out.println("단위: " + kgDef.getSymbol());
            System.out.println("이름: " + kgDef.getDisplayName());
            System.out.println("카테고리: " + kgDef.getCategory());
            System.out.println("기본 단위: " + kgDef.isBaseUnit());
            System.out.println("별칭: " + kgDef.getAliases());
        }
        
        // 단위 유효성 확인
        System.out.println("\n단위 유효성 확인:");
        System.out.println("'kg' 유효: " + ECU.Info.isValidUnit("kg"));
        System.out.println("'xyz' 유효: " + ECU.Info.isValidUnit("xyz"));
    }
}
