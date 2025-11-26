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
