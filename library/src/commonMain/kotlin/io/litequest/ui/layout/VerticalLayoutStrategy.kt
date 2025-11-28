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
package io.litequest.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget
import kotlinx.serialization.json.JsonElement

class VerticalLayoutStrategy : LayoutStrategy {
  @Composable
  override fun Layout(
    items: List<Item>,
    widgets: Map<String, ItemWidget>,
    onValueChange: (String, JsonElement) -> Unit,
    values: Map<String, JsonElement?>,
    errorMessages: Map<String, String>,
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      items(items) { item ->
        widgets[item.linkId]?.let { widget ->
          widget.Render(
            value = values[item.linkId],
            onValueChange = { value -> onValueChange(item.linkId, value) },
            errorMessage = errorMessages[item.linkId],
          )
        }
      }
    }
  }
}
