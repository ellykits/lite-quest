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

import androidx.compose.runtime.Composable
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget

interface LayoutStrategy {
  @Composable
  fun Layout(
    items: List<Item>,
    widgets: Map<String, ItemWidget>,
    onValueChange: (String, kotlinx.serialization.json.JsonElement) -> Unit,
    values: Map<String, kotlinx.serialization.json.JsonElement?>,
    errorMessages: Map<String, String>,
  )
}
