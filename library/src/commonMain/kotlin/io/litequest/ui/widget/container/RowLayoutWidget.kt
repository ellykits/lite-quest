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
package io.litequest.ui.widget.container

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget
import io.litequest.ui.widget.WidgetFactory
import kotlinx.serialization.json.JsonElement

class RowLayoutWidget(
  override val item: Item,
  private val widgetFactory: WidgetFactory,
  private val onValueChange: (String, JsonElement) -> Unit,
  private val values: Map<String, JsonElement?>,
  private val errorMessages: Map<String, String>,
) : ItemWidget {
  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement) -> Unit,
    errorMessage: String?,
  ) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      item.items.forEach { childItem ->
        val childWidget = widgetFactory.createWidget(childItem)
        childWidget.Render(
          value = values[childItem.linkId],
          onValueChange = { newValue ->
            this@RowLayoutWidget.onValueChange(childItem.linkId, newValue)
          },
          errorMessage = errorMessages[childItem.linkId],
        )
      }
    }
  }
}
