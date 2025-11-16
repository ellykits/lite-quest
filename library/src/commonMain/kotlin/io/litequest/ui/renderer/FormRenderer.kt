package io.litequest.ui.renderer

import androidx.compose.runtime.Composable
import io.litequest.model.Item
import io.litequest.model.ItemType
import io.litequest.state.QuestionnaireState
import io.litequest.ui.layout.LayoutStrategy
import io.litequest.ui.layout.VerticalLayoutStrategy
import io.litequest.ui.widget.DefaultWidgetFactory
import io.litequest.ui.widget.WidgetFactory
import io.litequest.ui.widget.group.RepeatingGroupWidget
import kotlinx.serialization.json.JsonElement

@Composable
fun FormRenderer(
  items: List<Item>,
  state: QuestionnaireState,
  onAnswerChange: (String, JsonElement) -> Unit,
  widgetFactory: WidgetFactory = DefaultWidgetFactory(),
  layoutStrategy: LayoutStrategy = VerticalLayoutStrategy(),
) {
  val values =
    state.response.items.associate { responseItem ->
      responseItem.linkId to responseItem.answers.firstOrNull()?.value
    }

  val errorMessages = state.validationErrors.associate { error -> error.linkId to error.message }

  val widgets =
    items.associate { item ->
      item.linkId to
        when {
          item.type == ItemType.GROUP && item.repeats -> {
            RepeatingGroupWidget(
              item = item,
              widgetFactory = widgetFactory,
              onValueChange = onAnswerChange,
              values = values,
              errorMessages = errorMessages,
            )
          }
          else -> widgetFactory.createWidget(item)
        }
    }

  layoutStrategy.Layout(
    items = items,
    widgets = widgets,
    onValueChange = onAnswerChange,
    values = values,
    errorMessages = errorMessages,
  )
}
