package io.litequest.ui.widget

import androidx.compose.runtime.Composable
import io.litequest.model.Item
import kotlinx.serialization.json.JsonElement

interface ItemWidget {
  val item: Item

  @Composable
  fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement) -> Unit,
    errorMessage: String? = null,
  )
}
