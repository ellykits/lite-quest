/*
* Copyright 2025 LiteQuest Contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.litequest.ui.widget.numeric

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

class IntegerInputWidget(override val item: Item) : ItemWidget {
  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement, String?) -> Unit,
    errorMessage: String?,
  ) {
    var localText by remember(value) { mutableStateOf(value?.jsonPrimitive?.content ?: "") }

    LaunchedEffect(localText) {
      if (localText != (value?.jsonPrimitive?.content ?: "")) {
        kotlinx.coroutines.delay(300)
        if (localText.isEmpty()) {
          onValueChange(JsonPrimitive(""), item.text)
        } else {
          localText.toIntOrNull()?.let { onValueChange(JsonPrimitive(it), item.text) }
        }
      }
    }

    OutlinedTextField(
      value = localText,
      onValueChange = { localText = it },
      label = { Text(item.text) },
      isError = errorMessage != null,
      supportingText = errorMessage?.let { { Text(it) } },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      enabled = !item.readOnly,
      readOnly = item.readOnly,
    )
  }
}
