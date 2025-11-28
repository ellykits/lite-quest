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

import io.litequest.model.CalculatedValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CalculatedValuesEngineTest {
  private val evaluator = JsonLogicEvaluator()
  private val engine = CalculatedValuesEngine(evaluator)

  @Test
  fun testSimpleCalculation() {
    val calculatedValues =
      listOf(
        CalculatedValue(
          name = "sum",
          expression =
            buildJsonObject {
              put(
                "+",
                buildJsonObject {
                  put("0", buildJsonObject { put("var", "a") })
                  put("1", buildJsonObject { put("var", "b") })
                },
              )
            },
        )
      )
    val dataContext = mutableMapOf<String, Any?>("a" to 5, "b" to 3)

    val results = engine.evaluate(calculatedValues, dataContext)

    assertEquals(8.0, results["sum"])
    assertEquals(8.0, dataContext["sum"]) // Should also update context
  }

  @Test
  fun testBMICalculation() {
    val calculatedValues =
      listOf(
        CalculatedValue(
          name = "bmi",
          expression =
            buildJsonObject {
              put(
                "/",
                buildJsonObject {
                  put("0", buildJsonObject { put("var", "weight") })
                  put(
                    "1",
                    buildJsonObject {
                      put(
                        "*",
                        buildJsonObject {
                          put("0", buildJsonObject { put("var", "height") })
                          put("1", buildJsonObject { put("var", "height") })
                        },
                      )
                    },
                  )
                },
              )
            },
        )
      )
    val dataContext = mutableMapOf<String, Any?>("weight" to 80.5, "height" to 1.8)

    val results = engine.evaluate(calculatedValues, dataContext)

    val bmi = results["bmi"] as Double
    assertEquals(24.845679012345678, bmi, 0.0001)
  }

  @Test
  fun testSequentialCalculations() {
    val calculatedValues =
      listOf(
        CalculatedValue(
          name = "subtotal",
          expression =
            buildJsonObject {
              put(
                "*",
                buildJsonObject {
                  put("0", buildJsonObject { put("var", "price") })
                  put("1", buildJsonObject { put("var", "quantity") })
                },
              )
            },
        ),
        CalculatedValue(
          name = "tax",
          expression =
            buildJsonObject {
              put(
                "*",
                buildJsonObject {
                  put("0", buildJsonObject { put("var", "subtotal") })
                  put("1", 0.15)
                },
              )
            },
        ),
        CalculatedValue(
          name = "total",
          expression =
            buildJsonObject {
              put(
                "+",
                buildJsonObject {
                  put("0", buildJsonObject { put("var", "subtotal") })
                  put("1", buildJsonObject { put("var", "tax") })
                },
              )
            },
        ),
      )
    val dataContext = mutableMapOf<String, Any?>("price" to 100.0, "quantity" to 2.0)

    val results = engine.evaluate(calculatedValues, dataContext)

    assertEquals(200.0, results["subtotal"])
    assertEquals(30.0, results["tax"])
    assertEquals(230.0, results["total"])
  }

  @Test
  fun testConditionalCalculation() {
    val calculatedValues =
      listOf(
        CalculatedValue(
          name = "discount",
          expression =
            buildJsonObject {
              put(
                "if",
                buildJsonObject {
                  put(
                    "0",
                    buildJsonObject {
                      put(
                        ">",
                        buildJsonObject {
                          put("0", buildJsonObject { put("var", "amount") })
                          put("1", 100)
                        },
                      )
                    },
                  )
                  put("1", 10.0)
                  put("2", 0.0)
                },
              )
            },
        )
      )

    val dataContext1 = mutableMapOf<String, Any?>("amount" to 150.0)
    val results1 = engine.evaluate(calculatedValues, dataContext1)
    assertEquals(10.0, results1["discount"])

    val dataContext2 = mutableMapOf<String, Any?>("amount" to 50.0)
    val results2 = engine.evaluate(calculatedValues, dataContext2)
    assertEquals(0.0, results2["discount"])
  }

  @Test
  fun testEmptyCalculatedValues() {
    val calculatedValues = emptyList<CalculatedValue>()
    val dataContext = mutableMapOf<String, Any?>("a" to 5)

    val results = engine.evaluate(calculatedValues, dataContext)

    assertEquals(0, results.size)
    assertEquals(5, dataContext["a"]) // Original context unchanged
  }
}
