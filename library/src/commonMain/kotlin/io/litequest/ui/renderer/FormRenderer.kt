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
import androidx.compose.runtime.CompositionLocalProvider
import io.litequest.model.Item
import io.litequest.model.ResponseItem
import io.litequest.state.QuestionnaireState
import io.litequest.ui.layout.LayoutStrategy
import io.litequest.ui.layout.VerticalLayoutStrategy
import io.litequest.ui.widget.DefaultWidgetFactory
import io.litequest.ui.widget.WidgetFactory
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

@Composable
fun FormRenderer(
  items: List<Item>,
  state: QuestionnaireState,
  onAnswerChange: (String, JsonElement, String?) -> Unit,
  onRepetitionAdd: ((String) -> Unit)? = null,
  onRepetitionRemove: ((String, Int) -> Unit)? = null,
  onRepetitionFieldChange: ((String, Int, String, JsonElement, String?) -> Unit)? = null,
  widgetFactory: WidgetFactory? = null,
  layoutStrategy: LayoutStrategy = VerticalLayoutStrategy(),
) {
  val factory = widgetFactory ?: DefaultWidgetFactory()

  val values = mutableMapOf<String, JsonElement?>()
  val repetitions = mutableMapOf<String, List<Map<String, JsonElement?>>>()

  fun flattenResponseItems(responseItems: List<ResponseItem>) {
    responseItems.forEach { responseItem ->
      if (responseItem.answers.isNotEmpty() && responseItem.answers.first().items.isNotEmpty()) {
        val repetitionList =
          responseItem.answers.map { answer ->
            val repetitionValues = mutableMapOf<String, JsonElement?>()
            answer.items.forEach { childItem ->
              repetitionValues[childItem.linkId] = childItem.answers.firstOrNull()?.value
            }
            repetitionValues
          }
        repetitions[responseItem.linkId] = repetitionList
      } else {
        values[responseItem.linkId] = responseItem.answers.firstOrNull()?.value
      }

      if (responseItem.items.isNotEmpty()) {
        flattenResponseItems(responseItem.items)
      }
    }
  }

  flattenResponseItems(state.response.items)

  state.calculatedValues.forEach { (linkId, value) ->
    values[linkId] =
      when (value) {
        null -> null
        is Number -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is String -> JsonPrimitive(value)
        else -> JsonPrimitive(value.toString())
      }
  }

  val errorMessages = state.validationErrors.associate { error -> error.linkId to error.message }
  val widgets = items.associate { item -> item.linkId to factory.createWidget(item) }

  CompositionLocalProvider(
    LocalFormContext provides
      FormContext(
        values = values,
        onValueChange = onAnswerChange,
        errorMessages = errorMessages,
        widgetFactory = factory,
        repetitions = repetitions,
        onRepetitionAdd = onRepetitionAdd,
        onRepetitionRemove = onRepetitionRemove,
        onRepetitionFieldChange = onRepetitionFieldChange,
      )
  ) {
    layoutStrategy.Layout(
      items = items,
      widgets = widgets,
      onValueChange = onAnswerChange,
      values = values,
      errorMessages = errorMessages,
    )
  }
}
