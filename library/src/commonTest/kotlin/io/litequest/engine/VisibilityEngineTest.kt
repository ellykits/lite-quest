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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class VisibilityEngineTest {
  private val evaluator = JsonLogicEvaluator()
  private val engine = VisibilityEngine(evaluator)

  @Test
  fun testItemWithNoConditionIsVisible() {
    val item = Item(linkId = "name", type = ItemType.STRING, text = "Name")
    val dataContext = emptyMap<String, Any?>()

    val isVisible = engine.isVisible(item, dataContext)

    assertTrue(isVisible)
  }

  @Test
  fun testItemWithTrueConditionIsVisible() {
    val item =
      Item(
        linkId = "symptoms-list",
        type = ItemType.TEXT,
        text = "List symptoms",
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
      )
    val dataContext = mapOf("has-symptoms" to true)

    val isVisible = engine.isVisible(item, dataContext)

    assertTrue(isVisible)
  }

  @Test
  fun testItemWithFalseConditionIsHidden() {
    val item =
      Item(
        linkId = "symptoms-list",
        type = ItemType.TEXT,
        text = "List symptoms",
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
      )
    val dataContext = mapOf("has-symptoms" to false)

    val isVisible = engine.isVisible(item, dataContext)

    assertFalse(isVisible)
  }

  @Test
  fun testGetVisibleItemsFiltersCorrectly() {
    val items =
      listOf(
        Item(linkId = "name", type = ItemType.STRING, text = "Name"),
        Item(linkId = "has-symptoms", type = ItemType.BOOLEAN, text = "Has symptoms?"),
        Item(
          linkId = "symptoms-list",
          type = ItemType.TEXT,
          text = "List symptoms",
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
      )
    val dataContext = mapOf("has-symptoms" to false)

    val visibleItems = engine.getVisibleItems(items, dataContext)

    assertEquals(2, visibleItems.size)
    assertTrue(visibleItems.any { it.linkId == "name" })
    assertTrue(visibleItems.any { it.linkId == "has-symptoms" })
    assertFalse(visibleItems.any { it.linkId == "symptoms-list" })
  }

  @Test
  fun testNestedItemVisibility() {
    val items =
      listOf(
        Item(
          linkId = "group1",
          type = ItemType.GROUP,
          text = "Group",
          items =
            listOf(
              Item(linkId = "nested-visible", type = ItemType.STRING, text = "Always visible"),
              Item(
                linkId = "nested-conditional",
                type = ItemType.STRING,
                text = "Conditionally visible",
                visibleIf =
                  buildJsonObject {
                    put(
                      "==",
                      buildJsonObject {
                        put("0", buildJsonObject { put("var", "show") })
                        put("1", true)
                      },
                    )
                  },
              ),
            ),
        )
      )
    val dataContext = mapOf("show" to false)

    val visibleItems = engine.getVisibleItems(items, dataContext)

    assertEquals(1, visibleItems.size)
    val group = visibleItems[0]
    assertEquals(1, group.items.size)
    assertEquals("nested-visible", group.items[0].linkId)
  }

  @Test
  fun testComplexVisibilityCondition() {
    val item =
      Item(
        linkId = "special-field",
        type = ItemType.TEXT,
        text = "Special field",
        visibleIf =
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
                        put("0", buildJsonObject { put("var", "age") })
                        put("1", 18)
                      },
                    )
                  },
                )
                put(
                  "1",
                  buildJsonObject {
                    put(
                      "==",
                      buildJsonObject {
                        put("0", buildJsonObject { put("var", "country") })
                        put("1", "US")
                      },
                    )
                  },
                )
              },
            )
          },
      )

    val dataContext1 = mapOf("age" to 25, "country" to "US")
    assertTrue(engine.isVisible(item, dataContext1))

    val dataContext2 = mapOf("age" to 15, "country" to "US")
    assertFalse(engine.isVisible(item, dataContext2))

    val dataContext3 = mapOf("age" to 25, "country" to "UK")
    assertFalse(engine.isVisible(item, dataContext3))
  }
}
