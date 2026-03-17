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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.litequest.model.Item
import io.litequest.ui.renderer.LocalFormContext
import io.litequest.ui.widget.ItemWidget
import kotlinx.serialization.json.JsonElement

class RepeatingGroupWidget(override val item: Item) : ItemWidget {

  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement, String?) -> Unit,
    errorMessage: String?,
  ) {
    val context = LocalFormContext.current
    val repetitions = context.repetitions[item.linkId] ?: emptyList()

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      if (item.text.isNotEmpty()) {
        Text(
          text = item.text,
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.padding(bottom = 4.dp),
        )
      }

      repetitions.forEachIndexed { index, repetitionValues ->
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = Color.Transparent,
          shape = MaterialTheme.shapes.large,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
              if (repetitions.size > 1) {
                OutlinedButton(
                  onClick = { context.onRepetitionRemove?.invoke(item.linkId, index) },
                  modifier = Modifier.height(32.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                  Text(
                    "Remove",
                    style = MaterialTheme.typography.labelSmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                  )
                }
              }
            }

            item.items.forEach { childItem ->
              val childWidget = context.widgetFactory.createWidget(childItem)
              childWidget.Render(
                value = repetitionValues[childItem.linkId],
                onValueChange = { newValue, text ->
                  context.onRepetitionFieldChange?.invoke(
                    item.linkId,
                    index,
                    childItem.linkId,
                    newValue,
                    text,
                  )
                },
                errorMessage = null,
              )
            }
          }
        }
      }

      Button(
        onClick = { context.onRepetitionAdd?.invoke(item.linkId) },
        modifier = Modifier.wrapContentWidth().align(Alignment.CenterHorizontally),
      ) {
        Text("+ Add ${item.text}")
      }
    }
  }
}
