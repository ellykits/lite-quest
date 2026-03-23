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
import io.litequest.model.CalculatedValue
import io.litequest.model.Item
import io.litequest.model.ItemType
import io.litequest.model.Questionnaire
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonPrimitive

class TransitiveCalculatedValueTest {

  @Test
  fun testTransitiveCalculatedValues() {
    val questionnaire =
      Questionnaire(
        id = "test-q",
        title = "Test",
        version = "1.0",
        items = listOf(Item(linkId = "fieldA", type = ItemType.INTEGER, text = "Field A")),
        calculatedValues =
          listOf(
            CalculatedValue(
              name = "calcB",
              expression =
                kotlinx.serialization.json.buildJsonObject {
                  put(
                    "+",
                    kotlinx.serialization.json.buildJsonArray {
                      add(
                        kotlinx.serialization.json.buildJsonObject {
                          put("var", JsonPrimitive("fieldA"))
                        }
                      )
                      add(JsonPrimitive(1))
                    },
                  )
                },
            ),
            CalculatedValue(
              name = "calcC",
              expression =
                kotlinx.serialization.json.buildJsonObject {
                  put(
                    "+",
                    kotlinx.serialization.json.buildJsonArray {
                      add(
                        kotlinx.serialization.json.buildJsonObject {
                          put("var", JsonPrimitive("calcB"))
                        }
                      )
                      add(JsonPrimitive(1))
                    },
                  )
                },
            ),
          ),
      )

    val evaluator = LiteQuestEvaluator(questionnaire)
    val manager = QuestionnaireManager(questionnaire, evaluator)

    // 1. Initial state (fieldA = 10)
    manager.updateAnswer("fieldA", JsonPrimitive(10))

    // calcB should be 11, calcC should be 12
    assertEquals(
      11.0,
      manager.state.value.calculatedValues["calcB"] as? Double,
      "calcB should be INITIALIZED correctly",
    )
    assertEquals(
      12.0,
      manager.state.value.calculatedValues["calcC"] as? Double,
      "calcC should be INITIALIZED correctly",
    )

    // 2. Update fieldA = 20
    manager.updateAnswer("fieldA", JsonPrimitive(20))

    // calcB should be 21, calcC should be 22
    assertEquals(
      21.0,
      manager.state.value.calculatedValues["calcB"] as? Double,
      "calcB should update INCREMENTALLY",
    )

    // THIS IS WHERE IT LIKELY FAILS in current implementation
    assertEquals(
      22.0,
      manager.state.value.calculatedValues["calcC"] as? Double,
      "calcC should update TRANSITIVELY",
    )
  }
}
