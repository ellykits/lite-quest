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
package io.litequest.ui.renderer

import androidx.compose.runtime.Composable
import io.litequest.model.Item
import io.litequest.model.ItemType
import io.litequest.state.QuestionnaireState
import io.litequest.ui.layout.LayoutStrategy
import io.litequest.ui.layout.VerticalLayoutStrategy
import io.litequest.ui.widget.DefaultWidgetFactory
import io.litequest.ui.widget.WidgetFactory
import io.litequest.ui.widget.group.RepeatingGroupWidget
import kotlinx.serialization.json.JsonElement

@Composable
fun FormRenderer(
  items: List<Item>,
  state: QuestionnaireState,
  onAnswerChange: (String, JsonElement) -> Unit,
  widgetFactory: WidgetFactory = DefaultWidgetFactory(),
  layoutStrategy: LayoutStrategy = VerticalLayoutStrategy(),
) {
  val values =
    state.response.items.associate { responseItem ->
      responseItem.linkId to responseItem.answers.firstOrNull()?.value
    }

  val errorMessages = state.validationErrors.associate { error -> error.linkId to error.message }

  val widgets =
    items.associate { item ->
      item.linkId to
        when {
          item.type == ItemType.GROUP && item.repeats -> {
            RepeatingGroupWidget(
              item = item,
              widgetFactory = widgetFactory,
              onValueChange = onAnswerChange,
              values = values,
              errorMessages = errorMessages,
            )
          }
          else -> widgetFactory.createWidget(item)
        }
    }

  layoutStrategy.Layout(
    items = items,
    widgets = widgets,
    onValueChange = onAnswerChange,
    values = values,
    errorMessages = errorMessages,
  )
}
