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

import io.litequest.model.Answer
import io.litequest.model.QuestionnaireResponse
import io.litequest.model.ResponseItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class ExtractionEngineTest {
  private val engine = ExtractionEngine()

  @Test
  fun testExtractFromAnswerSource() {
    val response = createSampleResponse()
    val answerMap = mapOf("name" to "John Doe", "age" to 30)
    val calculatedValues = emptyMap<String, Any?>()

    val template = buildJsonObject {
      put(
        "patientName",
        buildJsonObject {
          put("source", JsonPrimitive("answer"))
          put("linkId", JsonPrimitive("name"))
        },
      )
      put(
        "patientAge",
        buildJsonObject {
          put("source", JsonPrimitive("answer"))
          put("linkId", JsonPrimitive("age"))
        },
      )
    }

    val result = engine.extract(response, template, calculatedValues, answerMap)

    val expected = buildJsonObject {
      put("patientName", JsonPrimitive("John Doe"))
      put("patientAge", JsonPrimitive(30))
    }

    assertEquals(expected, result)
  }

  @Test
  fun testExtractFromCalculatedValueSource() {
    val response = createSampleResponse()
    val answerMap = emptyMap<String, Any?>()
    val calculatedValues = mapOf("bmi" to 22.5, "category" to "normal")

    val template = buildJsonObject {
      put(
        "bodyMassIndex",
        buildJsonObject {
          put("source", JsonPrimitive("calculatedValue"))
          put("name", JsonPrimitive("bmi"))
        },
      )
      put(
        "bmiCategory",
        buildJsonObject {
          put("source", JsonPrimitive("calculatedValue"))
          put("name", JsonPrimitive("category"))
        },
      )
    }

    val result = engine.extract(response, template, calculatedValues, answerMap)

    val expected = buildJsonObject {
      put("bodyMassIndex", JsonPrimitive(22.5))
      put("bmiCategory", JsonPrimitive("normal"))
    }

    assertEquals(expected, result)
  }

  @Test
  fun testExtractFromMetadataSource() {
    val response = createSampleResponse()
    val answerMap = emptyMap<String, Any?>()
    val calculatedValues = emptyMap<String, Any?>()

    val template = buildJsonObject {
      put(
        "responseId",
        buildJsonObject {
          put("source", JsonPrimitive("metadata"))
          put("path", JsonPrimitive("id"))
        },
      )
      put(
        "questionnaireId",
        buildJsonObject {
          put("source", JsonPrimitive("metadata"))
          put("path", JsonPrimitive("questionnaireId"))
        },
      )
    }

    val result = engine.extract(response, template, calculatedValues, answerMap)

    val expected = buildJsonObject {
      put("responseId", JsonPrimitive("response-123"))
      put("questionnaireId", JsonPrimitive("questionnaire-456"))
    }

    assertEquals(expected, result)
  }

  @Test
  fun testExtractNestedObjects() {
    val response = createSampleResponse()
    val answerMap = mapOf("firstName" to "Jane", "lastName" to "Smith", "age" to 25)
    val calculatedValues = emptyMap<String, Any?>()

    val template = buildJsonObject {
      put(
        "patient",
        buildJsonObject {
          put(
            "name",
            buildJsonObject {
              put(
                "first",
                buildJsonObject {
                  put("source", JsonPrimitive("answer"))
                  put("linkId", JsonPrimitive("firstName"))
                },
              )
              put(
                "last",
                buildJsonObject {
                  put("source", JsonPrimitive("answer"))
                  put("linkId", JsonPrimitive("lastName"))
                },
              )
            },
          )
          put(
            "age",
            buildJsonObject {
              put("source", JsonPrimitive("answer"))
              put("linkId", JsonPrimitive("age"))
            },
          )
        },
      )
    }

    val result = engine.extract(response, template, calculatedValues, answerMap)

    val expected = buildJsonObject {
      put(
        "patient",
        buildJsonObject {
          put(
            "name",
            buildJsonObject {
              put("first", JsonPrimitive("Jane"))
              put("last", JsonPrimitive("Smith"))
            },
          )
          put("age", JsonPrimitive(25))
        },
      )
    }

    assertEquals(expected, result)
  }

  @Test
  fun testExtractWithArrays() {
    val response = createSampleResponse()
    val answerMap = mapOf("symptom1" to "headache", "symptom2" to "fever")
    val calculatedValues = emptyMap<String, Any?>()

    val template = buildJsonObject {
      put(
        "symptoms",
        buildJsonArray {
          add(
            buildJsonObject {
              put("source", JsonPrimitive("answer"))
              put("linkId", JsonPrimitive("symptom1"))
            }
          )
          add(
            buildJsonObject {
              put("source", JsonPrimitive("answer"))
              put("linkId", JsonPrimitive("symptom2"))
            }
          )
        },
      )
    }

    val result = engine.extract(response, template, calculatedValues, answerMap)

    val expected = buildJsonObject {
      put(
        "symptoms",
        buildJsonArray {
          add(JsonPrimitive("headache"))
          add(JsonPrimitive("fever"))
        },
      )
    }

    assertEquals(expected, result)
  }

  @Test
  fun testExtractMissingAnswerReturnsNull() {
    val response = createSampleResponse()
    val answerMap = emptyMap<String, Any?>()
    val calculatedValues = emptyMap<String, Any?>()

    val template = buildJsonObject {
      put(
        "missingField",
        buildJsonObject {
          put("source", JsonPrimitive("answer"))
          put("linkId", JsonPrimitive("nonexistent"))
        },
      )
    }

    val result = engine.extract(response, template, calculatedValues, answerMap)

    val expected = buildJsonObject { put("missingField", JsonNull) }

    assertEquals(expected, result)
  }

  @Test
  fun testExtractMissingCalculatedValueReturnsNull() {
    val response = createSampleResponse()
    val answerMap = emptyMap<String, Any?>()
    val calculatedValues = emptyMap<String, Any?>()

    val template = buildJsonObject {
      put(
        "missingCalc",
        buildJsonObject {
          put("source", JsonPrimitive("calculatedValue"))
          put("name", JsonPrimitive("nonexistent"))
        },
      )
    }

    val result = engine.extract(response, template, calculatedValues, answerMap)

    val expected = buildJsonObject { put("missingCalc", JsonNull) }

    assertEquals(expected, result)
  }

  @Test
  fun testExtractWithUnknownSourceReturnsNull() {
    val response = createSampleResponse()
    val answerMap = emptyMap<String, Any?>()
    val calculatedValues = emptyMap<String, Any?>()

    val template = buildJsonObject {
      put(
        "unknownSource",
        buildJsonObject {
          put("source", JsonPrimitive("unknown"))
          put("field", JsonPrimitive("value"))
        },
      )
    }

    val result = engine.extract(response, template, calculatedValues, answerMap)

    val expected = buildJsonObject { put("unknownSource", JsonNull) }

    assertEquals(expected, result)
  }

  @Test
  fun testExtractWithMixedSources() {
    val response = createSampleResponse()
    val answerMap = mapOf("name" to "Alice", "age" to 28)
    val calculatedValues = mapOf("risk_score" to 75.5)

    val template = buildJsonObject {
      put(
        "patientName",
        buildJsonObject {
          put("source", JsonPrimitive("answer"))
          put("linkId", JsonPrimitive("name"))
        },
      )
      put(
        "assessmentScore",
        buildJsonObject {
          put("source", JsonPrimitive("calculatedValue"))
          put("name", JsonPrimitive("risk_score"))
        },
      )
      put(
        "submissionId",
        buildJsonObject {
          put("source", JsonPrimitive("metadata"))
          put("path", JsonPrimitive("id"))
        },
      )
    }

    val result = engine.extract(response, template, calculatedValues, answerMap)

    val expected = buildJsonObject {
      put("patientName", JsonPrimitive("Alice"))
      put("assessmentScore", JsonPrimitive(75.5))
      put("submissionId", JsonPrimitive("response-123"))
    }

    assertEquals(expected, result)
  }

  @Test
  fun testExtractWithBooleanValues() {
    val response = createSampleResponse()
    val answerMap = mapOf("hasSymptoms" to true, "isVaccinated" to false)
    val calculatedValues = emptyMap<String, Any?>()

    val template = buildJsonObject {
      put(
        "symptoms",
        buildJsonObject {
          put("source", JsonPrimitive("answer"))
          put("linkId", JsonPrimitive("hasSymptoms"))
        },
      )
      put(
        "vaccinated",
        buildJsonObject {
          put("source", JsonPrimitive("answer"))
          put("linkId", JsonPrimitive("isVaccinated"))
        },
      )
    }

    val result = engine.extract(response, template, calculatedValues, answerMap)

    val expected = buildJsonObject {
      put("symptoms", JsonPrimitive(true))
      put("vaccinated", JsonPrimitive(false))
    }

    assertEquals(expected, result)
  }

  @Test
  fun testExtractWithStaticValues() {
    val response = createSampleResponse()
    val answerMap = mapOf("name" to "Bob")
    val calculatedValues = emptyMap<String, Any?>()

    val template = buildJsonObject {
      put(
        "patientName",
        buildJsonObject {
          put("source", JsonPrimitive("answer"))
          put("linkId", JsonPrimitive("name"))
        },
      )
      put("version", JsonPrimitive("1.0.0"))
      put("status", JsonPrimitive("completed"))
    }

    val result = engine.extract(response, template, calculatedValues, answerMap)

    val expected = buildJsonObject {
      put("patientName", JsonPrimitive("Bob"))
      put("version", JsonPrimitive("1.0.0"))
      put("status", JsonPrimitive("completed"))
    }

    assertEquals(expected, result)
  }

  private fun createSampleResponse(): QuestionnaireResponse {
    return QuestionnaireResponse(
      id = "response-123",
      questionnaireId = "questionnaire-456",
      authored = "2025-01-01T00:00:00Z",
      items =
        listOf(
          ResponseItem(
            linkId = "name",
            answers = listOf(Answer(value = JsonPrimitive("John Doe"))),
          ),
          ResponseItem(linkId = "age", answers = listOf(Answer(value = JsonPrimitive(30)))),
        ),
    )
  }
}
