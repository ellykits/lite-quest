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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class JsonLogicEvaluatorTest {
  private val evaluator = JsonLogicEvaluator()

  @Test
  fun testVarOperator() {
    val data = mapOf("name" to "John", "age" to 25)
    val expression = buildJsonObject { put("var", "name") }

    val result = evaluator.evaluate(expression, data)
    assertEquals("John", result)
  }

  @Test
  fun testEqualsOperator() {
    val data = mapOf("status" to "active")
    val expression = buildJsonObject {
      put(
        "==",
        buildJsonObject {
          put("0", buildJsonObject { put("var", "status") })
          put("1", "active")
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertTrue(result as Boolean)
  }

  @Test
  fun testNotEqualsOperator() {
    val data = mapOf("status" to "active")
    val expression = buildJsonObject {
      put(
        "!=",
        buildJsonObject {
          put("0", buildJsonObject { put("var", "status") })
          put("1", "inactive")
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertTrue(result as Boolean)
  }

  @Test
  fun testGreaterThanOperator() {
    val data = mapOf("age" to 25)
    val expression = buildJsonObject {
      put(
        ">",
        buildJsonObject {
          put("0", buildJsonObject { put("var", "age") })
          put("1", 18)
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertTrue(result as Boolean)
  }

  @Test
  fun testGreaterOrEqualOperator() {
    val data = mapOf("age" to 18)
    val expression = buildJsonObject {
      put(
        ">=",
        buildJsonObject {
          put("0", buildJsonObject { put("var", "age") })
          put("1", 18)
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertTrue(result as Boolean)
  }

  @Test
  fun testLessThanOperator() {
    val data = mapOf("age" to 15)
    val expression = buildJsonObject {
      put(
        "<",
        buildJsonObject {
          put("0", buildJsonObject { put("var", "age") })
          put("1", 18)
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertTrue(result as Boolean)
  }

  @Test
  fun testLessOrEqualOperator() {
    val data = mapOf("age" to 18)
    val expression = buildJsonObject {
      put(
        "<=",
        buildJsonObject {
          put("0", buildJsonObject { put("var", "age") })
          put("1", 18)
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertTrue(result as Boolean)
  }

  @Test
  fun testAndOperator() {
    val data = mapOf("age" to 25, "active" to true)
    val expression = buildJsonObject {
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
          put("1", buildJsonObject { put("var", "active") })
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertTrue(result as Boolean)
  }

  @Test
  fun testOrOperator() {
    val data = mapOf("age" to 15, "hasPermission" to true)
    val expression = buildJsonObject {
      put(
        "or",
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
          put("1", buildJsonObject { put("var", "hasPermission") })
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertTrue(result as Boolean)
  }

  @Test
  fun testNotOperator() {
    val data = mapOf("inactive" to false)
    val expression = buildJsonObject { put("!", buildJsonObject { put("var", "inactive") }) }

    val result = evaluator.evaluate(expression, data)
    assertTrue(result as Boolean)
  }

  @Test
  fun testIfOperator() {
    val data = mapOf("age" to 25)
    val expression = buildJsonObject {
      put(
        "if",
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
          put("1", "adult")
          put("2", "minor")
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertEquals("adult", result)
  }

  @Test
  fun testAddOperator() {
    val data = mapOf("a" to 5, "b" to 3)
    val expression = buildJsonObject {
      put(
        "+",
        buildJsonObject {
          put("0", buildJsonObject { put("var", "a") })
          put("1", buildJsonObject { put("var", "b") })
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertEquals(8.0, result)
  }

  @Test
  fun testSubtractOperator() {
    val data = mapOf("a" to 10, "b" to 3)
    val expression = buildJsonObject {
      put(
        "-",
        buildJsonObject {
          put("0", buildJsonObject { put("var", "a") })
          put("1", buildJsonObject { put("var", "b") })
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertEquals(7.0, result)
  }

  @Test
  fun testMultiplyOperator() {
    val data = mapOf("a" to 5, "b" to 3)
    val expression = buildJsonObject {
      put(
        "*",
        buildJsonObject {
          put("0", buildJsonObject { put("var", "a") })
          put("1", buildJsonObject { put("var", "b") })
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertEquals(15.0, result)
  }

  @Test
  fun testDivideOperator() {
    val data = mapOf("a" to 10, "b" to 2)
    val expression = buildJsonObject {
      put(
        "/",
        buildJsonObject {
          put("0", buildJsonObject { put("var", "a") })
          put("1", buildJsonObject { put("var", "b") })
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertEquals(5.0, result)
  }

  @Test
  fun testDivideByZero() {
    val data = mapOf("a" to 10, "b" to 0)
    val expression = buildJsonObject {
      put(
        "/",
        buildJsonObject {
          put("0", buildJsonObject { put("var", "a") })
          put("1", buildJsonObject { put("var", "b") })
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertNull(result)
  }

  @Test
  fun testModuloOperator() {
    val data = mapOf("a" to 10, "b" to 3)
    val expression = buildJsonObject {
      put(
        "%",
        buildJsonObject {
          put("0", buildJsonObject { put("var", "a") })
          put("1", buildJsonObject { put("var", "b") })
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertEquals(1.0, result)
  }

  @Test
  fun testComplexExpression() {
    val data = mapOf("weight" to 80.5, "height" to 1.8)
    val expression = buildJsonObject {
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
    }

    val result = evaluator.evaluate(expression, data) as Double
    assertEquals(24.845679012345678, result, 0.0001)
  }

  @Test
  fun testDotNotationSingleLevel() {
    val data = mapOf("consentSection" to mapOf("consentGiven" to true))
    val expression = buildJsonObject { put("var", "consentSection.consentGiven") }

    val result = evaluator.evaluate(expression, data)
    assertEquals(true, result)
  }

  @Test
  fun testDotNotationMultipleLevels() {
    val data =
      mapOf(
        "patient" to
          mapOf(
            "demographics" to mapOf("name" to mapOf("firstName" to "John", "lastName" to "Doe"))
          )
      )
    val expression = buildJsonObject { put("var", "patient.demographics.name.firstName") }

    val result = evaluator.evaluate(expression, data)
    assertEquals("John", result)
  }

  @Test
  fun testDotNotationWithEqualsOperator() {
    val data = mapOf("consentSection" to mapOf("consentGiven" to true))
    val expression = buildJsonObject {
      put(
        "==",
        buildJsonObject {
          put("0", buildJsonObject { put("var", "consentSection.consentGiven") })
          put("1", true)
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertTrue(result as Boolean)
  }

  @Test
  fun testDotNotationWithNonExistentPath() {
    val data = mapOf("consentSection" to mapOf("consentGiven" to true))
    val expression = buildJsonObject { put("var", "consentSection.nonExistent") }

    val result = evaluator.evaluate(expression, data)
    assertNull(result)
  }

  @Test
  fun testDotNotationWithNonExistentParent() {
    val data = mapOf("consentSection" to mapOf("consentGiven" to true))
    val expression = buildJsonObject { put("var", "nonExistent.consentGiven") }

    val result = evaluator.evaluate(expression, data)
    assertNull(result)
  }

  @Test
  fun testDotNotationWithExistsOperator() {
    val data = mapOf("consentSection" to mapOf("consentGiven" to true))
    val expression = buildJsonObject { put("!!", "consentSection.consentGiven") }

    val result = evaluator.evaluate(expression, data)
    assertTrue(result as Boolean)
  }

  @Test
  fun testDotNotationWithExistsOperatorNonExistent() {
    val data = mapOf("consentSection" to mapOf("consentGiven" to true))
    val expression = buildJsonObject { put("!!", "consentSection.nonExistent") }

    val result = evaluator.evaluate(expression, data)
    assertEquals(false, result)
  }

  @Test
  fun testDotNotationWithComplexCondition() {
    val data = mapOf("household" to mapOf("head" to mapOf("age" to 35, "employed" to true)))
    val expression = buildJsonObject {
      put(
        "and",
        buildJsonObject {
          put(
            "0",
            buildJsonObject {
              put(
                ">",
                buildJsonObject {
                  put("0", buildJsonObject { put("var", "household.head.age") })
                  put("1", 18)
                },
              )
            },
          )
          put("1", buildJsonObject { put("var", "household.head.employed") })
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertTrue(result as Boolean)
  }

  @Test
  fun testDotNotationDeepNestingWithEquals() {
    val data = mapOf("patient" to mapOf("demographics" to mapOf("age" to 25)))

    val varResult =
      evaluator.evaluate(buildJsonObject { put("var", "patient.demographics.age") }, data)
    assertEquals(25, varResult)

    val simpleExpression = buildJsonObject {
      put(
        "==",
        buildJsonObject {
          put("0", 25)
          put("1", 25)
        },
      )
    }
    val simpleResult = evaluator.evaluate(simpleExpression, data)
    assertTrue(simpleResult as Boolean, "Simple comparison should work")

    val expression = buildJsonObject {
      put(
        "==",
        buildJsonObject {
          put("0", buildJsonObject { put("var", "patient.demographics.age") })
          put("1", 25)
        },
      )
    }

    val result = evaluator.evaluate(expression, data)
    assertTrue(
      result as? Boolean ?: false,
      "Expected true but got: $result, varResult was: $varResult",
    )
  }
}
