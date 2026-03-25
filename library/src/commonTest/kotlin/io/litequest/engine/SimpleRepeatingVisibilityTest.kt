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
import io.litequest.util.TruthinessChecker
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SimpleRepeatingVisibilityTest {
  private val evaluator = JsonLogicEvaluator()
  private val engine = VisibilityEngine(evaluator)

  @Test
  fun testEmptyMapIsFalsy() {
    val emptyMap = emptyMap<String, Any?>()
    assertFalse(
      TruthinessChecker.isTruthy(emptyMap),
      "Empty map should be falsy in TruthinessChecker",
    )
    assertFalse(evaluator.isTruthy(emptyMap), "Empty map should be falsy in Evaluator")
  }

  @Test
  fun testLookupWidgetTruthiness() {
    val item =
      Item(
        linkId = "newSupplier",
        type = ItemType.GROUP,
        visibleIf = buildJsonObject { put("!", buildJsonObject { put("var", "supplierId") }) },
      )

    // 1. Not selected (Empty Map)
    val dataContextEmpty = mapOf("supplierId" to emptyMap<String, Any?>())
    assertTrue(engine.isVisible(item, dataContextEmpty), "Should be visible when Lookup is empty")

    // 2. Selected (Non-empty Map)
    val dataContextSelected = mapOf("supplierId" to mapOf("id" to "123"))
    assertFalse(
      engine.isVisible(item, dataContextSelected),
      "Should be hidden when Lookup has value",
    )
  }

  @Test
  fun testRepeatingGroupQualifiedPathResolution() {
    val item =
      Item(
        linkId = "itemId",
        type = ItemType("LOOKUP"),
        visibleIf =
          buildJsonObject {
            put(
              "==",
              buildJsonObject {
                put("0", buildJsonObject { put("var", "receivedItems.method") })
                put("1", "SEARCH")
              },
            )
          },
      )

    // Simulate Row 0: method=SEARCH
    val row0Data = mapOf("method" to "SEARCH")
    assertTrue(
      engine.isVisible(item, emptyMap(), row0Data, "receivedItems"),
      "Should be visible when row method is SEARCH (qualified path)",
    )

    // Simulate Row 1: method=SCAN
    val row1Data = mapOf("method" to "SCAN")
    assertFalse(
      engine.isVisible(item, emptyMap(), row1Data, "receivedItems"),
      "Should be hidden when row method is SCAN (qualified path)",
    )
  }

  @Test
  fun testRepeatingGroupRelativePathResolution() {
    val item =
      Item(
        linkId = "itemId",
        type = ItemType("LOOKUP"),
        visibleIf =
          buildJsonObject {
            put(
              "==",
              buildJsonObject {
                put("0", buildJsonObject { put("var", "method") })
                put("1", "SEARCH")
              },
            )
          },
      )

    val row0Data = mapOf("method" to "SEARCH")
    assertTrue(
      engine.isVisible(item, emptyMap(), row0Data),
      "Should be visible when row method is SEARCH (relative path)",
    )
  }

  @Test
  fun testGlobalFallbackFromWithinRepetition() {
    val item =
      Item(
        linkId = "itemId",
        type = ItemType("LOOKUP"),
        visibleIf = buildJsonObject { put("var", "globalShow") },
      )

    val globalContext = mapOf("globalShow" to true)
    val rowData = mapOf("method" to "SEARCH")

    assertTrue(
      engine.isVisible(item, globalContext, rowData),
      "Should fallback to global context if not found in row",
    )
  }
}
