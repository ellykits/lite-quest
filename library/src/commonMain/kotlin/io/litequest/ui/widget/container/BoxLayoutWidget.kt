package io.litequest.ui.widget.container

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget
import io.litequest.ui.widget.WidgetFactory
import kotlinx.serialization.json.JsonElement

class BoxLayoutWidget(
  override val item: Item,
  private val widgetFactory: WidgetFactory,
  private val onValueChange: (String, JsonElement) -> Unit,
  private val values: Map<String, JsonElement?>,
  private val errorMessages: Map<String, String>,
) : ItemWidget {
  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement) -> Unit,
    errorMessage: String?,
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      item.items.forEach { childItem ->
        val childWidget = widgetFactory.createWidget(childItem)
        childWidget.Render(
          value = values[childItem.linkId],
          onValueChange = { newValue ->
            this@BoxLayoutWidget.onValueChange(childItem.linkId, newValue)
          },
          errorMessage = errorMessages[childItem.linkId],
        )
      }
    }
  }
}
