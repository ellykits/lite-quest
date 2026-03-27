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
import io.litequest.model.Answer
import io.litequest.model.Item
import io.litequest.model.Questionnaire
import io.litequest.model.QuestionnaireResponse
import io.litequest.model.ResponseItem
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

class QuestionnaireManagerSampleRegressionTest {
  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun sampleFlow_updatesAnswers_skipLogic_validation_calculated_and_generatedResponse() {
    val questionnaire = loadQuestionnaireSample()
    val expectedResponse = loadResponseSample()
    val manager = QuestionnaireManager(questionnaire, LiteQuestEvaluator(questionnaire))

    // Populate manager directly from sample response so updates in JSON drive test coverage.
    applyAnswersFromSample(manager, expectedResponse)

    val state = manager.state.value
    assertTrue(state.validationErrors.isEmpty(), "Complete sample response should validate")

    val generatedResponse = manager.getResponse()
    assertEquals(expectedResponse.questionnaireId, generatedResponse.questionnaireId)

    val calculatedLinkIds = collectCalculatedLinkIds(questionnaire.items)
    assertResponseMatchesExpected(
      expectedItems = expectedResponse.items,
      actualItems = generatedResponse.items,
      calculatedLinkIds = calculatedLinkIds,
    )

    // Skip logic: clear all sections gated by consent, keep ungated answers.
    manager.updateAnswer("consentGiven", JsonPrimitive(false))
    val skippedResponse = manager.getResponse()

    val consentGatedLinkIds = collectConsentGatedTopLevelLinkIds(questionnaire.items)
    consentGatedLinkIds.forEach { linkId ->
      val item = itemAt(skippedResponse.items, linkId)
      assertTrue(item != null, "Expected response item '$linkId' to exist")
      assertItemTreeCleared(item)
    }

    val ungatedExpected =
      expectedResponse.items.filter {
        !consentGatedLinkIds.contains(it.linkId) &&
          it.linkId != "consentSection" &&
          it.linkId != "consentGiven"
      }
    val ungatedActual =
      skippedResponse.items.filter {
        !consentGatedLinkIds.contains(it.linkId) &&
          it.linkId != "consentSection" &&
          it.linkId != "consentGiven"
      }
    assertResponseMatchesExpected(ungatedExpected, ungatedActual, calculatedLinkIds)
  }

  @Test
  fun sampleValidation_requiredFieldHiddenThenVisible_behavesCorrectly() {
    val questionnaire = loadQuestionnaireSample().withRequiredField("firstName")
    val manager = QuestionnaireManager(questionnaire, LiteQuestEvaluator(questionnaire))

    assertTrue(
      manager.state.value.validationErrors.none { it.linkId == "firstName" },
      "firstName should not fail before consent makes it visible",
    )

    manager.updateAnswer("consentGiven", JsonPrimitive(true))
    assertTrue(
      manager.state.value.validationErrors.any { it.linkId == "firstName" },
      "Required field should validate once section becomes visible",
    )

    manager.updateAnswer("firstName", JsonPrimitive("John"))
    assertTrue(
      manager.state.value.validationErrors.none { it.linkId == "firstName" },
      "Validation error should clear after updateAnswer fills required field",
    )
  }

  private fun applyAnswersFromSample(
    manager: QuestionnaireManager,
    expected: QuestionnaireResponse,
  ) {
    expected.items.forEach { item -> applyItemAnswers(manager, item, parentRepeating = null) }
  }

  private fun applyItemAnswers(
    manager: QuestionnaireManager,
    item: ResponseItem,
    parentRepeating: Pair<String, Int>?,
  ) {
    if (item.answers.isNotEmpty()) {
      val hasNestedRepetitions = item.answers.any { it.items.isNotEmpty() }
      if (hasNestedRepetitions) {
        item.answers.forEachIndexed { repetitionIndex, answer ->
          manager.addRepetition(item.linkId)
          answer.items.forEach { nested ->
            applyItemAnswers(manager, nested, parentRepeating = item.linkId to repetitionIndex)
          }
        }
      } else {
        val firstValue = item.answers.firstOrNull()?.value
        if (firstValue != null) {
          if (parentRepeating != null) {
            manager.updateInRepetition(
              groupLinkId = parentRepeating.first,
              repetitionIndex = parentRepeating.second,
              fieldLinkId = item.linkId,
              value = firstValue,
            )
          } else {
            manager.updateAnswer(item.linkId, firstValue)
          }
        }
      }
    }

    // Nested non-repeating group structures.
    if (item.items.isNotEmpty()) {
      item.items.forEach { nested -> applyItemAnswers(manager, nested, parentRepeating) }
    }
  }

  private fun assertResponseMatchesExpected(
    expectedItems: List<ResponseItem>,
    actualItems: List<ResponseItem>,
    calculatedLinkIds: Set<String>,
  ) {
    expectedItems.forEach { expectedItem ->
      val actualItem = itemAt(actualItems, expectedItem.linkId)
      assertTrue(actualItem != null, "Missing response item '${expectedItem.linkId}'")

      assertAnswersMatch(
        linkId = expectedItem.linkId,
        expectedAnswers = expectedItem.answers,
        actualAnswers = actualItem.answers,
        allowNumericTolerance = calculatedLinkIds.contains(expectedItem.linkId),
      )

      if (expectedItem.items.isNotEmpty()) {
        assertResponseMatchesExpected(expectedItem.items, actualItem.items, calculatedLinkIds)
      }
    }
  }

  private fun assertAnswersMatch(
    linkId: String,
    expectedAnswers: List<Answer>,
    actualAnswers: List<Answer>,
    allowNumericTolerance: Boolean,
  ) {
    assertEquals(expectedAnswers.size, actualAnswers.size, "Answer count mismatch for $linkId")

    expectedAnswers.indices.forEach { index ->
      val expected = expectedAnswers[index]
      val actual = actualAnswers[index]

      if (expected.value != null && actual.value != null && allowNumericTolerance) {
        val expectedNum = (expected.value as? JsonPrimitive)?.content?.toDoubleOrNull()
        val actualNum = (actual.value as? JsonPrimitive)?.content?.toDoubleOrNull()
        if (expectedNum != null && actualNum != null) {
          assertTrue(
            abs(expectedNum - actualNum) <= 0.02,
            "Numeric calculated mismatch for $linkId at index $index",
          )
        } else {
          assertEquals(expected.value, actual.value, "Value mismatch for $linkId at index $index")
        }
      } else {
        assertEquals(expected.value, actual.value, "Value mismatch for $linkId at index $index")
      }

      if (expected.items.isNotEmpty()) {
        assertResponseMatchesExpected(
          expectedItems = expected.items,
          actualItems = actual.items,
          calculatedLinkIds = emptySet(),
        )
      }
    }
  }

  private fun assertItemTreeCleared(item: ResponseItem) {
    assertTrue(item.answers.isEmpty(), "Expected '${item.linkId}' answers to be cleared")
    item.items.forEach { nested -> assertItemTreeCleared(nested) }
  }

  private fun collectCalculatedLinkIds(items: List<Item>): Set<String> {
    val result = mutableSetOf<String>()
    fun walk(itemList: List<Item>) {
      itemList.forEach { item ->
        if (item.calculatedExpression != null) {
          result.add(item.linkId)
        }
        if (item.items.isNotEmpty()) {
          walk(item.items)
        }
      }
    }
    walk(items)
    return result
  }

  private fun collectConsentGatedTopLevelLinkIds(items: List<Item>): Set<String> {
    return items
      .filter {
        val expr = it.visibleIf?.toString() ?: return@filter false
        expr.contains("\"consentGiven\"")
      }
      .map { it.linkId }
      .toSet()
  }

  private fun itemAt(items: List<ResponseItem>, linkId: String): ResponseItem? {
    return items.firstOrNull { it.linkId == linkId }
  }

  private fun loadQuestionnaireSample(): Questionnaire {
    return json.decodeFromString(loadProjectFileText("files/single_page_questionnaire_sample.json"))
  }

  private fun loadResponseSample(): QuestionnaireResponse {
    return json.decodeFromString(loadProjectFileText("files/single_page_response_sample.json"))
  }

  private fun Questionnaire.withRequiredField(linkId: String): Questionnaire {
    return copy(items = items.markRequired(linkId))
  }

  private fun List<Item>.markRequired(linkId: String): List<Item> {
    return map { item ->
      when {
        item.linkId == linkId -> item.copy(required = true)
        item.items.isNotEmpty() -> item.copy(items = item.items.markRequired(linkId))
        else -> item
      }
    }
  }
}

expect fun loadProjectFileText(path: String): String
