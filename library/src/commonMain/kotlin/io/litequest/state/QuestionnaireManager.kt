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
import io.litequest.model.ItemType
import io.litequest.model.Questionnaire
import io.litequest.model.QuestionnaireResponse
import io.litequest.model.ResponseItem
import io.litequest.model.ValidationError
import kotlin.time.Clock
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
  private val _state: MutableStateFlow<QuestionnaireState>
  val state: StateFlow<QuestionnaireState>

  init {
    val emptyResponse = createEmptyResponse()
    val initialVisibleItems = evaluator.getVisibleItems(emptyResponse)
    val initialCalculatedValues = evaluator.calculateValues(emptyResponse)
    _state =
      MutableStateFlow(
        QuestionnaireState(
          questionnaire = questionnaire,
          response = emptyResponse,
          visibleItems = initialVisibleItems,
          validationErrors = emptyList(),
          calculatedValues = initialCalculatedValues,
          isValid = false,
        )
      )
    state = _state.asStateFlow()
  }

  fun updateAnswer(linkId: String, value: JsonElement, text: String? = null) {
    val currentResponse = _state.value.response
    val updatedItems = updateResponseItem(currentResponse.items, linkId, value, text)
    val updatedResponse = currentResponse.copy(items = updatedItems)

    val items = questionnaire.items.filter { item -> item.linkId == linkId }
    recomputeState(updatedResponse, items)
  }

  fun addRepetition(groupLinkId: String) {
    val currentResponse = _state.value.response
    val groupItem = findItemInQuestionnaire(questionnaire.items, groupLinkId) ?: return
    if (!groupItem.repeats) return

    val updatedItems = addRepetitionToResponseItem(currentResponse.items, groupLinkId, groupItem)
    val updatedResponse = currentResponse.copy(items = updatedItems)
    recomputeState(updatedResponse)
  }

  fun removeRepetition(groupLinkId: String, repetitionIndex: Int) {
    val currentResponse = _state.value.response
    val updatedItems =
      removeRepetitionFromResponseItem(currentResponse.items, groupLinkId, repetitionIndex)
    val updatedResponse = currentResponse.copy(items = updatedItems)
    recomputeState(updatedResponse)
  }

  fun updateInRepetition(
    groupLinkId: String,
    repetitionIndex: Int,
    fieldLinkId: String,
    value: JsonElement,
    text: String? = null,
  ) {
    val currentResponse = _state.value.response
    val updatedItems =
      updateFieldInRepetition(
        currentResponse.items,
        groupLinkId,
        repetitionIndex,
        fieldLinkId,
        value,
        text,
      )
    val updatedResponse = currentResponse.copy(items = updatedItems)
    recomputeState(updatedResponse)
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
    val visibleItems = evaluator.getVisibleItems(response)
    val visibleLinkIds = collectVisibleLinkIds(visibleItems)
    val cleanedResponse = clearHiddenItemAnswers(response, visibleLinkIds)
    val calculatedValues = evaluator.calculateValues(cleanedResponse)
    val validationErrors = evaluator.validateResponse(cleanedResponse, items)

    _state.value =
      QuestionnaireState(
        questionnaire = questionnaire,
        response = cleanedResponse,
        visibleItems = visibleItems,
        validationErrors = validationErrors,
        calculatedValues = calculatedValues,
        isValid = validationErrors.isEmpty(),
      )
  }

  private fun collectVisibleLinkIds(items: List<Item>): Set<String> {
    val linkIds = mutableSetOf<String>()
    items.forEach { item ->
      linkIds.add(item.linkId)
      if (item.items.isNotEmpty()) {
        linkIds.addAll(collectVisibleLinkIds(item.items))
      }
    }
    return linkIds
  }

  private fun clearHiddenItemAnswers(
    response: QuestionnaireResponse,
    visibleLinkIds: Set<String>,
  ): QuestionnaireResponse {
    val cleanedItems = clearAnswersFromHidden(response.items, visibleLinkIds)
    return response.copy(items = cleanedItems)
  }

  private fun clearAnswersFromHidden(
    items: List<ResponseItem>,
    visibleLinkIds: Set<String>,
  ): List<ResponseItem> {
    return items.map { item ->
      if (visibleLinkIds.contains(item.linkId)) {
        if (item.items.isNotEmpty()) {
          item.copy(items = clearAnswersFromHidden(item.items, visibleLinkIds))
        } else {
          item
        }
      } else {
        item.copy(answers = emptyList(), items = emptyList())
      }
    }
  }

  private fun findItemInQuestionnaire(items: List<Item>, linkId: String): Item? {
    items.forEach { item ->
      if (item.linkId == linkId) return item
      if (item.items.isNotEmpty()) {
        val found = findItemInQuestionnaire(item.items, linkId)
        if (found != null) return found
      }
    }
    return null
  }

  private fun addRepetitionToResponseItem(
    items: List<ResponseItem>,
    groupLinkId: String,
    groupItem: Item,
  ): List<ResponseItem> {
    return items.map { item ->
      if (item.linkId == groupLinkId) {
        val newAnswer = Answer(value = null, items = initializeResponseItems(groupItem.items))
        item.copy(answers = item.answers + newAnswer)
      } else if (item.items.isNotEmpty()) {
        item.copy(items = addRepetitionToResponseItem(item.items, groupLinkId, groupItem))
      } else {
        item
      }
    }
  }

  private fun removeRepetitionFromResponseItem(
    items: List<ResponseItem>,
    groupLinkId: String,
    repetitionIndex: Int,
  ): List<ResponseItem> {
    return items.map { item ->
      if (item.linkId == groupLinkId) {
        item.copy(answers = item.answers.filterIndexed { index, _ -> index != repetitionIndex })
      } else if (item.items.isNotEmpty()) {
        item.copy(
          items = removeRepetitionFromResponseItem(item.items, groupLinkId, repetitionIndex)
        )
      } else {
        item
      }
    }
  }

  private fun updateFieldInRepetition(
    items: List<ResponseItem>,
    groupLinkId: String,
    repetitionIndex: Int,
    fieldLinkId: String,
    value: JsonElement,
    text: String?,
  ): List<ResponseItem> {
    return items.map { item ->
      if (item.linkId == groupLinkId) {
        val updatedAnswers =
          item.answers.mapIndexed { index, answer ->
            if (index == repetitionIndex) {
              val updatedItems = updateResponseItem(answer.items, fieldLinkId, value, text)
              answer.copy(items = updatedItems)
            } else {
              answer
            }
          }
        item.copy(answers = updatedAnswers)
      } else if (item.items.isNotEmpty()) {
        item.copy(
          items =
            updateFieldInRepetition(
              item.items,
              groupLinkId,
              repetitionIndex,
              fieldLinkId,
              value,
              text,
            )
        )
      } else {
        item
      }
    }
  }

  private fun updateResponseItem(
    items: List<ResponseItem>,
    linkId: String,
    value: JsonElement,
    text: String? = null,
  ): List<ResponseItem> {
    return items.map { item ->
      if (item.linkId == linkId) {
        item.copy(
          text = text,
          answers = if (value is JsonNull) emptyList() else listOf(Answer(value)),
        )
      } else if (item.items.isNotEmpty()) {
        item.copy(items = updateResponseItem(item.items, linkId, value, text))
      } else {
        item
      }
    }
  }

  @OptIn(ExperimentalUuidApi::class)
  private fun createEmptyResponse(): QuestionnaireResponse {
    return QuestionnaireResponse(
      id = Uuid.random().toString(),
      questionnaireId = questionnaire.id,
      authored = Clock.System.now().toString(),
      subject = null,
      items = initializeResponseItems(questionnaire.items),
    )
  }

  private fun initializeResponseItems(items: List<Item>): List<ResponseItem> {
    return items.flatMap { item ->
      when (item.type) {
        ItemType.LAYOUT_ROW,
        ItemType.LAYOUT_COLUMN,
        ItemType.LAYOUT_BOX -> {
          initializeResponseItems(item.items)
        }
        else -> {
          listOf(
            ResponseItem(
              linkId = item.linkId,
              text = item.text.takeIf { it.isNotEmpty() },
              answers = emptyList(),
              items =
                if (item.items.isNotEmpty() && !item.repeats) {
                  initializeResponseItems(item.items)
                } else {
                  emptyList()
                },
            )
          )
        }
      }
    }
  }
}
