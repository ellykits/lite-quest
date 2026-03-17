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
    assertTrue(parentGroup.items.isEmpty(), "Hidden parent group should have empty nested items")
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
}
