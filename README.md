# LiteQuest

![Maven Central Version](https://img.shields.io/maven-central/v/io.github.ellykits.litequest/litequest-library)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-purple.svg)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web-green.svg)](https://kotlinlang.org/docs/multiplatform.html)

A lightweight, FHIR-inspired questionnaire library for Kotlin Multiplatform applications.

## This library is :construction: work in progress and not production ready.

## Features

### Core Engine

- ✅ **Reactive State** - StateFlow-based automatic state propagation
- ✅ **Dynamic Calculations** - JsonLogic expressions for validation, visibility, and computed values
- ✅ **Type-Safe** - Full Kotlin type safety with kotlinx.serialization
- ✅ **Cross-Platform** - Single codebase for Android, iOS, Desktop, and Web
- ✅ **Extensible** - Extend JsonLogicEvaluator for custom evaluation logic

### UI Components

- ✅ **Unified Questionnaire Screen** - Single component for Edit and Summary modes
- ✅ **Multi-Page Support** - Pagination with progress indicators and navigation
- ✅ **Rich Widgets** - Text, Decimal, Integer, Boolean, Choice, Date, Time, DateTime, Quantity, Display, Group widgets
- ✅ **Media Widgets** - Photo (FileKit), Barcode (KScan), Attachment (FileKit)
- ✅ **Layout Components** - Row, Column, and Box layout containers for visual organization
- ✅ **Repeating Groups** - Dynamic add/remove of grouped items
- ✅ **Card-Based Summary** - Beautiful summary view with page organization
- ✅ **Theme Support** - Light and Dark themes with Material 3 design

### Planned Features

- 🔄 **Multi-language Support** - Remote translation loading with caching
- 🔄 **Media Widgets** - Photo, Barcode, Location, Signature widgets
- 🔄 **FHIR Conversion** - Bidirectional FHIR Questionnaire conversion

## Quick Start

### Installation

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

### Basic Usage

```kotlin
@Composable
@Suppress("functionName")
fun MyQuestionnaireScreen() {
    // Define a questionnaire
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

    // Initialize manager with evaluator
    val evaluator = remember { LiteQuestEvaluator(questionnaire) }
    val manager = remember { QuestionnaireManager(questionnaire, evaluator) }
    val state by manager.state.collectAsState()
    
    // Mode switching between Edit and Summary
    var mode by remember { mutableStateOf(QuestionnaireMode.Edit) }

    // Render the questionnaire
    QuestionnaireScreen(
        type = QuestionnaireType.Single(questionnaire),
        state = state,
        mode = mode,
        onAnswerChange = { linkId, value -> manager.updateAnswer(linkId, value) },
        onSubmit = { 
            // Handle form submission
            println("Form submitted: ${state.response}")
        },
        onModeChange = { newMode -> mode = newMode },
        onDismiss = { /* Handle dismiss */ }
    )
}

// For multipage questionnaires
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
    onAnswerChange = { linkId, value -> manager.updateAnswer(linkId, value) },
    onSubmit = { /* Handle submission */ },
    onDismiss = { /* Handle dismiss */ }
)
```



### Widget Types Mapping

| ItemType      | Widget               | Data Type    | Features                                            |
|---------------|----------------------|--------------|-----------------------------------------------------|
| STRING        | TextInputWidget      | String       | Single-line text input                              |
| TEXT          | TextInputWidget      | String       | Multi-line text area                                |
| BOOLEAN       | BooleanWidget        | Boolean      | Switch/Checkbox toggle                              |
| DECIMAL       | DecimalInputWidget   | Double       | Numeric keyboard with decimal support               |
| INTEGER       | IntegerInputWidget   | Int          | Numeric keyboard for whole numbers                  |
| DATE          | DatePickerWidget     | String (ISO) | Platform-native date selection                      |
| TIME          | TimePickerWidget     | String (ISO) | Platform-native time selection                      |
| DATETIME      | DateTimePickerWidget | String (ISO) | Combined date and time selection                    |
| CHOICE        | ChoiceWidget         | String(s)    | Radio buttons, Dropdowns, or Chips                  |
| OPEN_CHOICE   | OpenChoiceWidget     | String(s)    | Choice with "Other" free-text option                |
| DISPLAY       | DisplayWidget        | N/A          | Static text or instructional content                |
| GROUP         | GroupWidget          | N/A          | logical grouping of items, supports repetition      |
| QUANTITY      | QuantityWidget       | Object       | Numeric value with associated unit                  |
| REFERENCE     | ReferenceWidget      | Object       | Searchable reference to external entities           |
| BARCODE       | BarcodeScannerWidget | String       | Integrated camera barcode scanning (KScan)          |
| PHOTO         | PhotoSelectorWidget  | File/Base64  | Image capture or gallery selection (FileKit)        |
| ATTACHMENT    | AttachmentWidget     | File/Base64  | Generic file attachment support (FileKit)           |
| LAYOUT_ROW    | RowLayoutWidget      | N/A          | Horizontal arrangement of child widgets             |
| LAYOUT_COLUMN | ColumnLayoutWidget   | N/A          | Vertical arrangement of child widgets               |
| LAYOUT_BOX    | BoxLayoutWidget      | N/A          | Stacked or layered arrangement of child widgets     |

---

## Pagination System

Multipage questionnaires are supported through `PaginatedQuestionnaire` which organizes items into logical pages. The `PageNavigator` handles page transitions with validation, while the UI displays progress indicators and Previous/Next/Submit actions based on the current page.

---

## Theme Support

The application supports both **Light** and **Dark** themes using Material 3 design system. Theme selection adapts to the system preference automatically, ensuring consistent and accessible UI across all form components.

---

## Extension & Customization

### Custom Widget Example

```kotlin
// 1. Create custom widget
class RatingWidget(override val item: Item) : ItemWidget {
    @Composable
    override fun Render(value: JsonElement?, onValueChange: (JsonElement) -> Unit, 
                       isError: Boolean, config: WidgetConfig) {
        val rating = value?.jsonPrimitive?.intOrNull ?: 0
        Row {
            repeat(5) { index ->
                Icon(
                    if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                    modifier = Modifier.clickable { onValueChange(JsonPrimitive(index + 1)) },
                    contentDescription = null
                )
            }
        }
    }
}

// 2. Register it
val factory = DefaultWidgetFactory().apply {
    registerWidget(ItemType.RATING) { RatingWidget(it) }
}
```

### Override Existing Widget

```kotlin
class CustomTextWidget(override val item: Item) : ItemWidget {
    @Composable
    override fun Render( value: JsonElement?,
                         onValueChange: (JsonElement) -> Unit,
                         errorMessage: String? = null) {
        // Custom implementation
    }
}

val factory = DefaultWidgetFactory().apply {
    registerWidget(ItemType.TEXT) { CustomTextWidget(it) }
}
```

### Custom Layout Strategy

```kotlin
class MasonryLayoutStrategy : LayoutStrategy {
    @Composable
    override fun Layout(items: List<Item>, widgets: Map<String, ItemWidget>,
                       config: LayoutConfig) {
        // Staggered grid implementation
    }
}

val config = RenderConfig(layout = LayoutConfig(strategy = MasonryLayoutStrategy()))
```

---

## Other Usage Examples

### Edit and Summary Modes

```kotlin
// Using the unified QuestionnaireScreen with mode switching
var mode by remember { mutableStateOf(QuestionnaireMode.Edit) }

QuestionnaireScreen(
    type = QuestionnaireType.Single(questionnaire),
    state = questionnaireState,
    mode = mode,
    onAnswerChange = { linkId, value -> viewModel.updateAnswer(linkId, value) },
    onSubmit = { viewModel.submit() },
    onModeChange = { newMode -> mode = newMode },
    onDismiss = { /* handle dismiss */ }
)

// For paginated questionnaires
QuestionnaireScreen(
    type = QuestionnaireType.Paginated(paginatedQuestionnaire),
    state = questionnaireState,
    mode = QuestionnaireMode.Edit,
    onAnswerChange = { linkId, value -> viewModel.updateAnswer(linkId, value) },
    onSubmit = { viewModel.submit() },
    onDismiss = { /* handle dismiss */ }
)
```

### Custom Evaluation Logic

```kotlin
// Extend JsonLogicEvaluator to provide custom evaluation logic
class CustomJsonLogicEvaluator : JsonLogicEvaluator() {
    
    override fun evaluate(
        logic: JsonElement,
        data: Map<String, Any?>
    ): JsonElement {
        // Add custom operators or override existing logic
        // For example, add a custom "contains" operator
        if (logic is JsonObject && logic.containsKey("custom_contains")) {
             TODO("Your custom logic here")
        }
        
        // Delegate to parent for standard JsonLogic operations
        return super.evaluate(logic, data)
    }
}

// Pass custom evaluator to LiteQuestEvaluator
val customJsonLogicEvaluator = CustomJsonLogicEvaluator()
val evaluator = LiteQuestEvaluator(questionnaire, customJsonLogicEvaluator)
val manager = QuestionnaireManager(questionnaire, evaluator)
```

### Custom Widget

```kotlin
// Create a custom widget for a specific item type
@Composable
@Suppress("functionName")
fun RatingWidget(
  item: Item,
  value: JsonElement?,
  onValueChange: (JsonElement) -> Unit,
  isError: Boolean
) {
  val rating = value?.jsonPrimitive?.intOrNull ?: 0

  Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
    repeat(5) { index ->
      Icon(
        imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
        contentDescription = "Star ${index + 1}",
        modifier = Modifier
          .size(32.dp)
          .clickable { onValueChange(JsonPrimitive(index + 1)) },
        tint = if (index < rating) Color(0xFFFFB300) else Color.Gray
      )
    }
  }
}

// Use custom widget in FormRenderer by checking item type or extension
when (item.type) {
  ItemType.INTEGER -> {
    if (item.linkId == "satisfaction-rating") {
      RatingWidget(
        item = item,
        value = state.response.items.find { it.linkId == item.linkId }?.answer,
        onValueChange = { value -> onAnswerChange(item.linkId, value) },
        isError = false
      )
    } else {
      // Default integer widget
    }
  }
  // ... other types
}
```

---


## Running the Demo

The project includes a Compose Multiplatform demo app showcasing library features.

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

All dynamic behavior is expressed using JsonLogic, a JSON-based expression language:

```kotlin
// Visibility condition
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

// Calculated value
CalculatedValue(
    name = "bmi",
    expression = buildJsonObject {
        put("/", buildJsonObject {
            put("0", buildJsonObject { put("var", "weight") })
            put("1", buildJsonObject {
                put("*", buildJsonObject {
                    put("0", buildJsonObject { put("var", "height") })
                    put("1", buildJsonObject { put("var", "height") })
                })
            })
        })
    }
)

// Dynamic Form References (Autocomplete search against Host App's domain)
Item(
    linkId = "supplier_id",
    type = ItemType.REFERENCE,
    text = "Select Existing Supplier",
    extension = buildJsonObject {
        put("referenceType", JsonPrimitive("Supplier"))
    }
)
```

### Reactive State Management

State updates propagate automatically:

```txt
Answer Change → Recalculate Values → Update Visibility → Revalidate → Emit New State
```

### Multi-language Support

Decouple translations from questionnaire definitions:

```kotlin
val translationManager = TranslationManager(
    loader = TranslationLoader(),
    cache = TranslationCache()
)

// Load translations on demand
translationManager.loadTranslation("en", "https://example.com/translations/en.json")
```

## Development

### Project Structure

```txt
lite-quest/
├── library/          # Core KMP library
│   ├── src/
│   │   ├── commonMain/
│   │   ├── commonTest/
│   │   ├── androidMain/
│   │   ├── desktopMain/
│   │   ├── iosMain/
│   │   └── wasmJsMain/
│   └── build.gradle.kts
├── demo/             # Compose Multiplatform demo app
│   └── src/
└── settings.gradle.kts
```

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

## Platform Support

| Platform   | Status          | Min Version          |
|------------|-----------------|----------------------|
| Android    | ✅ Stable        | API 24 (Android 7.0) |
| iOS        | ✅ Stable        | iOS 14.0+            |
| Desktop    | ✅ Stable        | JVM 11+              |
| Web (WASM) | ⚠️ Experimental | Modern browsers      |

## Dependencies

- Kotlin 2.0.21+
- kotlinx-serialization 1.7.3+
- kotlinx-coroutines 1.9.0+
- kotlinx-datetime 0.6.1+
- ktor-client 3.0.1+

See [gradle/libs.versions.toml](gradle/libs.versions.toml) for complete dependency list.

## Documentation

- [LiteQuest Technical Specification v1.0.0](docs/spec/LITEQUEST_TECHNICAL_SPECIFICATION_V1_0_0.md) - Core engine architecture and JsonLogic evaluation
- [Demo App](demo) - Working examples for all platforms

## Community

- 💬 [Discussions](https://github.com/litequest/lite-quest/discussions) - Ask questions and share ideas
- 🐛 [Issues](https://github.com/litequest/lite-quest/issues) - Report bugs and request features
- 📧 [Mailing List](mailto:dev@litequest.io) - Development announcements

## Roadmap

### Version 1.1

- Multi-language support with remote translation loading
- Advanced validation rules with custom validators

### Version 2.0

- Visual form builder/editor
- Form versioning and migration tools
- Advanced conditional logic builder

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
