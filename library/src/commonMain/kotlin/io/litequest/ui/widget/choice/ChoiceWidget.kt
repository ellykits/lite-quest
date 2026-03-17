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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.litequest.model.AnswerOption
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class ChoiceWidget(override val item: Item) : ItemWidget {
  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement, String?) -> Unit,
    errorMessage: String?,
  ) {
    if (item.repeats) {
      val selectedCodes =
        runCatching { value?.jsonArray?.map { it.jsonPrimitive.content }?.toSet() ?: emptySet() }
          .getOrDefault(emptySet())

      Column(modifier = Modifier.fillMaxWidth()) {
        WidgetLabel(item.text, errorMessage != null)
        CheckboxGroup(
          options = item.answerOptions,
          selectedCodes = selectedCodes,
          onToggle = { code ->
            val updated = handleExclusiveToggle(code, selectedCodes, item.answerOptions)
            val text = getDisplayText(updated, item.answerOptions)
            onValueChange(JsonArray(updated.map { JsonPrimitive(it) }), text.ifEmpty { null })
          },
        )
        ErrorLabel(errorMessage)
      }
    } else {
      val selectedCode = value?.jsonPrimitive?.content ?: ""

      Column(modifier = Modifier.fillMaxWidth()) {
        WidgetLabel(item.text, errorMessage != null)

        if (item.answerOptions.size < 5) {
          RadioGroup(
            options = item.answerOptions,
            selectedCode = selectedCode,
            onSelected = { code ->
              val text = item.answerOptions.find { it.code == code }?.display
              onValueChange(JsonPrimitive(code), text)
            },
            enabled = !item.readOnly,
          )
        } else {
          DropdownChoice(
            options = item.answerOptions,
            selectedCode = selectedCode,
            onSelected = { code ->
              val text = item.answerOptions.find { it.code == code }?.display
              onValueChange(JsonPrimitive(code), text)
            },
            isError = errorMessage != null,
            readOnly = item.readOnly,
          )
        }
        ErrorLabel(errorMessage)
      }
    }
  }

  @Composable
  private fun WidgetLabel(text: String, isError: Boolean) {
    if (text.isEmpty()) return
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(bottom = 8.dp),
    )
  }

  @Composable
  private fun ErrorLabel(errorMessage: String?) {
    if (errorMessage != null) {
      Text(
        text = errorMessage,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 4.dp, start = 16.dp),
      )
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
        val isChecked = selectedCodes.contains(option.code)
        val bgColor =
          if (isChecked) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
          } else {
            Color.Transparent
          }
        val borderColor =
          if (isChecked) {
            MaterialTheme.colorScheme.primary
          } else {
            MaterialTheme.colorScheme.outlineVariant
          }

        Row(
          modifier =
            Modifier.fillMaxWidth()
              .border(width = 1.dp, color = borderColor, shape = MaterialTheme.shapes.small)
              .background(color = bgColor, shape = MaterialTheme.shapes.small)
              .clickable { onToggle(option.code) }
              .padding(horizontal = 4.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Checkbox(checked = isChecked, onCheckedChange = { onToggle(option.code) })
          Text(
            text = option.display,
            style = MaterialTheme.typography.bodyLarge,
            color =
              if (isChecked) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurface
              },
            modifier = Modifier.padding(start = 4.dp),
          )
        }
      }
    }
  }

  @Composable
  private fun RadioGroup(
    options: List<AnswerOption>,
    selectedCode: String,
    onSelected: (String) -> Unit,
    enabled: Boolean = true,
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      options.forEach { option ->
        val isSelected = option.code == selectedCode
        val bgColor =
          if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
          } else {
            Color.Transparent
          }
        val borderColor =
          if (isSelected) {
            MaterialTheme.colorScheme.primary
          } else {
            MaterialTheme.colorScheme.outlineVariant
          }

        Row(
          modifier =
            Modifier.fillMaxWidth()
              .border(width = 1.dp, color = borderColor, shape = MaterialTheme.shapes.small)
              .background(color = bgColor, shape = MaterialTheme.shapes.small)
              .clickable(enabled = enabled) { onSelected(option.code) }
              .padding(horizontal = 12.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          RadioButton(
            selected = isSelected,
            onClick = { onSelected(option.code) },
            enabled = enabled,
          )
          Text(
            text = option.display,
            style = MaterialTheme.typography.bodyLarge,
            color =
              if (isSelected) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurface
              },
            modifier = Modifier.padding(start = 4.dp),
          )
        }
      }
    }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  private fun DropdownChoice(
    options: List<AnswerOption>,
    selectedCode: String,
    onSelected: (String) -> Unit,
    isError: Boolean,
    readOnly: Boolean = false,
  ) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = options.find { it.code == selectedCode }

    ExposedDropdownMenuBox(
      expanded = expanded && !readOnly,
      onExpandedChange = { if (!readOnly) expanded = !expanded },
      modifier = Modifier.fillMaxWidth(),
    ) {
      OutlinedTextField(
        value = selectedOption?.display ?: "",
        onValueChange = {},
        readOnly = true,
        enabled = !readOnly,
        isError = isError,
        trailingIcon = {
          if (!readOnly) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        },
        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        modifier =
          Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
      )

      ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { option ->
          DropdownMenuItem(
            text = { Text(option.display) },
            onClick = {
              onSelected(option.code)
              expanded = false
            },
          )
        }
      }
    }
  }
}
