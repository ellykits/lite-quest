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
package io.litequest.ui.widget.display

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget
import kotlinx.serialization.json.JsonElement

class DisplayWidget(override val item: Item) : ItemWidget {
  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement, String?) -> Unit,
    errorMessage: String?,
  ) {
    Text(
      text = item.text,
      modifier = Modifier.padding(bottom = 8.dp),
      style = MaterialTheme.typography.bodyLarge,
    )
  }
}
