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
