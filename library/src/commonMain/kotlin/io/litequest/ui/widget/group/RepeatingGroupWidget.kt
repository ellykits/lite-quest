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
package io.litequest.ui.widget.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget
import io.litequest.ui.widget.WidgetFactory
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class RepeatingGroupWidget(
  override val item: Item,
  private val widgetFactory: WidgetFactory,
  private val onValueChange: (String, JsonElement) -> Unit,
  private val values: Map<String, JsonElement?>,
  private val errorMessages: Map<String, String>,
) : ItemWidget {

  private fun getGroupInstances(): List<Map<String, JsonElement?>> {
    val groupValue = values[item.linkId] as? JsonArray
    return groupValue?.map { jsonElement ->
      if (jsonElement is JsonObject) {
        jsonElement.mapValues { it.value }
      } else {
        emptyMap()
      }
    } ?: listOf(emptyMap())
  }

  private fun updateGroupValue(instanceIndex: Int, childLinkId: String, newValue: JsonElement) {
    val instances = getGroupInstances().toMutableList()

    while (instances.size <= instanceIndex) {
      instances.add(emptyMap())
    }

    val currentInstance = instances[instanceIndex].toMutableMap()
    currentInstance[childLinkId] = newValue
    instances[instanceIndex] = currentInstance

    val jsonArray = buildJsonArray {
      instances.forEach { instance ->
        add(buildJsonObject { instance.forEach { (key, value) -> put(key, value ?: JsonNull) } })
      }
    }

    onValueChange(item.linkId, jsonArray)
  }

  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement) -> Unit,
    errorMessage: String?,
  ) {
    val instances = getGroupInstances()
    var instanceCount by remember { mutableStateOf(instances.size.coerceAtLeast(1)) }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      if (item.text.isNotEmpty()) {
        Text(
          text = item.text,
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.padding(bottom = 4.dp),
        )
      }

      repeat(instanceCount) { index ->
        val instanceValues = if (index < instances.size) instances[index] else emptyMap()

        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = Color.Transparent,
          shape = MaterialTheme.shapes.large,
          border =
            androidx.compose.foundation.BorderStroke(
              1.dp,
              MaterialTheme.colorScheme.outlineVariant,
            ),
          shadowElevation = 0.dp,
          tonalElevation = 0.dp,
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Surface(
                  modifier = Modifier.size(24.dp),
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primary,
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = "${index + 1}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onPrimary,
                    )
                  }
                }
                Text(
                  text = item.text,
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
              if (instanceCount > 1) {
                OutlinedButton(
                  onClick = {
                    instanceCount--
                    removeInstance(index)
                  },
                  modifier = Modifier.height(32.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                  Text("Remove", style = MaterialTheme.typography.labelSmall)
                }
              }
            }

            item.items.forEach { childItem ->
              val childWidget = widgetFactory.createWidget(childItem)
              childWidget.Render(
                value = instanceValues[childItem.linkId],
                onValueChange = { newValue -> updateGroupValue(index, childItem.linkId, newValue) },
                errorMessage = errorMessages["${item.linkId}[$index].${childItem.linkId}"],
              )
            }
          }
        }
      }

      Button(onClick = { instanceCount++ }, modifier = Modifier.fillMaxWidth()) {
        Text("+ Add ${item.text}")
      }
    }
  }

  private fun removeInstance(indexToRemove: Int) {
    val instances = getGroupInstances().toMutableList()
    if (indexToRemove < instances.size) {
      instances.removeAt(indexToRemove)

      val jsonArray = buildJsonArray {
        instances.forEach { instance ->
          add(buildJsonObject { instance.forEach { (key, value) -> put(key, value ?: JsonNull) } })
        }
      }

      onValueChange(item.linkId, jsonArray)
    }
  }
}
