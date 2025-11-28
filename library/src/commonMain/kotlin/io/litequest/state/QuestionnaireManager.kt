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
package io.litequest.state

import io.litequest.engine.LiteQuestEvaluator
import io.litequest.i18n.TranslationManager
import io.litequest.model.Answer
import io.litequest.model.Item
import io.litequest.model.Questionnaire
import io.litequest.model.QuestionnaireResponse
import io.litequest.model.ResponseItem
import io.litequest.model.ValidationError
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

class QuestionnaireManager(
  private val questionnaire: Questionnaire,
  private val evaluator: LiteQuestEvaluator,
  private val translationManager: TranslationManager? = null,
) {
  private val _state =
    MutableStateFlow(
      QuestionnaireState.initial(
        questionnaire = questionnaire,
        response = createEmptyResponse(),
        items = questionnaire.items,
      )
    )
  val state: StateFlow<QuestionnaireState> = _state.asStateFlow()

  fun updateAnswer(linkId: String, value: JsonElement) {
    val currentResponse = _state.value.response
    val updatedItems = updateResponseItem(currentResponse.items, linkId, value)
    val updatedResponse = currentResponse.copy(items = updatedItems)

    val items = questionnaire.items.filter { item -> item.linkId == linkId }
    recomputeState(updatedResponse, items)
  }

  fun validate(): List<ValidationError> {
    return evaluator.validateResponse(_state.value.response)
  }

  fun isValid(): Boolean {
    return validate().isEmpty()
  }

  fun extractData(): JsonElement? {
    return evaluator.extractData(_state.value.response)
  }

  fun getResponse(): QuestionnaireResponse {
    return _state.value.response
  }

  fun setResponse(response: QuestionnaireResponse) {
    recomputeState(response)
  }

  private fun recomputeState(response: QuestionnaireResponse, items: List<Item>? = null) {
    val calculatedValues = evaluator.calculateValues(response)
    val visibleItems = evaluator.getVisibleItems(response)
    val validationErrors = evaluator.validateResponse(response, items)

    _state.value =
      QuestionnaireState(
        questionnaire = questionnaire,
        response = response,
        visibleItems = visibleItems,
        validationErrors = validationErrors,
        calculatedValues = calculatedValues,
        isValid = validationErrors.isEmpty(),
      )
  }

  private fun updateResponseItem(
    items: List<ResponseItem>,
    linkId: String,
    value: JsonElement,
  ): List<ResponseItem> {
    val existingItem = items.find { it.linkId == linkId }

    return if (existingItem != null) {
      items.map { item ->
        if (item.linkId == linkId) {
          item.copy(
            answers =
              if (value is JsonNull) {
                emptyList()
              } else {
                listOf(Answer(value))
              },
            items = emptyList(),
          )
        } else {
          val updatedNestedItems =
            if (item.items.isNotEmpty()) {
              updateResponseItem(item.items, linkId, value)
            } else {
              item.items
            }
          if (updatedNestedItems != item.items) {
            item.copy(items = updatedNestedItems)
          } else {
            item
          }
        }
      }
    } else {
      items +
        ResponseItem(
          linkId = linkId,
          answers = if (value is JsonNull) emptyList() else listOf(Answer(value)),
        )
    }
  }

  private fun createEmptyResponse(): QuestionnaireResponse {
    return QuestionnaireResponse(
      id = generateId(),
      questionnaireId = questionnaire.id,
      authored = getCurrentTimestamp(),
      subject = null,
      items = initializeResponseItems(questionnaire.items),
    )
  }

  private fun initializeResponseItems(items: List<Item>): List<ResponseItem> {
    return items.map { item ->
      ResponseItem(
        linkId = item.linkId,
        answers = emptyList(),
        items =
          if (item.items.isNotEmpty() && !item.repeats) {
            initializeResponseItems(item.items)
          } else {
            emptyList()
          },
      )
    }
  }

  @OptIn(ExperimentalUuidApi::class)
  private fun generateId(): String {
    return Uuid.toString()
  }

  @OptIn(ExperimentalTime::class)
  private fun getCurrentTimestamp(): String {
    return kotlin.time.Clock.System.now().toString()
  }
}
