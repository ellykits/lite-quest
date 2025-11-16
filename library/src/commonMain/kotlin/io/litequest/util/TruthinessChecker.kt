package io.litequest.util

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

object TruthinessChecker {
  fun isTruthy(value: Any?): Boolean {
    return when (value) {
      null -> false
      is Boolean -> value
      is Number -> value.toDouble() != 0.0
      is String -> value.isNotEmpty()
      is Collection<*> -> value.isNotEmpty()
      is JsonNull -> false
      is JsonElement -> isTruthyJson(value)
      else -> true
    }
  }

  private fun isTruthyJson(element: JsonElement): Boolean {
    return when (element) {
      is JsonNull -> false
      is JsonPrimitive -> {
        element.asBoolean()
          ?: run {
            val doubleVal = element.asDouble()
            if (doubleVal != null) {
              doubleVal != 0.0
            } else {
              element.asString()?.isNotEmpty() ?: false
            }
          }
      }
      else -> true
    }
  }
}
