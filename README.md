# LiteQuest

![Maven Central Version](https://img.shields.io/maven-central/v/io.github.ellykits.litequest/litequest-library)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-purple.svg)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web-green.svg)](https://kotlinlang.org/docs/multiplatform.html)

A lightweight, FHIR-inspired questionnaire library for Kotlin Multiplatform applications.

## This library is :construction: work in progress and not production ready.

## Installation

Add to your `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("io.github.ellykits.litequest:litequest-library:1.0.0-alpha03")
            }
        }
    }
}
```

## Usage

### Basic Questionnaire

```kotlin
@Composable
fun MyQuestionnaireScreen() {
    val questionnaire = Questionnaire(
        id = "patient-intake",
        version = "1.0.0",
        title = "Patient Intake Form",
        items = listOf(
            Item(
                linkId = "name",
                type = ItemType.TEXT,
                text = "What is your full name?",
                required = true
            ),
            Item(
                linkId = "age",
                type = ItemType.INTEGER,
                text = "What is your age?",
                required = true
            )
        )
    )

    val evaluator = remember { LiteQuestEvaluator(questionnaire) }
    val manager = remember { QuestionnaireManager(questionnaire, evaluator) }
    val state by manager.state.collectAsState()
    var mode by remember { mutableStateOf(QuestionnaireMode.Edit) }

    QuestionnaireScreen(
        type = QuestionnaireType.Single(questionnaire),
        state = state,
        mode = mode,
        onAnswerChange = { linkId, value, text -> manager.updateAnswer(linkId, value, text) },
        onSubmit = { println("Form submitted: ${state.response}") },
        onModeChange = { newMode -> mode = newMode },
        onDismiss = { /* Handle dismiss */ }
    )
}
```

### Paginated Questionnaires

```kotlin
val paginatedQuestionnaire = PaginatedQuestionnaire(
    id = "health-survey",
    title = "Health Survey",
    pages = listOf(
        QuestionnairePage(
            id = "demographics",
            title = "Demographics",
            order = 0,
            items = listOf(/* page 1 items */)
        ),
        QuestionnairePage(
            id = "health-history",
            title = "Health History",
            order = 1,
            items = listOf(/* page 2 items */)
        )
    )
)

QuestionnaireScreen(
    type = QuestionnaireType.Paginated(paginatedQuestionnaire),
    state = state,
    onAnswerChange = { linkId, value, text -> manager.updateAnswer(linkId, value, text) },
    onSubmit = { /* Handle submission */ },
    onDismiss = { /* Handle dismiss */ }
)
```

### JsonLogic Expressions

Visibility conditions (skip logic):

```kotlin
Item(
    linkId = "symptoms",
    text = "Please describe your symptoms",
    visibleIf = buildJsonObject {
        put("==", buildJsonObject {
            put("0", buildJsonObject { put("var", "has-symptoms") })
            put("1", true)
        })
    }
)
```

Calculated expressions:

```kotlin
// BMI calculation
Item(
    linkId = "bmi",
    type = ItemType.DECIMAL,
    text = "Body Mass Index",
    readOnly = true,
    calculatedExpression = buildJsonObject {
        put("/", buildJsonArray {
            add(buildJsonObject { put("var", "weight") })
            add(buildJsonObject {
                put("*", buildJsonArray {
                    add(buildJsonObject { put("var", "height") })
                    add(buildJsonObject { put("var", "height") })
                })
            })
        })
    }
)

// String concatenation
Item(
    linkId = "fullName",
    type = ItemType.STRING,
    text = "Full Name",
    readOnly = true,
    calculatedExpression = buildJsonObject {
        put("cat", buildJsonArray {
            add(buildJsonObject { put("var", "firstName") })
            add(JsonPrimitive(" "))
            add(buildJsonObject { put("var", "lastName") })
        })
    }
)
```

### Custom Widgets

```kotlin
class RatingWidget(override val item: Item) : ItemWidget {
    @Composable
    override fun Render(
        value: JsonElement?,
        onValueChange: (JsonElement, String?) -> Unit,
        errorMessage: String?
    ) {
        val rating = value?.jsonPrimitive?.intOrNull ?: 0
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(5) { index ->
                Icon(
                    imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star ${index + 1}",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onValueChange(JsonPrimitive(index + 1), null) },
                    tint = if (index < rating) Color(0xFFFFB300) else Color.Gray
                )
            }
        }
    }
}

// Register custom widget
val factory = DefaultWidgetFactory().apply {
    registerWidget(ItemType.RATING) { RatingWidget(it) }
}
```

### Custom JsonLogic Evaluator

```kotlin
class CustomJsonLogicEvaluator : JsonLogicEvaluator() {
    override fun evaluate(logic: JsonElement, data: Map<String, Any?>): Any? {
        // Add custom operators
        if (logic is JsonObject && logic.containsKey("custom_contains")) {
            // Your custom logic here
        }
        return super.evaluate(logic, data)
    }
}

val customEvaluator = CustomJsonLogicEvaluator()
val evaluator = LiteQuestEvaluator(questionnaire, customEvaluator)
val manager = QuestionnaireManager(questionnaire, evaluator)
```

## Architecture

LiteQuest follows a clean, layered architecture:

```txt
library/
├── model/        # Data structures (Questionnaire, Item, Response)
├── engine/       # JsonLogic evaluation, validation, visibility, calculations
├── state/        # QuestionnaireManager - reactive state orchestration
├── ui/           # Compose UI components
│   ├── screen/   # QuestionnaireScreen - unified Edit/Summary screen
│   ├── widget/   # Input widgets for different item types
│   ├── summary/  # Summary/review page components
│   ├── pagination/ # Multi-page support with navigation
│   └── renderer/ # Form rendering logic
└── util/         # Helper utilities
```

## Key Concepts

### JsonLogic Expressions

LiteQuest uses a **custom Kotlin Multiplatform implementation of [JsonLogic](https://jsonlogic.com/)** for all dynamic behavior. This pure-Kotlin evaluator works across all platforms (Android, iOS, Desktop, Web) without external dependencies.

**Supported Operators:**

| Operator | Category    | Description                                                         | Example                                                         |
|----------|-------------|---------------------------------------------------------------------|-----------------------------------------------------------------|
| `var`    | Variables   | Access form field values with dot notation support for nested paths | `{"var": "firstName"}` or `{"var": "patient.demographics.age"}` |
| `==`     | Comparison  | Equality check - returns true if values are equal                   | `{"==": [{"var": "age"}, 18]}`                                  |
| `!=`     | Comparison  | Inequality check - returns true if values are not equal             | `{"!=": [{"var": "status"}, "active"]}`                         |
| `>`      | Comparison  | Greater than - numeric comparison                                   | `{">": [{"var": "age"}, 18]}`                                   |
| `>=`     | Comparison  | Greater than or equal to - numeric comparison                       | `{">=": [{"var": "score"}, 70]}`                                |
| `<`      | Comparison  | Less than - numeric comparison                                      | `{"<": [{"var": "temperature"}, 38]}`                           |
| `<=`     | Comparison  | Less than or equal to - numeric comparison                          | `{"<=": [{"var": "bmi"}, 25]}`                                  |
| `and`    | Logic       | Logical AND - returns true if all conditions are true               | `{"and": [{"var": "isAdult"}, {"var": "hasConsent"}]}`          |
| `or`     | Logic       | Logical OR - returns true if any condition is true                  | `{"or": [{"var": "isEmergency"}, {"var": "hasPermission"}]}`    |
| `!`      | Logic       | Logical NOT - negates a boolean value                               | `{"!": {"var": "isDisabled"}}`                                  |
| `!!`     | Logic       | Truthy check - returns true if value exists and is truthy           | `{"!!": {"var": "optionalField"}}`                              |
| `if`     | Conditional | Ternary conditional - if/then/else logic                            | `{"if": [{"var": "isAdult"}, "adult", "minor"]}`                |
| `+`      | Arithmetic  | Addition - sums numeric values                                      | `{"+": [{"var": "score1"}, {"var": "score2"}]}`                 |
| `-`      | Arithmetic  | Subtraction - subtracts second value from first                     | `{"-": [{"var": "total"}, {"var": "discount"}]}`                |
| `*`      | Arithmetic  | Multiplication - multiplies numeric values                          | `{"*": [{"var": "price"}, {"var": "quantity"}]}`                |
| `/`      | Arithmetic  | Division - divides first value by second                            | `{"/": [{"var": "weight"}, {"var": "height"}]}`                 |
| `%`      | Arithmetic  | Modulo - returns remainder of division                              | `{"%": [{"var": "number"}, 2]}`                                 |
| `cat`    | String      | Concatenation - joins strings together                              | `{"cat": [{"var": "firstName"}, " ", {"var": "lastName"}]}`     |

**Implementation:**

- `JsonLogicEvaluator.kt` - Core evaluator engine
- `VisibilityEngine.kt` - Skip logic using JsonLogic
- `CalculatedValuesEngine.kt` - Computed fields using JsonLogic
- `ValidationEngine.kt` - Custom validation rules using JsonLogic

### Reactive State Management

State updates propagate automatically:

```txt
Answer Change → Recalculate Values → Update Visibility → Revalidate → Emit New State
```

### Widget Types

| ItemType      | Widget               | Data Type    | Features                                        |
|---------------|----------------------|--------------|-------------------------------------------------|
| STRING        | TextInputWidget      | String       | Single-line text input                          |
| TEXT          | TextInputWidget      | String       | Multi-line text area                            |
| BOOLEAN       | BooleanWidget        | Boolean      | Switch/Checkbox toggle                          |
| DECIMAL       | DecimalInputWidget   | Double       | Numeric keyboard with decimal support           |
| INTEGER       | IntegerInputWidget   | Int          | Numeric keyboard for whole numbers              |
| DATE          | DatePickerWidget     | String (ISO) | Platform-native date selection                  |
| TIME          | TimePickerWidget     | String (ISO) | Platform-native time selection                  |
| DATETIME      | DateTimePickerWidget | String (ISO) | Combined date and time selection                |
| CHOICE        | ChoiceWidget         | String(s)    | Radio buttons, Dropdowns, or Chips              |
| OPEN_CHOICE   | OpenChoiceWidget     | String(s)    | Choice with "Other" free-text option            |
| DISPLAY       | DisplayWidget        | N/A          | Static text or instructional content            |
| GROUP         | GroupWidget          | N/A          | Logical grouping of items, supports repetition  |
| QUANTITY      | QuantityWidget       | Object       | Numeric value with associated unit              |
| REFERENCE     | ReferenceWidget      | Object       | Searchable reference to external entities       |
| BARCODE       | BarcodeScannerWidget | String       | Integrated camera barcode scanning (KScan)      |
| IMAGE         | ImageSelectorWidget  | File/Base64  | Image capture or gallery selection (FileKit)    |
| ATTACHMENT    | AttachmentWidget     | File/Base64  | Generic file attachment support (FileKit)       |
| LAYOUT_ROW    | RowLayoutWidget      | N/A          | Horizontal arrangement of child widgets         |
| LAYOUT_COLUMN | ColumnLayoutWidget   | N/A          | Vertical arrangement of child widgets           |
| LAYOUT_BOX    | BoxLayoutWidget      | N/A          | Stacked or layered arrangement of child widgets |

## Running the Demo

### Desktop

```bash
./gradlew :demo:run
```

### Android

```bash
./gradlew :demo:installDebug
```

### iOS

Open `iosApp/iosApp.xcodeproj` in Xcode and run.

## Development

### Running Tests

```bash
# Run all tests
./gradlew :library:desktopTest

# Run platform-specific tests
./gradlew :library:androidUnitTest
./gradlew :library:iosSimulatorArm64Test
```

### Building

```bash
# Build library
./gradlew :library:assemble

# Build demo app
./gradlew :demo:assembleDebug
```

## Platform Support

| Platform   | Status          | Min Version          |
|------------|-----------------|----------------------|
| Android    | ✅ Stable        | API 24 (Android 7.0) |
| iOS        | ✅ Stable        | iOS 14.0+            |
| Desktop    | ✅ Stable        | JVM 11+              |
| Web (WASM) | ⚠️ Experimental | Modern browsers      |

## Documentation

- [LiteQuest Technical Specification v1.0.0](docs/spec/LITEQUEST_TECHNICAL_SPECIFICATION_V1_0_0.md) - Core engine architecture and JsonLogic evaluation

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Development Setup

1. Clone the repository
2. Open in IntelliJ IDEA or Android Studio
3. Run tests: `./gradlew :library:desktopTest`
4. Run demo: `./gradlew :demo:run`

### Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful names
- Keep functions small and focused
- Write tests for new features

## License

```txt
Copyright 2025 LiteQuest Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Acknowledgments

Based on the [LiteQuest Technical Specification v1.0.0](docs/spec/LITEQUEST_TECHNICAL_SPECIFICATION_V1_0_0.md), inspired by HL7 FHIR Questionnaire resources.

Special thanks to all [contributors](https://github.com/litequest/lite-quest/graphs/contributors).

---

Made with ❤️ by the LiteQuest community
