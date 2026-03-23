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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class DependencyExtractorTest {

  @Test
  fun testSimpleVarExtraction() {
    val expression = buildJsonObject { put("var", JsonPrimitive("firstName")) }

    val dependencies = DependencyExtractor.extractDependencies(expression)

    assertEquals(setOf("firstName"), dependencies)
  }

  @Test
  fun testNestedPathExtraction() {
    val expression = buildJsonObject { put("var", JsonPrimitive("patient.name.first")) }

    val dependencies = DependencyExtractor.extractDependencies(expression)

    assertEquals(setOf("patient", "name", "first"), dependencies)
  }

  @Test
  fun testMultipleVarsInExpression() {
    val expression = buildJsonObject {
      put(
        "cat",
        buildJsonArray {
          add(buildJsonObject { put("var", JsonPrimitive("firstName")) })
          add(JsonPrimitive(" "))
          add(buildJsonObject { put("var", JsonPrimitive("lastName")) })
        },
      )
    }

    val dependencies = DependencyExtractor.extractDependencies(expression)

    assertEquals(setOf("firstName", "lastName"), dependencies)
  }

  @Test
  fun testArithmeticExpression() {
    val expression = buildJsonObject {
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
    }

    val dependencies = DependencyExtractor.extractDependencies(expression)

    assertEquals(setOf("weight", "height"), dependencies)
  }

  @Test
  fun testNoVarsInExpression() {
    val expression = buildJsonObject { put("+", buildJsonArray { add(JsonPrimitive(1)) }) }

    val dependencies = DependencyExtractor.extractDependencies(expression)

    assertEquals(emptySet(), dependencies)
  }

  @Test
  fun testConditionalExpression() {
    val expression = buildJsonObject {
      put(
        "if",
        buildJsonArray {
          add(
            buildJsonObject {
              put(
                ">",
                buildJsonArray {
                  add(buildJsonObject { put("var", JsonPrimitive("age")) })
                  add(JsonPrimitive(18))
                },
              )
            }
          )
          add(JsonPrimitive("adult"))
          add(JsonPrimitive("minor"))
        },
      )
    }

    val dependencies = DependencyExtractor.extractDependencies(expression)

    assertEquals(setOf("age"), dependencies)
  }
}
