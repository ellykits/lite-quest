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
package io.litequest.util

import io.litequest.model.Answer
import io.litequest.model.QuestionnaireResponse
import io.litequest.model.ResponseItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class DataContextBuilderTest {
  @Test
  fun testBuildWithSimpleAnswers() {
    val response =
      QuestionnaireResponse(
        id = "test-1",
        questionnaireId = "q1",
        authored = "2025-11-07",
        items =
          listOf(
            ResponseItem(linkId = "name", answers = listOf(Answer(JsonPrimitive("John")))),
            ResponseItem(linkId = "age", answers = listOf(Answer(JsonPrimitive(25)))),
          ),
      )

    val context = DataContextBuilder.build(response)

    assertEquals("John", context["name"])
    assertEquals(25L, context["age"])
  }

  @Test
  fun testBuildWithNestedItems() {
    val response =
      QuestionnaireResponse(
        id = "test-2",
        questionnaireId = "q1",
        authored = "2025-11-07",
        items =
          listOf(
            ResponseItem(
              linkId = "group1",
              items =
                listOf(
                  ResponseItem(
                    linkId = "nested-field",
                    answers = listOf(Answer(JsonPrimitive("nested-value"))),
                  )
                ),
            )
          ),
      )

    val context = DataContextBuilder.build(response)

    assertEquals("nested-value", context["nested-field"])
  }

  @Test
  fun testBuildWithMultipleAnswers() {
    val response =
      QuestionnaireResponse(
        id = "test-3",
        questionnaireId = "q1",
        authored = "2025-11-07",
        items =
          listOf(
            ResponseItem(
              linkId = "tags",
              answers =
                listOf(
                  Answer(JsonPrimitive("tag1")),
                  Answer(JsonPrimitive("tag2")),
                  Answer(JsonPrimitive("tag3")),
                ),
            )
          ),
      )

    val context = DataContextBuilder.build(response)

    assertTrue(context["tags"] is List<*>)
    val tags = context["tags"] as List<*>
    assertEquals(3, tags.size)
    assertEquals("tag1", tags[0])
    assertEquals("tag2", tags[1])
    assertEquals("tag3", tags[2])
  }

  @Test
  fun testBuildWithEmptyResponse() {
    val response =
      QuestionnaireResponse(
        id = "test-4",
        questionnaireId = "q1",
        authored = "2025-11-07",
        items = emptyList(),
      )

    val context = DataContextBuilder.build(response)

    assertTrue(context.isEmpty())
  }

  @Test
  fun testBuildWithComplexNestedStructure() {
    val response =
      QuestionnaireResponse(
        id = "test-5",
        questionnaireId = "q1",
        authored = "2025-11-07",
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
            ResponseItem(linkId = "has-symptoms", answers = listOf(Answer(JsonPrimitive(true)))),
          ),
      )

    val context = DataContextBuilder.build(response)

    assertEquals(80.5, context["weight-kg"])
    assertEquals(1.8, context["height-m"])
    assertEquals(true, context["has-symptoms"])
    assertEquals(3, context.size)
  }

  @Test
  fun testBuildWithRepeatingGroupStructure() {
    val medicationsArray = buildJsonArray {
      add(
        buildJsonObject {
          put("medication-name", JsonPrimitive("Aspirin"))
          put("medication-dosage", JsonPrimitive("100mg"))
          put("medication-frequency", JsonPrimitive("Daily"))
        }
      )
      add(
        buildJsonObject {
          put("medication-name", JsonPrimitive("Ibuprofen"))
          put("medication-dosage", JsonPrimitive("200mg"))
          put("medication-frequency", JsonPrimitive("Twice daily"))
        }
      )
    }

    val response =
      QuestionnaireResponse(
        id = "test-6",
        questionnaireId = "q1",
        authored = "2025-11-07",
        items =
          listOf(ResponseItem(linkId = "medications", answers = listOf(Answer(medicationsArray)))),
      )

    val context = DataContextBuilder.build(response)

    assertTrue(context["medications"] is List<*>)
    val medications = context["medications"] as List<Map<String, Any?>>
    assertEquals(2, medications.size)

    assertEquals("Aspirin", medications[0]["medication-name"])
    assertEquals("100mg", medications[0]["medication-dosage"])
    assertEquals("Daily", medications[0]["medication-frequency"])

    assertEquals("Ibuprofen", medications[1]["medication-name"])
    assertEquals("200mg", medications[1]["medication-dosage"])
    assertEquals("Twice daily", medications[1]["medication-frequency"])
  }

  @Test
  fun testBuildWithNestedJsonObjectStructure() {
    val patientInfo = buildJsonObject {
      put("name", JsonPrimitive("John Doe"))
      put("age", JsonPrimitive(35))
      put(
        "contact",
        buildJsonObject {
          put("email", JsonPrimitive("john@example.com"))
          put("phone", JsonPrimitive("1234567890"))
        },
      )
    }

    val response =
      QuestionnaireResponse(
        id = "test-7",
        questionnaireId = "q1",
        authored = "2025-11-07",
        items = listOf(ResponseItem(linkId = "patient-info", answers = listOf(Answer(patientInfo)))),
      )

    val context = DataContextBuilder.build(response)

    assertTrue(context["patient-info"] is Map<*, *>)
    val info = context["patient-info"] as Map<String, Any?>
    assertEquals("John Doe", info["name"])
    assertEquals(35L, info["age"])

    assertTrue(info["contact"] is Map<*, *>)
    val contact = info["contact"] as Map<String, Any?>
    assertEquals("john@example.com", contact["email"])
    assertEquals(1234567890L, contact["phone"])
  }
}
