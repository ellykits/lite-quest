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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import io.litequest.model.Item
import io.litequest.ui.renderer.LocalFormContext
import io.litequest.ui.widget.ItemWidget
import kotlinx.serialization.json.JsonElement

class GroupWidget(override val item: Item) : ItemWidget {
  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement, String?) -> Unit,
    errorMessage: String?,
  ) {
    val context = LocalFormContext.current
    val nestedContext = context.copy(pathPrefix = context.childPath(item.linkId))
    var expanded by rememberSaveable { mutableStateOf(true) }
    val rotationAngle by animateFloatAsState(if (expanded) 180f else 0f)

    val childWidgets =
      item.items.associateWith { childItem -> context.widgetFactory.createWidget(childItem) }

    OutlinedCard(
      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
      shape = MaterialTheme.shapes.medium,
    ) {
      Column {
        if (item.text.isNotEmpty()) {
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = item.text,
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f),
            )
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = if (expanded) "Collapse" else "Expand",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.rotate(rotationAngle),
            )
          }
        }

        if (expanded) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            childWidgets.forEach { (childItem, childWidget) ->
              if (!nestedContext.isChildVisible(childItem.linkId)) {
                return@forEach
              }
              key(childItem.linkId) {
                CompositionLocalProvider(LocalFormContext provides nestedContext) {
                  childWidget.Render(
                    value = context.values[childItem.linkId],
                    onValueChange = { newValue, text ->
                      context.onValueChange(childItem.linkId, newValue, text)
                    },
                    errorMessage = context.errorMessages[childItem.linkId],
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
