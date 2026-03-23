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
import io.litequest.model.Item
import io.litequest.model.ItemType
import io.litequest.model.Questionnaire
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class QuestionnaireManagerTest {
  @Test
  fun testHiddenItemAnswersAreCleared() {
    val questionnaire =
      Questionnaire(
        id = "test-q",
        title = "Test",
        version = "1.0",
        items =
          listOf(
            Item(
              linkId = "consentSection",
              type = ItemType.GROUP,
              text = "Consent",
              items =
                listOf(
                  Item(linkId = "consentGiven", type = ItemType.BOOLEAN, text = "Do you consent?")
                ),
            ),
            Item(
              linkId = "conditionalField",
              type = ItemType.STRING,
              text = "Conditional Field",
              visibleIf =
                buildJsonObject {
                  put(
                    "==",
                    buildJsonObject {
                      put("0", buildJsonObject { put("var", "consentSection.consentGiven") })
                      put("1", true)
                    },
                  )
                },
            ),
          ),
      )

    val evaluator = LiteQuestEvaluator(questionnaire)
    val manager = QuestionnaireManager(questionnaire, evaluator)

    manager.updateAnswer("consentGiven", JsonPrimitive(true))
    manager.updateAnswer("conditionalField", JsonPrimitive("visible value"))

    var response = manager.getResponse()
    val conditionalField = response.items.find { it.linkId == "conditionalField" }
    assertTrue(conditionalField != null && conditionalField.answers.isNotEmpty())

    manager.updateAnswer("consentGiven", JsonPrimitive(false))

    response = manager.getResponse()
    val hiddenField = response.items.find { it.linkId == "conditionalField" }
    assertTrue(hiddenField != null, "Item should still exist in response")
    assertTrue(hiddenField.answers.isEmpty(), "Hidden item should have cleared answers")
  }

  @Test
  fun testNestedHiddenItemAnswersAreCleared() {
    val questionnaire =
      Questionnaire(
        id = "test-q",
        title = "Test",
        version = "1.0",
        items =
          listOf(
            Item(linkId = "showGroup", type = ItemType.BOOLEAN, text = "Show group?"),
            Item(
              linkId = "parentGroup",
              type = ItemType.GROUP,
              text = "Parent Group",
              visibleIf =
                buildJsonObject {
                  put(
                    "==",
                    buildJsonObject {
                      put("0", buildJsonObject { put("var", "showGroup") })
                      put("1", true)
                    },
                  )
                },
              items =
                listOf(
                  Item(linkId = "nestedField1", type = ItemType.STRING, text = "Field 1"),
                  Item(linkId = "nestedField2", type = ItemType.STRING, text = "Field 2"),
                ),
            ),
          ),
      )

    val evaluator = LiteQuestEvaluator(questionnaire)
    val manager = QuestionnaireManager(questionnaire, evaluator)

    manager.updateAnswer("showGroup", JsonPrimitive(true))
    manager.updateAnswer("nestedField1", JsonPrimitive("value1"))
    manager.updateAnswer("nestedField2", JsonPrimitive("value2"))

    var response = manager.getResponse()
    var parentGroup = response.items.find { it.linkId == "parentGroup" }
    assertTrue(parentGroup != null, "Parent group should exist when visible")

    val nestedField1 = parentGroup.items?.find { it.linkId == "nestedField1" }
    val nestedField2 = parentGroup?.items?.find { it.linkId == "nestedField2" }
    assertTrue(nestedField1?.answers?.isNotEmpty() == true, "nestedField1 should have answers")
    assertTrue(nestedField2?.answers?.isNotEmpty() == true, "nestedField2 should have answers")

    manager.updateAnswer("showGroup", JsonPrimitive(false))

    response = manager.getResponse()
    parentGroup = response.items.find { it.linkId == "parentGroup" }
    assertTrue(parentGroup != null, "Parent group should still exist in response")
    assertTrue(parentGroup.answers.isEmpty(), "Hidden parent group should have empty answers")
    assertFalse(
      parentGroup.items.isEmpty(),
      "Hidden parent group should preserve its nested items structure",
    )
    assertTrue(
      parentGroup.items.all { it.answers.isEmpty() },
      "All nested items should have empty answers",
    )
  }

  @Test
  fun testDataContextExcludesHiddenItems() {
    val questionnaire =
      Questionnaire(
        id = "test-q",
        title = "Test",
        version = "1.0",
        items =
          listOf(
            Item(linkId = "trigger", type = ItemType.BOOLEAN, text = "Trigger"),
            Item(
              linkId = "hiddenField",
              type = ItemType.STRING,
              text = "Hidden Field",
              visibleIf =
                buildJsonObject {
                  put(
                    "==",
                    buildJsonObject {
                      put("0", buildJsonObject { put("var", "trigger") })
                      put("1", true)
                    },
                  )
                },
            ),
          ),
      )

    val evaluator = LiteQuestEvaluator(questionnaire)
    val manager = QuestionnaireManager(questionnaire, evaluator)

    manager.updateAnswer("trigger", JsonPrimitive(true))
    manager.updateAnswer("hiddenField", JsonPrimitive("should be visible"))

    var dataContext = evaluator.buildDataContext(manager.getResponse())
    assertEquals("should be visible", dataContext["hiddenField"])

    manager.updateAnswer("trigger", JsonPrimitive(false))

    dataContext = evaluator.buildDataContext(manager.getResponse())
    assertFalse(dataContext.containsKey("hiddenField"))
  }

  @Test
  fun testNestedItemUpdateAndValidation() {
    val questionnaire =
      Questionnaire(
        id = "test-q",
        title = "Test",
        version = "1.0",
        items =
          listOf(
            Item(
              linkId = "rootField",
              type = ItemType.STRING,
              text = "Root Field",
              required = true,
            ),
            Item(
              linkId = "nestedGroup",
              type = ItemType.GROUP,
              text = "Nested Group",
              items =
                listOf(
                  Item(
                    linkId = "nestedField",
                    type = ItemType.STRING,
                    text = "Nested Field",
                    required = true,
                  )
                ),
            ),
          ),
      )

    val evaluator = LiteQuestEvaluator(questionnaire)
    val manager = QuestionnaireManager(questionnaire, evaluator)

    // Initial state: both fields missing, should now have 2 validation errors (fixed)
    assertEquals(
      2,
      manager.state.value.validationErrors.size,
      "Initially should have 2 validation errors",
    )

    // Update root field
    manager.updateAnswer("rootField", JsonPrimitive("root value"))
    assertEquals(
      1,
      manager.state.value.validationErrors.size,
      "After root update, should have 1 validation error remaining",
    )

    // Update nested field
    manager.updateAnswer("nestedField", JsonPrimitive("nested value"))

    val state = manager.state.value
    val response = state.response

    // Check if nested value is preserved
    val nestedGroup = response.items.find { it.linkId == "nestedGroup" }
    val nestedField = nestedGroup?.items?.find { it.linkId == "nestedField" }
    assertEquals(
      "nested value",
      nestedField?.answers?.firstOrNull()?.value?.toString()?.trim('"'),
      "Nested answer should be updated",
    )

    // Check validation errors
    assertEquals(
      0,
      state.validationErrors.size,
      "After both updates, should have 0 validation errors",
    )
  }

  @Test
  fun testValidationErrorsAreNotLostOnPartialUpdate() {
    val questionnaire =
      Questionnaire(
        id = "test-q",
        title = "Test",
        version = "1.0",
        items =
          listOf(
            Item(linkId = "field1", type = ItemType.STRING, text = "Field 1", required = true),
            Item(linkId = "field2", type = ItemType.STRING, text = "Field 2", required = true),
          ),
      )

    val evaluator = LiteQuestEvaluator(questionnaire)
    val manager = QuestionnaireManager(questionnaire, evaluator)

    // Initial: 2 errors
    assertEquals(2, manager.state.value.validationErrors.size)

    // Update field1
    manager.updateAnswer("field1", JsonPrimitive("value 1"))

    // Expect 1 error (for field2)
    assertEquals(
      1,
      manager.state.value.validationErrors.size,
      "Validation errors for other fields should NOT be lost",
    )
  }

  @Test
  fun testTogglingVisibilityDoesNotBreakHierarchy() {
    val questionnaire =
      Questionnaire(
        id = "test-q",
        title = "Test",
        version = "1.0",
        items =
          listOf(
            Item(linkId = "consent", type = ItemType.BOOLEAN, text = "Consent"),
            Item(
              linkId = "nestedGroup",
              type = ItemType.GROUP,
              text = "Nested Group",
              visibleIf =
                buildJsonObject {
                  put(
                    "==",
                    buildJsonArray {
                      add(buildJsonObject { put("var", JsonPrimitive("consent")) })
                      add(JsonPrimitive(true))
                    },
                  )
                },
              items = listOf(Item(linkId = "fieldX", type = ItemType.STRING, text = "Field X")),
            ),
          ),
      )

    val evaluator = LiteQuestEvaluator(questionnaire)
    val manager = QuestionnaireManager(questionnaire, evaluator)

    // 1. Consent = true
    manager.updateAnswer("consent", JsonPrimitive(true))

    // 2. Update fieldX
    manager.updateAnswer("fieldX", JsonPrimitive("first value"))

    // 3. Toggle Consent = false
    manager.updateAnswer("consent", JsonPrimitive(false))

    // 4. Toggle Consent = true again
    manager.updateAnswer("consent", JsonPrimitive(true))

    // 5. Update fieldX AGAIN
    manager.updateAnswer("fieldX", JsonPrimitive("second value"))

    // Check if updated correctly (hierarchy preserved)
    val response = manager.state.value.response
    val nestedGroup = response.items.find { it.linkId == "nestedGroup" }
    val fieldX = nestedGroup?.items?.find { it.linkId == "fieldX" }
    assertEquals(
      "second value",
      fieldX?.answers?.firstOrNull()?.value?.toString()?.trim('"'),
      "Fails to update answer after toggling visibility of parent group",
    )
  }

  @Test
  fun testSkipLogicInsideRepetition() {
    val questionnaire =
      Questionnaire(
        id = "test-q",
        title = "Test",
        version = "1.0",
        items =
          listOf(
            Item(
              linkId = "group",
              type = ItemType.GROUP,
              text = "Repeating Group",
              repeats = true,
              items =
                listOf(
                  Item(linkId = "showChild", type = ItemType.BOOLEAN, text = "Show child?"),
                  Item(
                    linkId = "childField",
                    type = ItemType.STRING,
                    text = "Child Field",
                    visibleIf =
                      buildJsonObject {
                        put(
                          "==",
                          buildJsonArray {
                            add(buildJsonObject { put("var", JsonPrimitive("globalShow")) })
                            add(JsonPrimitive(true))
                          },
                        )
                      },
                  ),
                ),
            ),
            Item(linkId = "globalShow", type = ItemType.BOOLEAN, text = "Global Show"),
          ),
      )

    val evaluator = LiteQuestEvaluator(questionnaire)
    val manager = QuestionnaireManager(questionnaire, evaluator)

    // 1. Initial state
    manager.updateAnswer("globalShow", JsonPrimitive(true))
    manager.addRepetition("group")

    // 2. Update childField in first repetition
    manager.updateInRepetition("group", 0, "childField", JsonPrimitive("some value"))

    // Verify answer is there
    val group = manager.state.value.response.items.find { it.linkId == "group" }
    val answer = group?.answers?.getOrNull(0)
    val field = answer?.items?.find { it.linkId == "childField" }
    assertEquals(
      "some value",
      field?.answers?.firstOrNull()?.value?.toString()?.trim('"'),
      "Answer should be present initially",
    )

    // 3. Toggle globalShow = false
    manager.updateAnswer("globalShow", JsonPrimitive(false))

    // 4. Verify answer is CLEARED in the data
    val groupAfterHide = manager.state.value.response.items.find { it.linkId == "group" }
    val answerAfterHide = groupAfterHide?.answers?.getOrNull(0)
    val fieldAfterHide = answerAfterHide?.items?.find { it.linkId == "childField" }
    assertTrue(
      fieldAfterHide?.answers?.isEmpty() == true,
      "Answer inside repetition should be cleared by skip logic",
    )
  }
}
