# LiteQuest Form Visualizer

## Technical Specification v1.0.0

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Widget System](#widget-system)
4. [Pagination System](#pagination-system)
5. [Theme Support](#theme-support)
6. [Extension & Customization](#extension--customization)
7. [Usage Examples](#usage-examples)

---

## Overview

### Goals

- **Modular**: Widget-based architecture allowing easy replacement and extension
- **Flexible Layouts**: Support for various positioning strategies (flow, grid, rows, columns)
- **User-Friendly**: Review page with clear question/answer presentation
- **Themeable**: Dark/light mode support with customizable styling
- **Paginated**: Multi-page forms with navigation and progress tracking
- **Extensible**: Easy to add custom widgets and override existing ones

### Core Features

#### 1. Form Visualizer

- Renders questionnaires using pluggable widget factories
- Supports multiple layout strategies
- Handles nested/grouped items with collapsible sections
- Real-time validation feedback
- Responsive design across platforms

#### 2. Review/Summary Page

- Read-only view of all answers
- Grouped by sections
- Two actions: **Edit** (return to form) and **Submit** (finalize)
- Clear question → answer mapping
- Highlights unanswered required fields

---

## Architecture

The library uses a modular architecture with the following key components:

- **QuestionnaireScreen**: Main unified component supporting both Edit and Summary modes, handling single-page and multi-page questionnaires through `QuestionnaireType` sealed class
- **FormRenderer**: Renders form items using a widget-based system with support for nested groups and dynamic visibility
- **SummaryPage**: Card-based summary view with page grouping for multi-page questionnaires
- **QuestionnaireManager**: Manages state, answer updates, and integrates with the evaluator for dynamic form behavior
- **LiteQuestEvaluator**: Processes enableWhen conditions and calculated expressions using JsonLogic

---

## Widget System

### Widget Types Mapping

| ItemType  | Widget               | Input Type   | Features                           |
|-----------|----------------------|--------------|------------------------------------|
| TEXT      | TextInputWidget      | String       | Single/multi-line, validation      |
| DECIMAL   | DecimalInputWidget   | Double       | Numeric keyboard, range validation |
| INTEGER   | IntegerInputWidget   | Int          | Numeric keyboard                   |
| BOOLEAN   | BooleanWidget        | Boolean      | Switch/Checkbox                    |
| CHOICE    | ChoiceWidget         | String(s)    | Radio/Dropdown/Chips               |
| DATE      | DatePickerWidget     | String (ISO) | Date picker                        |
| TIME      | TimePickerWidget     | String       | Time picker                        |
| DISPLAY   | DisplayWidget        | N/A          | Read-only info                     |
| GROUP     | GroupWidget          | N/A          | Collapsible container              |
| BARCODE   | BarcodeScannerWidget | String       | Camera-based barcode scanning (Not yet implemented)     |
| PHOTO     | PhotoSelectorWidget  | File/Base64  | Camera or gallery photo selection (Not yet implemented) |
| LOCATION  | LocationWidget       | GeoJSON      | GPS location capture (Not yet implemented)              |
| SIGNATURE | SignatureWidget      | Base64/SVG   | Touch signature pad (Not yet implemented)               |

---

## Pagination System

Multi-page questionnaires are supported through `PaginatedQuestionnaire` which organizes items into logical pages. The `PageNavigator` handles page transitions with validation, while the UI displays progress indicators and Previous/Next/Submit actions based on the current page.

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
    override fun Render(...) {
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

## Usage Examples

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
            // Your custom logic here
            // ...
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

## Conclusion

LiteQuest provides a flexible, modular system for rendering FHIR-based questionnaires with support for dynamic behavior, pagination, and customization. The unified API through `QuestionnaireScreen` simplifies implementation while maintaining extensibility for custom widgets and evaluation logic.
