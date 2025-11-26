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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

class TruthinessCheckerTest {
  @Test
  fun testNullIsFalsy() {
    assertFalse(TruthinessChecker.isTruthy(null))
  }

  @Test
  fun testBooleanTrue() {
    assertTrue(TruthinessChecker.isTruthy(true))
  }

  @Test
  fun testBooleanFalse() {
    assertFalse(TruthinessChecker.isTruthy(false))
  }

  @Test
  fun testNumberZeroIsFalsy() {
    assertFalse(TruthinessChecker.isTruthy(0))
    assertFalse(TruthinessChecker.isTruthy(0.0))
  }

  @Test
  fun testNonZeroNumberIsTruthy() {
    assertTrue(TruthinessChecker.isTruthy(1))
    assertTrue(TruthinessChecker.isTruthy(-1))
    assertTrue(TruthinessChecker.isTruthy(42.5))
  }

  @Test
  fun testEmptyStringIsFalsy() {
    assertFalse(TruthinessChecker.isTruthy(""))
  }

  @Test
  fun testNonEmptyStringIsTruthy() {
    assertTrue(TruthinessChecker.isTruthy("hello"))
    assertTrue(TruthinessChecker.isTruthy("false"))
    assertTrue(TruthinessChecker.isTruthy("0"))
  }

  @Test
  fun testEmptyCollectionIsFalsy() {
    assertFalse(TruthinessChecker.isTruthy(emptyList<Any>()))
    assertFalse(TruthinessChecker.isTruthy(emptySet<Any>()))
  }

  @Test
  fun testNonEmptyCollectionIsTruthy() {
    assertTrue(TruthinessChecker.isTruthy(listOf(1, 2, 3)))
    assertTrue(TruthinessChecker.isTruthy(setOf("a")))
  }

  @Test
  fun testJsonNullIsFalsy() {
    assertFalse(TruthinessChecker.isTruthy(JsonNull))
  }

  @Test
  fun testJsonPrimitiveBooleans() {
    assertTrue(TruthinessChecker.isTruthy(JsonPrimitive(true)))
    assertFalse(TruthinessChecker.isTruthy(JsonPrimitive(false)))
  }

  @Test
  fun testJsonPrimitiveNumbers() {
    assertFalse(TruthinessChecker.isTruthy(JsonPrimitive(0)))
    assertTrue(TruthinessChecker.isTruthy(JsonPrimitive(1)))
    assertTrue(TruthinessChecker.isTruthy(JsonPrimitive(-5)))
  }

  @Test
  fun testJsonPrimitiveStrings() {
    assertFalse(TruthinessChecker.isTruthy(JsonPrimitive("")))
    assertTrue(TruthinessChecker.isTruthy(JsonPrimitive("text")))
  }

  @Test
  fun testObjectsAreTruthy() {
    assertTrue(TruthinessChecker.isTruthy(Any()))
    assertTrue(TruthinessChecker.isTruthy(mapOf<String, Any>()))
  }
}
