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
import io.litequest.model.Item
import io.litequest.model.ItemType
import io.litequest.model.ResponseItem
import io.litequest.model.ValidationRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ValidationEngineTest {
  private val evaluator = JsonLogicEvaluator()
  private val engine = ValidationEngine(evaluator)

  @Test
  fun testRequiredFieldMissing() {
    val items =
      listOf(Item(linkId = "name", type = ItemType.STRING, text = "Name", required = true))
    val responseItems = emptyList<ResponseItem>()
    val dataContext = emptyMap<String, Any?>()

    val errors = engine.validateResponse(items, responseItems, dataContext)

    assertEquals(1, errors.size)
    assertEquals("name", errors[0].linkId)
    assertTrue(errors[0].message.contains("required"))
  }

  @Test
  fun testRequiredFieldPresent() {
    val items =
      listOf(Item(linkId = "name", type = ItemType.STRING, text = "Name", required = true))
    val responseItems =
      listOf(ResponseItem(linkId = "name", answers = listOf(Answer(JsonPrimitive("John")))))
    val dataContext = mapOf("name" to "John")

    val errors = engine.validateResponse(items, responseItems, dataContext)

    assertTrue(errors.isEmpty())
  }

  @Test
  fun testValidationRulePasses() {
    val items =
      listOf(
        Item(
          linkId = "age",
          type = ItemType.INTEGER,
          text = "Age",
          validations =
            listOf(
              ValidationRule(
                message = "Must be 18 or older",
                expression =
                  buildJsonObject {
                    put(
                      ">=",
                      buildJsonObject {
                        put("0", buildJsonObject { put("var", "age") })
                        put("1", 18)
                      },
                    )
                  },
              )
            ),
        )
      )
    val responseItems =
      listOf(ResponseItem(linkId = "age", answers = listOf(Answer(JsonPrimitive(25)))))
    val dataContext = mapOf("age" to 25)

    val errors = engine.validateResponse(items, responseItems, dataContext)

    assertTrue(errors.isEmpty())
  }

  @Test
  fun testValidationRuleFails() {
    val items =
      listOf(
        Item(
          linkId = "age",
          type = ItemType.INTEGER,
          text = "Age",
          validations =
            listOf(
              ValidationRule(
                message = "Must be 18 or older",
                expression =
                  buildJsonObject {
                    put(
                      ">=",
                      buildJsonObject {
                        put("0", buildJsonObject { put("var", "age") })
                        put("1", 18)
                      },
                    )
                  },
              )
            ),
        )
      )
    val responseItems =
      listOf(ResponseItem(linkId = "age", answers = listOf(Answer(JsonPrimitive(15)))))
    val dataContext = mapOf("age" to 15)

    val errors = engine.validateResponse(items, responseItems, dataContext)

    assertEquals(1, errors.size)
    assertEquals("age", errors[0].linkId)
    assertEquals("Must be 18 or older", errors[0].message)
  }

  @Test
  fun testNestedItemValidation() {
    val items =
      listOf(
        Item(
          linkId = "group1",
          type = ItemType.GROUP,
          text = "Group",
          items =
            listOf(
              Item(
                linkId = "nested-field",
                type = ItemType.STRING,
                text = "Nested Field",
                required = true,
              )
            ),
        )
      )
    val responseItems = listOf(ResponseItem(linkId = "group1", items = emptyList()))
    val dataContext = emptyMap<String, Any?>()

    val errors = engine.validateResponse(items, responseItems, dataContext)

    assertEquals(1, errors.size)
    assertEquals("nested-field", errors[0].linkId)
    assertEquals(listOf("group1", "nested-field"), errors[0].path)
  }

  @Test
  fun testNestedItemValidationWhenFieldsFilled() {
    val items =
      listOf(
        Item(
          linkId = "vitals-group",
          type = ItemType.GROUP,
          text = "Core Vitals",
          items =
            listOf(
              Item(
                linkId = "weight-kg",
                type = ItemType.DECIMAL,
                text = "Weight in kg",
                required = true,
              ),
              Item(
                linkId = "height-m",
                type = ItemType.DECIMAL,
                text = "Height in meters",
                required = true,
              ),
            ),
        )
      )
    val responseItems =
      listOf(
        ResponseItem(
          linkId = "vitals-group",
          items =
            listOf(
              ResponseItem(linkId = "weight-kg", answers = listOf(Answer(JsonPrimitive(70.0)))),
              ResponseItem(linkId = "height-m", answers = listOf(Answer(JsonPrimitive(1.75)))),
            ),
        )
      )
    val dataContext = mapOf("weight-kg" to 70.0, "height-m" to 1.75)

    val errors = engine.validateResponse(items, responseItems, dataContext)

    assertTrue(
      errors.isEmpty(),
      "No validation errors should exist when all required nested fields are filled",
    )
  }

  @Test
  fun testMultipleValidationErrors() {
    val items =
      listOf(
        Item(linkId = "name", type = ItemType.STRING, text = "Name", required = true),
        Item(linkId = "age", type = ItemType.INTEGER, text = "Age", required = true),
      )
    val responseItems = emptyList<ResponseItem>()
    val dataContext = emptyMap<String, Any?>()

    val errors = engine.validateResponse(items, responseItems, dataContext)

    assertEquals(2, errors.size)
  }
}
