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

import io.litequest.engine.JsonLogicEvaluator
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpressionParserTest {

  private val evaluator = JsonLogicEvaluator()

  @Test
  fun testSimpleAddition() {
    val expression = ExpressionParser.parseToJsonLogic("a + b")
    val data = mapOf("a" to 5, "b" to 3)
    val result = evaluator.evaluate(expression, data)
    assertEquals(8.0, result)
  }

  @Test
  fun testSimpleSubtraction() {
    val expression = ExpressionParser.parseToJsonLogic("a - b")
    val data = mapOf("a" to 10, "b" to 3)
    val result = evaluator.evaluate(expression, data)
    assertEquals(7.0, result)
  }

  @Test
  fun testSimpleMultiplication() {
    val expression = ExpressionParser.parseToJsonLogic("a * b")
    val data = mapOf("a" to 5, "b" to 3)
    val result = evaluator.evaluate(expression, data)
    assertEquals(15.0, result)
  }

  @Test
  fun testSimpleDivision() {
    val expression = ExpressionParser.parseToJsonLogic("a / b")
    val data = mapOf("a" to 15, "b" to 3)
    val result = evaluator.evaluate(expression, data)
    assertEquals(5.0, result)
  }

  @Test
  fun testBMICalculation() {
    val expression = ExpressionParser.parseToJsonLogic("weight / (height * height)")
    val data = mapOf("weight" to 70, "height" to 1.75)
    val result = evaluator.evaluate(expression, data)
    assertEquals(22.857142857142858, result)
  }

  @Test
  fun testStringConcatenation() {
    val expression = ExpressionParser.parseToJsonLogic("firstName + ' ' + lastName")
    val data = mapOf("firstName" to "John", "lastName" to "Doe")
    val result = evaluator.evaluate(expression, data)
    assertEquals("John Doe", result)
  }

  @Test
  fun testConcatenationWithThreeParts() {
    val expression = ExpressionParser.parseToJsonLogic("title + ' ' + firstName + ' ' + lastName")
    val data = mapOf("title" to "Dr.", "firstName" to "Jane", "lastName" to "Smith")
    val result = evaluator.evaluate(expression, data)
    assertEquals("Dr. Jane Smith", result)
  }

  @Test
  fun testMissingVariableReturnsNull() {
    val expression = ExpressionParser.parseToJsonLogic("a + b")
    val data = mapOf("a" to 5)
    val result = evaluator.evaluate(expression, data)
    assertEquals(5.0, result)
  }

  @Test
  fun testComplexExpression() {
    val expression = ExpressionParser.parseToJsonLogic("(a + b) * c")
    val data = mapOf("a" to 2, "b" to 3, "c" to 4)
    val result = evaluator.evaluate(expression, data)
    assertEquals(20.0, result)
  }
}
