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
package io.litequest.engine

import io.litequest.model.Item
import io.litequest.model.ItemType
import io.litequest.model.Questionnaire
import io.litequest.state.QuestionnaireManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class IncrementalCalculationTest {

  @Test
  fun testIncrementalCalculationOnlyRecalculatesAffectedFields() {
    val questionnaire =
      Questionnaire(
        id = "test",
        version = "1.0",
        title = "Incremental Test",
        items =
          listOf(
            Item(linkId = "firstName", type = ItemType.STRING, text = "First Name"),
            Item(linkId = "lastName", type = ItemType.STRING, text = "Last Name"),
            Item(linkId = "age", type = ItemType.INTEGER, text = "Age"),
            Item(linkId = "weight", type = ItemType.DECIMAL, text = "Weight (kg)"),
            Item(linkId = "height", type = ItemType.DECIMAL, text = "Height (m)"),
            Item(
              linkId = "fullName",
              type = ItemType.STRING,
              text = "Full Name",
              readOnly = true,
              calculatedExpression =
                buildJsonObject {
                  put(
                    "cat",
                    buildJsonArray {
                      add(buildJsonObject { put("var", JsonPrimitive("firstName")) })
                      add(JsonPrimitive(" "))
                      add(buildJsonObject { put("var", JsonPrimitive("lastName")) })
                    },
                  )
                },
            ),
            Item(
              linkId = "bmi",
              type = ItemType.DECIMAL,
              text = "BMI",
              readOnly = true,
              calculatedExpression =
                buildJsonObject {
                  put(
                    "/",
                    buildJsonArray {
                      add(buildJsonObject { put("var", JsonPrimitive("weight")) })
                      add(
                        buildJsonObject {
                          put(
                            "*",
                            buildJsonArray {
                              add(buildJsonObject { put("var", JsonPrimitive("height")) })
                              add(buildJsonObject { put("var", JsonPrimitive("height")) })
                            },
                          )
                        }
                      )
                    },
                  )
                },
            ),
          ),
      )

    val evaluator = LiteQuestEvaluator(questionnaire)
    val manager = QuestionnaireManager(questionnaire, evaluator)

    manager.updateAnswer("firstName", JsonPrimitive("John"), null)
    manager.updateAnswer("lastName", JsonPrimitive("Doe"), null)
    manager.updateAnswer("weight", JsonPrimitive(70.0), null)
    manager.updateAnswer("height", JsonPrimitive(1.75), null)

    val state1 = manager.state.value
    val fullName1 = state1.calculatedValues["fullName"]
    val bmi1 = state1.calculatedValues["bmi"]

    assertNotNull(fullName1)
    assertEquals("John Doe", fullName1)
    assertNotNull(bmi1)
    assertEquals(22.857142857142858, (bmi1 as Number).toDouble(), 0.001)

    manager.updateAnswer("age", JsonPrimitive(30), null)

    val state2 = manager.state.value
    val fullName2 = state2.calculatedValues["fullName"]
    val bmi2 = state2.calculatedValues["bmi"]

    assertEquals("John Doe", fullName2)
    assertEquals(22.857142857142858, (bmi2 as Number).toDouble(), 0.001)

    manager.updateAnswer("weight", JsonPrimitive(80.0), null)

    val state3 = manager.state.value
    val fullName3 = state3.calculatedValues["fullName"]
    val bmi3 = state3.calculatedValues["bmi"]

    assertEquals("John Doe", fullName3)
    assertEquals(26.122448979591837, (bmi3 as Number).toDouble(), 0.001)
  }

  @Test
  fun testDependencyGraphBuiltCorrectly() {
    val questionnaire =
      Questionnaire(
        id = "test",
        version = "1.0",
        title = "Dependency Test",
        items =
          listOf(
            Item(linkId = "a", type = ItemType.INTEGER, text = "A"),
            Item(linkId = "b", type = ItemType.INTEGER, text = "B"),
            Item(linkId = "c", type = ItemType.INTEGER, text = "C"),
            Item(
              linkId = "sum_ab",
              type = ItemType.INTEGER,
              text = "A + B",
              readOnly = true,
              calculatedExpression =
                buildJsonObject {
                  put(
                    "+",
                    buildJsonArray {
                      add(buildJsonObject { put("var", JsonPrimitive("a")) })
                      add(buildJsonObject { put("var", JsonPrimitive("b")) })
                    },
                  )
                },
            ),
            Item(
              linkId = "sum_all",
              type = ItemType.INTEGER,
              text = "A + B + C",
              readOnly = true,
              calculatedExpression =
                buildJsonObject {
                  put(
                    "+",
                    buildJsonArray {
                      add(buildJsonObject { put("var", JsonPrimitive("sum_ab")) })
                      add(buildJsonObject { put("var", JsonPrimitive("c")) })
                    },
                  )
                },
            ),
          ),
      )

    val evaluator = LiteQuestEvaluator(questionnaire)
    val manager = QuestionnaireManager(questionnaire, evaluator)

    manager.updateAnswer("a", JsonPrimitive(10), null)
    manager.updateAnswer("b", JsonPrimitive(20), null)
    manager.updateAnswer("c", JsonPrimitive(30), null)

    val state = manager.state.value
    assertEquals(30.0, (state.calculatedValues["sum_ab"] as Number).toDouble())
    assertEquals(60.0, (state.calculatedValues["sum_all"] as Number).toDouble())
  }
}
