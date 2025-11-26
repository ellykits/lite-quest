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
package io.litequest

import io.litequest.engine.LiteQuestEvaluator
import io.litequest.model.Answer
import io.litequest.model.CalculatedValue
import io.litequest.model.Item
import io.litequest.model.ItemType
import io.litequest.model.Questionnaire
import io.litequest.model.QuestionnaireResponse
import io.litequest.model.ResponseItem
import io.litequest.model.Subject
import io.litequest.model.Translations
import io.litequest.model.ValidationRule
import io.litequest.state.QuestionnaireManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class VitalsExampleTest {
  private val vitalsQuestionnaire =
    Questionnaire(
      id = "patient-vitals-v1.2",
      version = "1.2.0",
      title = "vitals.form.title",
      description = "vitals.form.description",
      translations =
        Translations(
          defaultLocale = "en",
          sources = mapOf("en" to "https://example.com/translations/vitals/en.json"),
        ),
      calculatedValues =
        listOf(
          CalculatedValue(
            name = "bmi",
            expression =
              buildJsonObject {
                put(
                  "if",
                  buildJsonObject {
                    put(
                      "0",
                      buildJsonObject {
                        put(
                          "and",
                          buildJsonObject {
                            put(
                              "0",
                              buildJsonObject {
                                put(
                                  ">",
                                  buildJsonObject {
                                    put("0", buildJsonObject { put("var", "height-m") })
                                    put("1", 0)
                                  },
                                )
                              },
                            )
                            put(
                              "1",
                              buildJsonObject {
                                put(
                                  ">",
                                  buildJsonObject {
                                    put("0", buildJsonObject { put("var", "weight-kg") })
                                    put("1", 0)
                                  },
                                )
                              },
                            )
                          },
                        )
                      },
                    )
                    put(
                      "1",
                      buildJsonObject {
                        put(
                          "/",
                          buildJsonObject {
                            put("0", buildJsonObject { put("var", "weight-kg") })
                            put(
                              "1",
                              buildJsonObject {
                                put(
                                  "*",
                                  buildJsonObject {
                                    put("0", buildJsonObject { put("var", "height-m") })
                                    put("1", buildJsonObject { put("var", "height-m") })
                                  },
                                )
                              },
                            )
                          },
                        )
                      },
                    )
                    put("2", JsonPrimitive(null as String?))
                  },
                )
              },
          )
        ),
      extractionTemplate =
        buildJsonObject {
          put(
            "patientId",
            buildJsonObject {
              put("source", "metadata")
              put("path", "subject.id")
            },
          )
          put(
            "effectiveDateTime",
            buildJsonObject {
              put("source", "metadata")
              put("path", "authored")
            },
          )
          put(
            "vitals",
            buildJsonObject {
              put(
                "bodyWeight",
                buildJsonObject {
                  put("source", "answer")
                  put("linkId", "weight-kg")
                },
              )
              put(
                "bodyHeight",
                buildJsonObject {
                  put("source", "answer")
                  put("linkId", "height-m")
                },
              )
              put(
                "bodyMassIndex",
                buildJsonObject {
                  put("source", "calculatedValue")
                  put("name", "bmi")
                },
              )
            },
          )
        },
      items =
        listOf(
          Item(
            linkId = "vitals-group",
            type = ItemType.GROUP,
            text = "vitals.group.title",
            items =
              listOf(
                Item(
                  linkId = "weight-kg",
                  type = ItemType.DECIMAL,
                  text = "vitals.weight.label",
                  required = true,
                  validations =
                    listOf(
                      ValidationRule(
                        message = "vitals.weight.validation.range",
                        expression =
                          buildJsonObject {
                            put(
                              "and",
                              buildJsonObject {
                                put(
                                  "0",
                                  buildJsonObject {
                                    put(
                                      ">=",
                                      buildJsonObject {
                                        put("0", buildJsonObject { put("var", "weight-kg") })
                                        put("1", 1)
                                      },
                                    )
                                  },
                                )
                                put(
                                  "1",
                                  buildJsonObject {
                                    put(
                                      "<=",
                                      buildJsonObject {
                                        put("0", buildJsonObject { put("var", "weight-kg") })
                                        put("1", 300)
                                      },
                                    )
                                  },
                                )
                              },
                            )
                          },
                      )
                    ),
                ),
                Item(
                  linkId = "height-m",
                  type = ItemType.DECIMAL,
                  text = "vitals.height.label",
                  required = true,
                ),
                Item(
                  linkId = "display-bmi",
                  type = ItemType.DISPLAY,
                  text = "vitals.bmi.display",
                  visibleIf =
                    buildJsonObject {
                      put(
                        "!=",
                        buildJsonObject {
                          put("0", buildJsonObject { put("var", "bmi") })
                          put("1", JsonPrimitive(null as String?))
                        },
                      )
                    },
                ),
              ),
          ),
          Item(linkId = "has-symptoms", type = ItemType.BOOLEAN, text = "vitals.symptoms.check"),
          Item(
            linkId = "symptoms-list",
            type = ItemType.TEXT,
            text = "vitals.symptoms.list",
            visibleIf =
              buildJsonObject {
                put(
                  "==",
                  buildJsonObject {
                    put("0", buildJsonObject { put("var", "has-symptoms") })
                    put("1", true)
                  },
                )
              },
          ),
        ),
    )

  @Test
  fun testBmiCalculation() {
    val evaluator = LiteQuestEvaluator(vitalsQuestionnaire)

    val response =
      QuestionnaireResponse(
        id = "resp-test",
        questionnaireId = vitalsQuestionnaire.id,
        authored = "2025-10-26T10:00:00Z",
        subject = Subject(id = "patient-123", type = "Patient"),
        items =
          listOf(
            ResponseItem(
              linkId = "vitals-group",
              items =
                listOf(
                  ResponseItem(linkId = "weight-kg", answers = listOf(Answer(JsonPrimitive(80.5)))),
                  ResponseItem(linkId = "height-m", answers = listOf(Answer(JsonPrimitive(1.8)))),
                ),
            )
          ),
      )

    val calculatedValues = evaluator.calculateValues(response)
    val bmi = calculatedValues["bmi"] as? Double

    assertNotNull(bmi)
    assertEquals(24.845679012345678, bmi, 0.0001)
  }

  @Test
  fun testVisibilityCondition() {
    val evaluator = LiteQuestEvaluator(vitalsQuestionnaire)

    val responseWithoutSymptoms =
      QuestionnaireResponse(
        id = "resp-test",
        questionnaireId = vitalsQuestionnaire.id,
        authored = "2025-10-26T10:00:00Z",
        items =
          listOf(
            ResponseItem(linkId = "has-symptoms", answers = listOf(Answer(JsonPrimitive(false))))
          ),
      )

    val visibleItems = evaluator.getVisibleItems(responseWithoutSymptoms)
    val symptomsListVisible = visibleItems.any { it.linkId == "symptoms-list" }

    assertFalse(symptomsListVisible)

    val responseWithSymptoms =
      QuestionnaireResponse(
        id = "resp-test-2",
        questionnaireId = vitalsQuestionnaire.id,
        authored = "2025-10-26T10:00:00Z",
        items =
          listOf(
            ResponseItem(linkId = "has-symptoms", answers = listOf(Answer(JsonPrimitive(true))))
          ),
      )

    val visibleItemsWithSymptoms = evaluator.getVisibleItems(responseWithSymptoms)
    val symptomsListNowVisible = visibleItemsWithSymptoms.any { it.linkId == "symptoms-list" }

    assertTrue(symptomsListNowVisible)
  }

  @Test
  fun testValidation() {
    val evaluator = LiteQuestEvaluator(vitalsQuestionnaire)

    val invalidResponse =
      QuestionnaireResponse(
        id = "resp-test",
        questionnaireId = vitalsQuestionnaire.id,
        authored = "2025-10-26T10:00:00Z",
        items =
          listOf(
            ResponseItem(
              linkId = "vitals-group",
              items =
                listOf(
                  ResponseItem(
                    linkId = "weight-kg",
                    answers = listOf(Answer(JsonPrimitive(350.0))),
                  ),
                  ResponseItem(linkId = "height-m", answers = listOf(Answer(JsonPrimitive(1.8)))),
                ),
            )
          ),
      )

    val errors = evaluator.validateResponse(invalidResponse)
    assertTrue(errors.isNotEmpty())
    assertTrue(errors.any { it.linkId == "weight-kg" })
  }

  @Test
  fun testDataExtraction() {
    val evaluator = LiteQuestEvaluator(vitalsQuestionnaire)

    val response =
      QuestionnaireResponse(
        id = "resp-a4b2-9c1d-e8f3",
        questionnaireId = vitalsQuestionnaire.id,
        authored = "2025-10-26T10:00:00Z",
        subject = Subject(id = "patient-123", type = "Patient"),
        items =
          listOf(
            ResponseItem(
              linkId = "vitals-group",
              items =
                listOf(
                  ResponseItem(linkId = "weight-kg", answers = listOf(Answer(JsonPrimitive(80.5)))),
                  ResponseItem(linkId = "height-m", answers = listOf(Answer(JsonPrimitive(1.8)))),
                ),
            ),
            ResponseItem(linkId = "has-symptoms", answers = listOf(Answer(JsonPrimitive(false)))),
          ),
      )

    val extracted = evaluator.extractData(response)
    assertNotNull(extracted)
  }

  @Test
  fun testQuestionnaireManager() {
    val manager =
      QuestionnaireManager(
        questionnaire = vitalsQuestionnaire,
        evaluator = LiteQuestEvaluator(vitalsQuestionnaire),
      )

    manager.updateAnswer("weight-kg", JsonPrimitive(80.5))
    manager.updateAnswer("height-m", JsonPrimitive(1.8))

    val state = manager.state.value

    assertTrue(state.calculatedValues.containsKey("bmi"))
    val bmi = state.calculatedValues["bmi"] as? Double
    assertNotNull(bmi)
    assertEquals(24.845679012345678, bmi, 0.0001)
  }

  @Test
  fun testQuestionnaireManagerWithRepeatingGroups() {
    val questionnaireWithGroups =
      vitalsQuestionnaire.copy(
        items =
          vitalsQuestionnaire.items +
            listOf(
              Item(
                linkId = "medications",
                type = ItemType.GROUP,
                text = "Current Medications",
                repeats = true,
                items =
                  listOf(
                    Item(
                      linkId = "medication-name",
                      type = ItemType.TEXT,
                      text = "Medication Name",
                      required = true,
                    ),
                    Item(
                      linkId = "medication-dosage",
                      type = ItemType.TEXT,
                      text = "Dosage",
                      required = true,
                    ),
                  ),
              )
            )
      )

    val manager =
      QuestionnaireManager(
        questionnaire = questionnaireWithGroups,
        evaluator = LiteQuestEvaluator(questionnaireWithGroups),
      )

    val medicationsArray = buildJsonArray {
      add(
        buildJsonObject {
          put("medication-name", JsonPrimitive("Aspirin"))
          put("medication-dosage", JsonPrimitive("100mg"))
        }
      )
      add(
        buildJsonObject {
          put("medication-name", JsonPrimitive("Ibuprofen"))
          put("medication-dosage", JsonPrimitive("200mg"))
        }
      )
    }

    manager.updateAnswer("medications", medicationsArray)
    manager.updateAnswer("weight-kg", JsonPrimitive(75.0))

    val response = manager.getResponse()
    val medicationsItem = response.items.find { it.linkId == "medications" }

    assertNotNull(medicationsItem)
    assertTrue(medicationsItem.answers.isNotEmpty())

    val extractedData = manager.extractData()
    assertNotNull(extractedData)
  }
}
