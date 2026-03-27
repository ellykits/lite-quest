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
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CalculatedValuesIntegrationTest {

  @Test
  fun testFullNameCalculation() {
    val questionnaire =
      Questionnaire(
        id = "test",
        version = "1.0",
        title = "Test Questionnaire",
        items =
          listOf(
            Item(linkId = "firstName", type = ItemType.STRING, text = "First Name"),
            Item(linkId = "lastName", type = ItemType.STRING, text = "Last Name"),
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
          ),
      )

    val evaluator = LiteQuestEvaluator(questionnaire)
    val manager = QuestionnaireManager(questionnaire, evaluator)

    manager.updateAnswer("firstName", JsonPrimitive("John"), null)
    manager.updateAnswer("lastName", JsonPrimitive("Doe"), null)

    val state = manager.state.value
    val calculatedFullName = state.calculatedValues["fullName"]

    assertNotNull(calculatedFullName)
    assertEquals("John Doe", calculatedFullName)
  }

  @Test
  fun testBMICalculation() {
    val questionnaire =
      Questionnaire(
        id = "test",
        version = "1.0",
        title = "BMI Test",
        items =
          listOf(
            Item(linkId = "weight", type = ItemType.DECIMAL, text = "Weight (kg)"),
            Item(linkId = "height", type = ItemType.DECIMAL, text = "Height (m)"),
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

    manager.updateAnswer("weight", JsonPrimitive(70.0), null)
    manager.updateAnswer("height", JsonPrimitive(1.75), null)

    val state = manager.state.value
    val calculatedBMI = state.calculatedValues["bmi"]

    assertNotNull(calculatedBMI)
    val bmiValue = (calculatedBMI as? Number)?.toDouble()
    assertNotNull(bmiValue)
    assertEquals(22.857142857142858, bmiValue, 0.001)
  }

  @Test
  fun testCalculatedValuesIncludedInResponse() {
    val questionnaire =
      Questionnaire(
        id = "test",
        version = "1.0",
        title = "Test Questionnaire",
        items =
          listOf(
            Item(linkId = "firstName", type = ItemType.STRING, text = "First Name"),
            Item(linkId = "lastName", type = ItemType.STRING, text = "Last Name"),
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
          ),
      )

    val evaluator = LiteQuestEvaluator(questionnaire)
    val manager = QuestionnaireManager(questionnaire, evaluator)

    manager.updateAnswer("firstName", JsonPrimitive("John"), null)
    manager.updateAnswer("lastName", JsonPrimitive("Doe"), null)

    val response = manager.getResponse()
    val fullNameItem = response.items.find { it.linkId == "fullName" }
    assertNotNull(fullNameItem)
    assertEquals(
      1,
      fullNameItem.answers.size,
      "fullName should have 1 answer with calculated value",
    )
    assertEquals("John Doe", fullNameItem.answers.first().value?.toString()?.trim('"'))
  }

  @Test
  fun testNestedContainerCalculationUsingQualifiedPath() {
    val questionnaire =
      Questionnaire(
        id = "nested-calc",
        version = "1.0",
        title = "Nested Calc",
        items =
          listOf(
            Item(
              linkId = "headOfHousehold",
              type = ItemType.GROUP,
              items =
                listOf(
                  Item(
                    linkId = "nameRow",
                    type = ItemType.LAYOUT_ROW,
                    items =
                      listOf(
                        Item(linkId = "firstName", type = ItemType.STRING, text = "First Name"),
                        Item(linkId = "lastName", type = ItemType.STRING, text = "Last Name"),
                        Item(
                          linkId = "fullNameDisplay",
                          type = ItemType.STRING,
                          text = "Full Name",
                          readOnly = true,
                          calculatedExpression =
                            buildJsonObject {
                              put(
                                "cat",
                                buildJsonArray {
                                  add(
                                    buildJsonObject {
                                      put("var", JsonPrimitive("headOfHousehold.nameRow.firstName"))
                                    }
                                  )
                                  add(JsonPrimitive(" "))
                                  add(
                                    buildJsonObject {
                                      put("var", JsonPrimitive("headOfHousehold.nameRow.lastName"))
                                    }
                                  )
                                },
                              )
                            },
                        ),
                      ),
                  )
                ),
            )
          ),
      )

    val manager = QuestionnaireManager(questionnaire, LiteQuestEvaluator(questionnaire))
    manager.updateAnswer("firstName", JsonPrimitive("Ada"), null)
    manager.updateAnswer("lastName", JsonPrimitive("Lovelace"), null)

    assertEquals("Ada Lovelace", manager.state.value.calculatedValues["fullNameDisplay"])

    val response = manager.getResponse()
    val fullNameDisplay =
      response.items
        .first { it.linkId == "headOfHousehold" }
        .items
        .first { it.linkId == "nameRow" }
        .items
        .first { it.linkId == "fullNameDisplay" }
    assertTrue(fullNameDisplay.answers.isNotEmpty())
    assertEquals("Ada Lovelace", fullNameDisplay.answers.first().value?.toString()?.trim('"'))
  }

  @Test
  fun testRepeatingNestedCalculationUsingIndexedPath() {
    val questionnaire =
      Questionnaire(
        id = "repeat-calc",
        version = "1.0",
        title = "Repeat Calc",
        items =
          listOf(
            Item(
              linkId = "membersSection",
              type = ItemType.GROUP,
              repeats = true,
              items =
                listOf(
                  Item(
                    linkId = "memberRow",
                    type = ItemType.LAYOUT_ROW,
                    items =
                      listOf(
                        Item(linkId = "memberName", type = ItemType.STRING, text = "Member Name")
                      ),
                  )
                ),
            ),
            Item(
              linkId = "firstMemberName",
              type = ItemType.STRING,
              readOnly = true,
              calculatedExpression =
                buildJsonObject {
                  put(
                    "var",
                    buildJsonArray {
                      add(JsonPrimitive("membersSection[0].memberRow.memberName"))
                      add(JsonPrimitive("N/A"))
                    },
                  )
                },
            ),
          ),
      )

    val manager = QuestionnaireManager(questionnaire, LiteQuestEvaluator(questionnaire))
    assertEquals("N/A", manager.state.value.calculatedValues["firstMemberName"])

    manager.addRepetition("membersSection")
    manager.updateInRepetition("membersSection", 0, "memberName", JsonPrimitive("Alice"), null)
    manager.addRepetition("membersSection")
    manager.updateInRepetition("membersSection", 1, "memberName", JsonPrimitive("Bob"), null)

    assertEquals("Alice", manager.state.value.calculatedValues["firstMemberName"])
  }
}
