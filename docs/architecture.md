# ECU 아키텍처 설계 문서

## 개요

Engineering Commerce Units (ECU)는 Java 8+ 환경에서 동작하는 모듈화된 단위 변환 라이브러리입니다. 타입 안전성, 확장성, 성능을 핵심 설계 원칙으로 삼았습니다.

## 아키텍처 원칙

### 1. 모듈화 설계
```
┌─────────────────┐
│   ecu-core      │ ← 핵심 단위 변환 (50KB)
├─────────────────┤
│  ecu-commerce   │ ← 상거래 특화 기능
├─────────────────┤
│ ecu-engineering │ ← 엔지니어링 단위
└─────────────────┘
```

### 2. 레이어드 아키텍처

```
┌──────────────────────────────────────┐
│         API Layer (ECU.kt)           │
├──────────────────────────────────────┤
│    Unit Types (Length, Weight...)    │
├──────────────────────────────────────┤
│      Core Services                   │
│  ┌─────────────┬──────────────────┐ │
│  │  Registry   │  Validation      │ │
│  ├─────────────┼──────────────────┤ │
│  │  Cache      │  Metrics         │ │
│  └─────────────┴──────────────────┘ │
├──────────────────────────────────────┤
│         Base Classes                 │
│  (BaseUnit, UnitDefinition)          │
└──────────────────────────────────────┘
```

## 핵심 컴포넌트

### 1. BaseUnit 추상 클래스

모든 단위 타입의 기반이 되는 추상 클래스입니다.

```kotlin
abstract class BaseUnit<T : BaseUnit<T>>(
    protected val baseValue: Double,    // 기본 단위로 변환된 값
    val symbol: String,                  // 단위 심볼
    val displayName: String,             // 표시 이름
    val category: UnitCategory,          // 단위 카테고리
    protected val precision: Int = -1,   // 정밀도
    protected val roundingMode: RoundingMode = RoundingMode.HALF_UP
)
```

**설계 결정사항:**
- 불변(Immutable) 객체로 설계하여 스레드 안전성 보장
- 모든 값은 내부적으로 기본 단위로 저장 (예: 길이는 미터)
- 체이닝 가능한 API 제공

### 2. UnitRegistry

단위 정의를 중앙에서 관리하는 싱글톤 레지스트리입니다.

```kotlin
object UnitRegistry {
    private val definitions = ConcurrentHashMap<String, UnitDefinition>()
    private val aliases = ConcurrentHashMap<String, String>()
    
    fun register(definition: UnitDefinition)
    fun getDefinition(symbol: String): UnitDefinition?
    fun isValidUnit(symbol: String): Boolean
}
```

**특징:**
- Thread-safe 구현 (ConcurrentHashMap 사용)
- 별칭(alias) 지원
- 동적 단위 등록 가능

### 3. 변환 캐시 시스템

반복적인 변환 연산의 성능을 최적화합니다.

```kotlin
object GlobalConversionCache {
    private val cache = ConcurrentHashMap<ConversionKey, Double>()
    
    fun cachedConvert(
        value: Double,
        fromUnit: String,
        toUnit: String,
        precision: Int,
        converter: () -> Double
    ): Double
}
```

**캐시 전략:**
- LRU 기반 제거 정책
- 최대 10,000개 엔트리
- TTL: 1시간

### 4. 검증 시스템

입력값의 유효성을 검증합니다.

```kotlin
object UnitValidator {
    fun validate(value: Double, unit: String): ValidationResult
    fun validateOrThrow(value: Double, unit: String)
}
```

**검증 규칙:**
- 음수 값 허용 여부 (온도는 절대영도 이하 불가)
- 단위 존재 여부
- 값의 범위 검증

## 모듈 상세

### ecu-core (필수)

기본적인 단위 변환 기능을 제공합니다.

**지원 단위:**
- Length: m, km, cm, mm, ft, in, yd, mi
- Weight: kg, g, mg, t, lb, oz
- Volume: l, ml, m³, gal, qt, pt, fl oz
- Temperature: K, °C, °F
- Area: m², km², cm², ft², in², acre, ha

**주요 기능:**
- 타입 안전 변환
- 연산자 오버로딩
- 배치 변환
- 국제화 지원

### ecu-commerce (선택)

상거래 환경에 특화된 기능을 제공합니다.

**추가 기능:**
- Quantity 타입 (수량 관리)
- 반올림 프로파일
- 가격 단위 통합
- 재고 관리 연동

### ecu-engineering (선택)

공학/과학 계산을 위한 고급 단위를 제공합니다.

**추가 단위:**
- Pressure: Pa, kPa, MPa, bar, psi, atm
- Energy: J, kJ, MJ, kWh, cal, BTU
- Speed: m/s, km/h, mph, knot
- Torque: N⋅m, kgf⋅m, lbf⋅ft

**특수 기능:**
- ISO/NIST 표준 준수
- 표준 조건 변환 (STP/NTP)
- 불확도 계산
- 공차 관리

## 확장 메커니즘

### 1. 모듈 시스템

Jackson 스타일의 모듈 시스템을 채택했습니다.

```kotlin
class CustomModule : UnitModule {
    override fun setupModule(context: SetupContext) {
        context.registerUnit(/* ... */)
        context.registerConverter(/* ... */)
    }
}

// 사용
ECU.registerModule(CustomModule())
```

### 2. 플러그인 아키텍처

```kotlin
interface UnitPlugin {
    fun initialize(registry: UnitRegistry)
    fun getName(): String
    fun getVersion(): String
}
```

### 3. 커스텀 단위 정의

```kotlin
val customUnit = CustomUnitBuilder()
    .symbol("widget")
    .displayName("Widget")
    .category(UnitCategory.CUSTOM)
    .baseRatio(1.0)
    .aliases("widgets", "wgt")
    .build()
```

## 성능 최적화

### 1. 지연 초기화
- 단위 정의는 처음 사용될 때 로드
- 모듈은 필요시에만 초기화

### 2. 캐싱 전략
- 변환 결과 캐싱
- 파싱 결과 캐싱
- 메타데이터 캐싱

### 3. 메모리 효율성
- 플라이웨이트 패턴 적용
- 문자열 인터닝
- 약한 참조 사용

## 스레드 안전성

모든 공개 API는 스레드 안전하게 설계되었습니다.

**보장 사항:**
- 불변 객체 사용
- ConcurrentHashMap 활용
- 동기화된 초기화
- 원자적 연산

## 에러 처리

### 예외 계층구조

```
Exception
├── IllegalArgumentException
│   ├── UnknownUnitException
│   ├── InvalidValueException
│   └── UnitMismatchException
├── IllegalStateException
│   └── RegistryNotInitializedException
└── ArithmeticException
    └── DivisionByZeroException
```

### 에러 복구 전략
- 기본값 제공
- 자동 단위 추론
- 사용자 친화적 메시지

## 테스트 전략

### 1. 단위 테스트
- 각 단위 타입별 변환 정확도
- 경계값 테스트
- 예외 상황 테스트

### 2. 통합 테스트
- 모듈 간 상호작용
- 실제 사용 시나리오
- 성능 벤치마크

### 3. 속성 기반 테스트
- 변환의 가역성
- 연산의 결합법칙
- 단위의 일관성

## 향후 확장 계획

### 1. 단기 (v1.1)
- [ ] 화폐 단위 지원
- [ ] 시간대 고려 날짜/시간 단위
- [ ] GraphQL 스키마 생성

### 2. 중기 (v1.2)
- [ ] 벡터 단위 (힘, 가속도)
- [ ] 복합 단위 자동 유도
- [ ] 단위 방정식 해결

### 3. 장기 (v2.0)
- [ ] 기계 학습 기반 단위 추론
- [ ] 블록체인 단위 검증
- [ ] 양자 컴퓨팅 단위

## 결론

ECU는 확장 가능하고 유지보수가 용이한 아키텍처를 통해 다양한 도메인의 단위 변환 요구사항을 충족시킵니다. 모듈화된 설계와 명확한 책임 분리를 통해 새로운 기능 추가가 용이하며, 성능과 정확성을 모두 만족시킵니다.
