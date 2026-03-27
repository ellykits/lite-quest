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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import io.litequest.model.Item
import io.litequest.ui.renderer.LocalFormContext
import io.litequest.ui.widget.ItemWidget
import kotlinx.serialization.json.JsonElement

class BoxLayoutWidget(override val item: Item) : ItemWidget {
  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement, String?) -> Unit,
    errorMessage: String?,
  ) {
    val context = LocalFormContext.current
    val nestedContext = context.copy(pathPrefix = context.childPath(item.linkId))

    val childWidgets =
      item.items.associateWith { childItem -> context.widgetFactory.createWidget(childItem) }

    Box(modifier = Modifier.fillMaxWidth()) {
      childWidgets.forEach { (childItem, childWidget) ->
        if (!nestedContext.isChildVisible(childItem.linkId)) {
          return@forEach
        }
        key(childItem.linkId) {
          CompositionLocalProvider(LocalFormContext provides nestedContext) {
            childWidget.Render(
              value = context.values[childItem.linkId],
              onValueChange = { value, text ->
                context.onValueChange(childItem.linkId, value, text)
              },
              errorMessage = context.errorMessages[childItem.linkId],
            )
          }
        }
      }
    }
  }
}
