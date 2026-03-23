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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.litequest.model.Item
import io.litequest.ui.renderer.LocalFormContext
import io.litequest.ui.widget.ItemWidget
import kotlinx.serialization.json.JsonElement

class ColumnLayoutWidget(override val item: Item) : ItemWidget {
  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement, String?) -> Unit,
    errorMessage: String?,
  ) {
    val context = LocalFormContext.current

    val widgetCache = remember(context.widgetFactory) { mutableMapOf<String, ItemWidget>() }
    val childWidgets =
      remember(item.items, widgetCache) {
        item.items.associateWith { childItem ->
          widgetCache.getOrPut(childItem.linkId) { context.widgetFactory.createWidget(childItem) }
        }
      }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      childWidgets.forEach { (childItem, childWidget) ->
        key(childItem.linkId) {
          childWidget.Render(
            value = context.values[childItem.linkId],
            onValueChange = { value, text -> context.onValueChange(childItem.linkId, value, text) },
            errorMessage = context.errorMessages[childItem.linkId],
          )
        }
      }
    }
  }
}
