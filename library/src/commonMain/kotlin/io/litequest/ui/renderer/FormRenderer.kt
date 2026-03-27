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
import androidx.compose.runtime.remember
import io.litequest.model.Item
import io.litequest.model.ResponseItem
import io.litequest.state.QuestionnaireState
import io.litequest.ui.layout.LayoutStrategy
import io.litequest.ui.layout.VerticalLayoutStrategy
import io.litequest.ui.validation.ValidationPresentation
import io.litequest.ui.widget.WidgetFactory
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

@Composable
fun FormRenderer(
  items: List<Item>,
  state: QuestionnaireState,
  onAnswerChange: (String, JsonElement, String?) -> Unit,
  touchedFieldIds: Set<String> = emptySet(),
  touchedFieldPaths: Set<String> = emptySet(),
  showAllValidationErrors: Boolean = false,
  submitAttemptedFieldIds: Set<String> = emptySet(),
  submitAttemptedFieldPaths: Set<String> = emptySet(),
  onRepetitionAdd: ((String) -> Unit)? = null,
  onRepetitionRemove: ((String, Int) -> Unit)? = null,
  onRepetitionFieldChange: ((String, Int, String, JsonElement, String?) -> Unit)? = null,
  widgetFactory: WidgetFactory,
  layoutStrategy: LayoutStrategy = VerticalLayoutStrategy,
) {
  // Build widgets from current visible items so nested visibility changes are reflected.
  val widgets =
    remember(items, widgetFactory) {
      items.associate { it.linkId to widgetFactory.createWidget(it) }
    }

  // Flatten response items + calculated values into a single values map.
  // Wrapped in remember so this only runs on actual state changes, not during scroll.
  val processedState =
    remember(state.response.items, state.calculatedValues) {
      val values = mutableMapOf<String, JsonElement?>()
      val repetitions = mutableMapOf<String, List<Map<String, JsonElement?>>>()
      flatten(state.response.items, values, repetitions)
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
      values to repetitions
    }

  val values = processedState.first
  val repetitions = processedState.second
  val visibleValidationErrors =
    remember(
      state.validationErrors,
      touchedFieldIds,
      touchedFieldPaths,
      showAllValidationErrors,
      submitAttemptedFieldIds,
      submitAttemptedFieldPaths,
    ) {
      ValidationPresentation.visibleValidationErrors(
        errors = state.validationErrors,
        touchedFieldIds = touchedFieldIds,
        touchedFieldPaths = touchedFieldPaths,
        showAllValidationErrors = showAllValidationErrors,
        submitAttemptedFieldIds = submitAttemptedFieldIds,
        submitAttemptedFieldPaths = submitAttemptedFieldPaths,
      )
    }
  val errorMessages =
    remember(visibleValidationErrors) {
      visibleValidationErrors.associate { it.linkId to it.message }
    }
  val pathErrorMessages =
    remember(visibleValidationErrors) {
      visibleValidationErrors.associate { it.path.joinToString(".") to it.message }
    }

  val formContext =
    remember(
      values,
      errorMessages,
      pathErrorMessages,
      widgetFactory,
      repetitions,
      onAnswerChange,
      onRepetitionAdd,
      onRepetitionRemove,
      onRepetitionFieldChange,
    ) {
      FormContext(
        values = values,
        onValueChange = onAnswerChange,
        errorMessages = errorMessages,
        pathErrorMessages = pathErrorMessages,
        widgetFactory = widgetFactory,
        repetitions = repetitions,
        onRepetitionAdd = onRepetitionAdd,
        onRepetitionRemove = onRepetitionRemove,
        onRepetitionFieldChange = onRepetitionFieldChange,
      )
    }

  CompositionLocalProvider(LocalFormContext provides formContext) {
    layoutStrategy.Layout(
      items = items,
      widgets = widgets,
      onValueChange = onAnswerChange,
      values = values,
      errorMessages = errorMessages,
    )
  }
}

private fun flatten(
  responseItems: List<ResponseItem>,
  values: MutableMap<String, JsonElement?>,
  repetitions: MutableMap<String, List<Map<String, JsonElement?>>>,
) {
  responseItems.forEach { responseItem ->
    if (responseItem.answers.isNotEmpty() && responseItem.answers.first().items.isNotEmpty()) {
      val repetitionList =
        responseItem.answers.map { answer ->
          val repetitionValues = mutableMapOf<String, JsonElement?>()
          flattenForRepetition(answer.items, repetitionValues)
          repetitionValues
        }
      repetitions[responseItem.linkId] = repetitionList
    } else {
      values[responseItem.linkId] = responseItem.answers.firstOrNull()?.value
    }
    if (responseItem.items.isNotEmpty()) {
      flatten(responseItem.items, values, repetitions)
    }
  }
}

private fun flattenForRepetition(
  responseItems: List<ResponseItem>,
  values: MutableMap<String, JsonElement?>,
) {
  responseItems.forEach { responseItem ->
    values[responseItem.linkId] = responseItem.answers.firstOrNull()?.value
    if (responseItem.items.isNotEmpty()) {
      flattenForRepetition(responseItem.items, values)
    }
  }
}
