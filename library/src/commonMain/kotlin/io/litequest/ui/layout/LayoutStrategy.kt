package io.litequest.ui.layout

import androidx.compose.runtime.Composable
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget

interface LayoutStrategy {
  @Composable
  fun Layout(
    items: List<Item>,
    widgets: Map<String, ItemWidget>,
    onValueChange: (String, kotlinx.serialization.json.JsonElement) -> Unit,
    values: Map<String, kotlinx.serialization.json.JsonElement?>,
    errorMessages: Map<String, String>,
  )
}
