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
import kotlinx.serialization.json.JsonPrimitive

class RepeatingGroupSkipLogicTest {

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
                      kotlinx.serialization.json.buildJsonObject {
                        put(
                          "==",
                          kotlinx.serialization.json.buildJsonArray {
                            // Use a trick here: in buildDataContext, repetitions are indexed
                            // But VisibilityEngine treats them specially?
                            // Actually, let's use a global dependency to keep it simple
                            add(
                              kotlinx.serialization.json.buildJsonObject {
                                put("var", JsonPrimitive("globalShow"))
                              }
                            )
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
    // updateInRepetition(groupLinkId, index, fieldLinkId, value)
    manager.updateInRepetition("group", 0, "childField", JsonPrimitive("some value"))

    // Verify answer is there
    assertEquals("some value", getRepetitionValue(manager, "group", 0, "childField"))

    // 3. Toggle globalShow = false
    manager.updateAnswer("globalShow", JsonPrimitive(false))

    // 4. Verify answer is CLEARED in the data
    // THIS IS WHERE IT LIKELY FAILS in current implementation
    assertEquals(
      null,
      getRepetitionValue(manager, "group", 0, "childField"),
      "Answer inside repetition should be cleared by skip logic",
    )
  }

  private fun getRepetitionValue(
    manager: QuestionnaireManager,
    groupLinkId: String,
    index: Int,
    fieldLinkId: String,
  ): String? {
    val group = manager.state.value.response.items.find { it.linkId == groupLinkId }
    val answer = group?.answers?.getOrNull(index)
    val field = answer?.items?.find { it.linkId == fieldLinkId }
    return field?.answers?.firstOrNull()?.value?.toString()?.trim('"')
  }
}
