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

import io.litequest.model.Item
import io.litequest.model.ItemType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

class CalculatedExpressionCollectorTest {

  @Test
  fun testEmptyItemsList() {
    val result = CalculatedExpressionCollector.collect(emptyList())
    assertTrue(result.isEmpty())
  }

  @Test
  fun testSingleItemWithCalculatedExpression() {
    val items =
      listOf(
        Item(
          linkId = "bmi",
          type = ItemType.DECIMAL,
          text = "BMI",
          calculatedExpression =
            buildJsonObject {
              putJsonArray("/") {
                add(buildJsonObject { put("var", "weight") })
                add(
                  buildJsonObject {
                    putJsonArray("*") {
                      add(buildJsonObject { put("var", "height") })
                      add(buildJsonObject { put("var", "height") })
                    }
                  }
                )
              }
            },
        )
      )

    val result = CalculatedExpressionCollector.collect(items)

    assertEquals(1, result.size)
    assertEquals("bmi", result[0].name)
    assertNotNull(result[0].expression)
  }

  @Test
  fun testMultipleItemsWithCalculatedExpressions() {
    val items =
      listOf(
        Item(
          linkId = "fullName",
          type = ItemType.STRING,
          text = "Full Name",
          calculatedExpression =
            buildJsonObject {
              putJsonArray("cat") {
                add(buildJsonObject { put("var", "firstName") })
                add(JsonPrimitive(" "))
                add(buildJsonObject { put("var", "lastName") })
              }
            },
        ),
        Item(
          linkId = "bmi",
          type = ItemType.DECIMAL,
          text = "BMI",
          calculatedExpression =
            buildJsonObject {
              putJsonArray("/") {
                add(buildJsonObject { put("var", "weight") })
                add(
                  buildJsonObject {
                    putJsonArray("*") {
                      add(buildJsonObject { put("var", "height") })
                      add(buildJsonObject { put("var", "height") })
                    }
                  }
                )
              }
            },
        ),
      )

    val result = CalculatedExpressionCollector.collect(items)

    assertEquals(2, result.size)
    assertEquals("fullName", result[0].name)
    assertNotNull(result[0].expression)
    assertEquals("bmi", result[1].name)
    assertNotNull(result[1].expression)
  }

  @Test
  fun testItemsWithoutCalculatedExpressions() {
    val items =
      listOf(
        Item(linkId = "firstName", type = ItemType.STRING, text = "First Name"),
        Item(linkId = "lastName", type = ItemType.STRING, text = "Last Name"),
      )

    val result = CalculatedExpressionCollector.collect(items)

    assertTrue(result.isEmpty())
  }

  @Test
  fun testMixedItemsWithAndWithoutCalculatedExpressions() {
    val items =
      listOf(
        Item(linkId = "firstName", type = ItemType.STRING, text = "First Name"),
        Item(
          linkId = "fullName",
          type = ItemType.STRING,
          text = "Full Name",
          calculatedExpression =
            buildJsonObject {
              putJsonArray("cat") {
                add(buildJsonObject { put("var", "firstName") })
                add(JsonPrimitive(" "))
                add(buildJsonObject { put("var", "lastName") })
              }
            },
        ),
        Item(linkId = "lastName", type = ItemType.STRING, text = "Last Name"),
      )

    val result = CalculatedExpressionCollector.collect(items)

    assertEquals(1, result.size)
    assertEquals("fullName", result[0].name)
  }

  @Test
  fun testNestedItemsWithCalculatedExpressions() {
    val items =
      listOf(
        Item(
          linkId = "personalInfo",
          type = ItemType.GROUP,
          text = "Personal Information",
          items =
            listOf(
              Item(linkId = "firstName", type = ItemType.STRING, text = "First Name"),
              Item(linkId = "lastName", type = ItemType.STRING, text = "Last Name"),
              Item(
                linkId = "fullName",
                type = ItemType.STRING,
                text = "Full Name",
                calculatedExpression =
                  buildJsonObject {
                    putJsonArray("cat") {
                      add(buildJsonObject { put("var", "firstName") })
                      add(JsonPrimitive(" "))
                      add(buildJsonObject { put("var", "lastName") })
                    }
                  },
              ),
            ),
        ),
        Item(
          linkId = "healthMetrics",
          type = ItemType.GROUP,
          text = "Health Metrics",
          items =
            listOf(
              Item(linkId = "weight", type = ItemType.DECIMAL, text = "Weight (kg)"),
              Item(linkId = "height", type = ItemType.DECIMAL, text = "Height (m)"),
              Item(
                linkId = "bmi",
                type = ItemType.DECIMAL,
                text = "BMI",
                calculatedExpression =
                  buildJsonObject {
                    putJsonArray("/") {
                      add(buildJsonObject { put("var", "weight") })
                      add(
                        buildJsonObject {
                          putJsonArray("*") {
                            add(buildJsonObject { put("var", "height") })
                            add(buildJsonObject { put("var", "height") })
                          }
                        }
                      )
                    }
                  },
              ),
            ),
        ),
      )

    val result = CalculatedExpressionCollector.collect(items)

    assertEquals(2, result.size)
    assertEquals("fullName", result[0].name)
    assertNotNull(result[0].expression)
    assertEquals("bmi", result[1].name)
    assertNotNull(result[1].expression)
  }

  @Test
  fun testDeeplyNestedCalculatedExpressions() {
    val items =
      listOf(
        Item(
          linkId = "section1",
          type = ItemType.GROUP,
          text = "Section 1",
          items =
            listOf(
              Item(
                linkId = "section2",
                type = ItemType.GROUP,
                text = "Section 2",
                items =
                  listOf(
                    Item(
                      linkId = "calculated1",
                      type = ItemType.STRING,
                      text = "Calculated 1",
                      calculatedExpression =
                        buildJsonObject {
                          putJsonArray("+") {
                            add(buildJsonObject { put("var", "a") })
                            add(buildJsonObject { put("var", "b") })
                          }
                        },
                    ),
                    Item(
                      linkId = "section3",
                      type = ItemType.GROUP,
                      text = "Section 3",
                      items =
                        listOf(
                          Item(
                            linkId = "calculated2",
                            type = ItemType.DECIMAL,
                            text = "Calculated 2",
                            calculatedExpression =
                              buildJsonObject {
                                putJsonArray("*") {
                                  add(buildJsonObject { put("var", "c") })
                                  add(buildJsonObject { put("var", "d") })
                                }
                              },
                          )
                        ),
                    ),
                  ),
              )
            ),
        )
      )

    val result = CalculatedExpressionCollector.collect(items)

    assertEquals(2, result.size)
    assertEquals("calculated1", result[0].name)
    assertNotNull(result[0].expression)
    assertEquals("calculated2", result[1].name)
    assertNotNull(result[1].expression)
  }

  @Test
  fun testLayoutItemsWithCalculatedExpressions() {
    val items =
      listOf(
        Item(
          linkId = "row1",
          type = ItemType.LAYOUT_ROW,
          items =
            listOf(
              Item(linkId = "field1", type = ItemType.STRING, text = "Field 1"),
              Item(
                linkId = "calculated",
                type = ItemType.STRING,
                text = "Calculated",
                calculatedExpression =
                  buildJsonObject {
                    putJsonArray("+") {
                      add(buildJsonObject { put("var", "field1") })
                      add(buildJsonObject { put("var", "field2") })
                    }
                  },
              ),
            ),
        )
      )

    val result = CalculatedExpressionCollector.collect(items)

    assertEquals(1, result.size)
    assertEquals("calculated", result[0].name)
    assertNotNull(result[0].expression)
  }

  @Test
  fun testGroupWithCalculatedExpressionAtParentLevel() {
    val items =
      listOf(
        Item(
          linkId = "personalInfo",
          type = ItemType.GROUP,
          text = "Personal Information",
          calculatedExpression =
            buildJsonObject {
              putJsonArray("+") {
                add(buildJsonObject { put("var", "some") })
                add(buildJsonObject { put("var", "expression") })
              }
            },
          items =
            listOf(
              Item(linkId = "firstName", type = ItemType.STRING, text = "First Name"),
              Item(
                linkId = "fullName",
                type = ItemType.STRING,
                text = "Full Name",
                calculatedExpression =
                  buildJsonObject {
                    putJsonArray("cat") {
                      add(buildJsonObject { put("var", "firstName") })
                      add(JsonPrimitive(" "))
                      add(buildJsonObject { put("var", "lastName") })
                    }
                  },
              ),
            ),
        )
      )

    val result = CalculatedExpressionCollector.collect(items)

    assertEquals(2, result.size)
    assertEquals("personalInfo", result[0].name)
    assertEquals("fullName", result[1].name)
  }
}
