package io.litequest.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget
import kotlinx.serialization.json.JsonElement

class VerticalLayoutStrategy : LayoutStrategy {
  @Composable
  override fun Layout(
    items: List<Item>,
    widgets: Map<String, ItemWidget>,
    onValueChange: (String, JsonElement) -> Unit,
    values: Map<String, JsonElement?>,
    errorMessages: Map<String, String>,
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars),
      contentPadding =
        PaddingValues(
          horizontal = 16.dp,
          vertical = 16.dp,
        ),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      items(items) { item ->
        widgets[item.linkId]?.let { widget ->
          widget.Render(
            value = values[item.linkId],
            onValueChange = { value -> onValueChange(item.linkId, value) },
            errorMessage = errorMessages[item.linkId],
          )
        }
      }
    }
  }
}
