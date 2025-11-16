# LiteQuest Technical Specification

**Version:** 1.0.0  
**Status:** Production Ready  
**Last Updated:** November 2025  
**License:** Apache 2.0

---

## Executive Summary

LiteQuest is a lightweight, FHIR-inspired questionnaire library for Kotlin Multiplatform applications. It bridges the gap between complex healthcare interoperability standards and pragmatic developer experience by providing a JSON-based approach optimized for mobile-first applications.

---

## 1. Vision and Objectives

### 1.1 Primary Vision

Deliver a production-ready questionnaire library that enables developers to build dynamic, validated, multi-language forms without the complexity of traditional healthcare standards.

### 1.2 Core Objectives

- **Developer Experience**: Simple, intuitive API with minimal boilerplate
- **Cross-Platform**: True code sharing across Android, iOS, Desktop, and Web
- **Performance**: Minimal runtime overhead with compile-time optimizations
- **Flexibility**: Extensible architecture supporting custom behaviors
- **Standards-Inspired**: Learn from FHIR while remaining pragmatic

---

## 2. Architecture

### 2.1 Layered Design

The library follows a strict layered architecture with clear separation of concerns:

#### Model Layer

Pure data structures with no business logic. Includes:

- Questionnaire definitions
- Response models
- Validation rules
- Calculated value definitions

#### Engine Layer

Business logic transformations:

- **JsonLogicEvaluator**: Expression evaluation engine
- **ValidationEngine**: Rule-based validation
- **VisibilityEngine**: Conditional display logic
- **CalculatedValuesEngine**: Dynamic computations
- **DataExtractionEngine**: Response transformation

#### I18n Layer

Internationalization support:

- **TranslationManager**: Translation lifecycle management
- **TranslationLoader**: Platform-specific HTTP clients
- **TranslationCache**: In-memory translation storage

#### State Layer

Reactive state management:

- **QuestionnaireManager**: Central orchestrator
- **QuestionnaireState**: Immutable state representation
- StateFlow-based reactive updates

#### Util Layer

Cross-cutting utilities:

- **DataContextBuilder**: Flatten response hierarchies
- **PathResolver**: Navigate object graphs
- **TruthinessChecker**: Type-agnostic boolean evaluation
- **JsonExtensions**: JSON manipulation helpers

### 2.2 Platform Strategy

**Common Business Logic**: 100% of business logic resides in `commonMain`, ensuring consistent behavior across all platforms.

**Platform-Specific Code**: Limited to HTTP client implementations for translation loading:

- Android: `ktor-client-android`
- iOS: `ktor-client-darwin`
- Desktop: `ktor-client-cio`
- WASM: `ktor-client-js`

---

## 3. Core Features

### 3.1 Decoupled Internationalization

**Problem**: Embedding translations in questionnaire definitions creates maintenance overhead and increases bundle size.

**Solution**: Use translation keys with remote loading.

**Benefits**:

- Single questionnaire definition for all languages
- On-demand translation loading
- Easy content updates without code changes
- Reduced initial bundle size
- Support for unlimited languages

**Implementation**:

```kotlin
TranslationManager(
    loader = TranslationLoader(),
    cache = TranslationCache()
)
```

### 3.2 Unified Expression Engine

**Problem**: Multiple DSLs for validation, visibility, and calculations create complexity.

**Solution**: JsonLogic as a single expression language for all dynamic behavior.

**Supported Operations**:

- Comparison: `==`, `!=`, `>`, `>=`, `<`, `<=`
- Logical: `and`, `or`, `!`
- Arithmetic: `+`, `-`, `*`, `/`, `%`
- Conditional: `if`
- Variable access: `var`

**Example**:

```json
{
  "if": [
    {">": [{"var": "age"}, 18]},
    "adult",
    "minor"
  ]
}
```

### 3.3 Template-Based Data Extraction

**Problem**: Response structure may differ from backend data models.

**Solution**: Flexible extraction templates that map questionnaire responses to target schemas.

**Features**:

- Source type specification (answer, calculatedValue, metadata)
- Nested structure transformations
- Path-based field mapping
- Type preservation

### 3.4 Reactive State Management

**Problem**: Manual state synchronization is error-prone and verbose.

**Solution**: Automatic propagation through StateFlow.

**State Update Chain**:

1. Answer changes → trigger evaluation
2. Recalculate computed values
3. Update visibility conditions
4. Revalidate visible items
5. Emit new state

**Guarantees**:

- Thread-safe state updates
- Conflation of rapid updates
- Back pressure handling
- Lifecycle-aware subscription

---

## 4. Design Decisions

### 4.1 Custom JsonLogic Implementation

**Rationale**:

- External libraries may not support all target platforms
- Full control over feature set and performance
- Zero external dependencies for core logic

**Trade-off**: Maintenance burden vs. control and minimal dependencies

**Result**: ~250 LOC, production-ready, optimized for mobile

### 4.2 JsonElement for Answer Values

**Rationale**: Support heterogeneous answer types without complex type hierarchies

**Trade-off**: Compile-time type safety vs. runtime flexibility

**Result**: Single unified Answer type with maximum flexibility

### 4.3 Sequential Calculated Values

**Rationale**: Enable dependent calculations (e.g., BMI category depends on BMI)

**Trade-off**: Order dependency vs. computational power

**Result**: Predictable, documented evaluation order

### 4.4 Flat Data Context

**Rationale**: Simplify JsonLogic expressions with direct variable access

**Trade-off**: Hierarchical structure vs. expression simplicity

**Result**: Intuitive `{"var": "fieldName"}` syntax

### 4.5 No Runtime Reflection

**Rationale**:

- Better performance
- Smaller binary size
- iOS compatibility
- Tree-shaking friendly

**Result**: All type resolution at compile time using Kotlin serialization

---

## 5. Performance Characteristics

### 5.1 Time Complexity

- **Memory**: O(n) where n = items × answers
- **Evaluation**: O(m × e) where m = expressions, e = expression depth
- **State Updates**: O(k) where k = visible items
- **Serialization**: O(n) where n = response size

All operations linear or better; no exponential complexity.

### 5.2 Memory Footprint

- Base library: ~150KB (minified)
- Runtime overhead: < 1MB for typical questionnaires
- Translation cache: Configurable, typically 50KB per language

### 5.3 Optimization Strategies

- Expression result caching
- Lazy visibility evaluation
- StateFlow conflation
- Efficient JSON parsing with kotlinx.serialization

---

## 6. Security Model

### 6.1 Expression Injection

**Risk**: Code injection through malicious expressions

**Mitigation**: JsonLogic is data, not code - no execution risk

### 6.2 Cross-Site Scripting (XSS)

**Risk**: Malicious content in translations

**Mitigation**: UI layer responsibility - escape HTML when rendering

### 6.3 Data Privacy

**Risk**: Unintended data collection

**Mitigation**: No built-in telemetry; full control to implementers

### 6.4 Transport Security

**Risk**: Man-in-the-middle attacks during translation loading

**Mitigation**: HTTPS required for all translation endpoints

---

## 7. Compliance

### 7.1 GDPR Compliance

- No PII stored by library
- Full data control to implementers
- Client-side processing only

### 7.2 HIPAA Compliance

- Client-side library only
- No data transmission by library
- Implementer responsible for PHI handling

### 7.3 Accessibility

- UI implementation responsibility
- Support for semantic HTML
- Screen reader compatible with proper labels

### 7.4 Localization

- Built-in support for any language with UTF-8
- RTL language support (UI layer)
- Cultural date/number formatting

---

## 8. Use Cases

### 8.1 Clinical Data Collection

- Patient intake forms
- Symptom checkers
- Health risk assessments
- Post-visit satisfaction surveys

### 8.2 Research Studies

- Multi-language research questionnaires
- Conditional question flows
- Validated data capture
- Standardized response formats

### 8.3 General Purpose Forms

- Loan applications
- Insurance quotes
- User onboarding flows
- Customer feedback collection

---

## 9. Extensibility

### 9.1 Custom Item Types

Extend `ItemType` enum and implement rendering:

```kotlin
enum class ItemType {
    // Built-in types
    STRING, INTEGER, DECIMAL, BOOLEAN, CHOICE, TEXT, DISPLAY, GROUP,
    
    // Custom types
    SIGNATURE, FILE_UPLOAD, LOCATION, DATE_TIME
}
```

### 9.2 Additional Operators

Extend `JsonLogicEvaluator` with new operators:

```kotlin
class CustomEvaluator : JsonLogicEvaluator() {
    override fun evaluate(expression: JsonObject, data: Map<String, Any?>): Any? {
        return when {
            expression.containsKey("regex") -> evaluateRegex(expression, data)
            else -> super.evaluate(expression, data)
        }
    }
}
```

### 9.3 Persistence Strategies

Integrate with platform-specific databases:

- Android: Room
- iOS: Core Data
- Cross-platform: SQLDelight

### 9.4 Pre-population

Implement data providers for questionnaire pre-filling:

```kotlin
interface DataProvider {
    suspend fun getData(questionnaireId: String): Map<String, Any?>
}
```

---

## 10. Theoretical Foundations

### 10.1 JsonLogic for Expression Evaluation

JsonLogic was chosen as the expression engine for its Kotlin Multiplatform compatibility:

- **Security**: Pure data, no code execution
- **Portability**: JSON serialization works across all platforms
- **Simplicity**: 15 operators cover validation, visibility, and calculations
- **No Platform Dependencies**: Works identically on Android, iOS, Desktop, and Web

### 10.2 StateFlow for Reactive State Management

StateFlow is the reactive primitive in LiteQuest:

- **Multiplatform**: First-party Kotlin support across all targets
- **Conflation**: Rapid updates automatically merged for UI responsiveness
- **State Holder**: Always has a current value, perfect for UI state
- **Coroutine Integration**: Natural fit with suspend functions

### 10.3 Kotlin Type Safety

The library leverages Kotlin's type system:

- **Non-nullable by default**: Eliminates null pointer errors
- **Sealed classes**: Exhaustive when expressions catch missing cases at compile time
- **Data classes**: Immutable state prevents race conditions
- **kotlinx.serialization**: Compile-time validation, no reflection overhead

### 10.4 Internationalization Architecture

Translation keys separate content from logic:

- Content managed independently from questionnaire definitions
- Remote loading with caching for offline support
- Single questionnaire definition works across all languages
- Platform-specific loaders (Ktor client) for network access

---

## 11. Future Roadmap

### 11.1 Version 1.1 (Q1 2026)

- FHIR Questionnaire bidirectional conversion
- Enhanced skip logic with jump-to functionality
- File attachment support (photos, documents, signatures)

### 11.2 Version 1.2 (Q2 2026)
- Advanced validation rules (regex, LUHN algorithm)

---

## 12. Technical Requirements

### 12.1 Dependencies

**Core**:

- Kotlin 2.0.21+
- kotlinx-serialization 1.7.3+
- kotlinx-coroutines 1.9.0+
- kotlinx-datetime 0.6.1+

**Networking**:

- ktor-client 3.0.1+

**UI (Demo)**:

- Compose Multiplatform 1.6.11+
- Lifecycle ViewModel 2.8.0+

### 12.2 Platform Requirements

- **Android**: API 24+ (Android 7.0)
- **iOS**: iOS 14.0+
- **Desktop**: JVM 11+
- **Web**: Modern browsers with WASM support

### 12.3 Build Requirements

- Gradle 8.13+
- Android Gradle Plugin 8.2.2+
- Xcode 14.0+ (for iOS builds)

---

## 13. Testing Strategy

### 13.1 Unit Tests

- 71 unit tests covering all layers
- Test coverage: >85%
- Platform: Desktop (JVM) for fast execution

### 13.2 Integration Tests

- End-to-end questionnaire flows
- Multi-language scenarios
- Complex validation chains

### 13.3 Platform Tests

- Android instrumented tests
- iOS XCTest suite
- Desktop integration tests

---

## 14. Contributing Guidelines

See CONTRIBUTING.md for:

- Code style conventions
- Pull request process
- Testing requirements
- Documentation standards

---

## 15. References

### 15.1 Inspirations

- HL7 FHIR Questionnaire Resource
- JsonLogic specification
- React Hook Form
- Survey.js

### 15.2 Standards

- ISO 8601 (Date/Time formatting)
- RFC 5646 (Language tags)
- WCAG 2.1 (Accessibility)

---

## Appendix A: JsonLogic Operator Reference

| Operator | Description      | Example                                   |
|----------|------------------|-------------------------------------------|
| `var`    | Access variable  | `{"var": "age"}`                          |
| `==`     | Equality         | `{"==": [{"var": "status"}, "active"]}`   |
| `!=`     | Inequality       | `{"!=": [{"var": "status"}, "inactive"]}` |
| `>`      | Greater than     | `{">": [{"var": "age"}, 18]}`             |
| `>=`     | Greater or equal | `{">=": [{"var": "age"}, 18]}`            |
| `<`      | Less than        | `{"<": [{"var": "age"}, 65]}`             |
| `<=`     | Less or equal    | `{"<=": [{"var": "age"}, 65]}`            |
| `and`    | Logical AND      | `{"and": [expr1, expr2]}`                 |
| `or`     | Logical OR       | `{"or": [expr1, expr2]}`                  |
| `!`      | Logical NOT      | `{"!": expr}`                             |
| `if`     | Conditional      | `{"if": [condition, then, else]}`         |
| `+`      | Addition         | `{"+": [num1, num2]}`                     |
| `-`      | Subtraction      | `{"-": [num1, num2]}`                     |
| `*`      | Multiplication   | `{"*": [num1, num2]}`                     |
| `/`      | Division         | `{"/": [num1, num2]}`                     |
| `%`      | Modulo           | `{"%": [num1, num2]}`                     |

---

### Document Control

| Version | Date     | Author         | Changes               |
|---------|----------|----------------|-----------------------|
| 1.0.0   | Nov 2025 | LiteQuest Team | Initial specification |

---

For implementation details and API documentation, see README.md and inline code documentation.
