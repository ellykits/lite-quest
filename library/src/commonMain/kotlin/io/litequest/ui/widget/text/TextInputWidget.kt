package io.litequest.ui.widget.text

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

class TextInputWidget(override val item: Item) : ItemWidget {
  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement) -> Unit,
    errorMessage: String?,
  ) {
    val text = value?.jsonPrimitive?.content ?: ""

    OutlinedTextField(
      value = text,
      onValueChange = { newValue ->
        if (newValue.isEmpty()) {
          onValueChange(JsonNull)
        } else {
          onValueChange(JsonPrimitive(newValue))
        }
      },
      label = { Text(item.text) },
      isError = errorMessage != null,
      supportingText = errorMessage?.let { { Text(it) } },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
    )
  }
}
