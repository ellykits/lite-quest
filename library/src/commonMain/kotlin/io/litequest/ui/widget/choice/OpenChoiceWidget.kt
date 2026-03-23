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
package io.litequest.ui.widget.choice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.litequest.model.AnswerOption
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/** Multi-select checkbox widget for OPEN_CHOICE items. */
class OpenChoiceWidget(override val item: Item) : ItemWidget {
  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement, String?) -> Unit,
    errorMessage: String?,
  ) {
    val selectedCodes: Set<String> =
      remember(value) {
        runCatching {
            when {
              value == null -> emptySet()
              value is JsonArray -> value.map { it.jsonPrimitive.content }.toSet()
              else -> setOf(value.jsonPrimitive.content)
            }
          }
          .getOrDefault(emptySet())
      }

    Column(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = item.text,
        style = MaterialTheme.typography.labelLarge,
        color =
          if (errorMessage != null) {
            MaterialTheme.colorScheme.error
          } else {
            MaterialTheme.colorScheme.onSurface
          },
        modifier = Modifier.padding(bottom = 8.dp),
      )

      CheckboxGroup(
        options = item.answerOptions,
        selectedCodes = selectedCodes,
        onToggle = { code ->
          val updatedCodes = handleExclusiveToggle(code, selectedCodes, item.answerOptions)
          val textInput = getDisplayText(updatedCodes, item.answerOptions)
          onValueChange(
            JsonArray(updatedCodes.map { JsonPrimitive(it) }),
            textInput.ifEmpty { null },
          )
        },
      )

      if (errorMessage != null) {
        Text(
          text = errorMessage,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(top = 4.dp, start = 16.dp),
        )
      }
    }
  }

  @Composable
  private fun CheckboxGroup(
    options: List<AnswerOption>,
    selectedCodes: Set<String>,
    onToggle: (String) -> Unit,
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      options.forEach { option ->
        androidx.compose.runtime.key(option.code) {
          val isChecked = selectedCodes.contains(option.code)
          val bgColor =
            if (isChecked) {
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            }

          Row(
            modifier =
              Modifier.fillMaxWidth()
                .background(color = bgColor, shape = MaterialTheme.shapes.small)
                .clickable { onToggle(option.code) }
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Checkbox(checked = isChecked, onCheckedChange = { onToggle(option.code) })
            Text(
              text = option.display,
              style = MaterialTheme.typography.bodyLarge,
              color =
                if (isChecked) {
                  MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
              modifier = Modifier.padding(start = 4.dp),
            )
          }
        }
      }
    }
  }
}
