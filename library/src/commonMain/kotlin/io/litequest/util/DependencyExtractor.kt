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

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object DependencyExtractor {
  fun extractDependencies(expression: JsonElement): Set<String> {
    val dependencies = mutableSetOf<String>()
    extractRecursive(expression, dependencies)
    return dependencies
  }

  private fun extractRecursive(node: JsonElement, dependencies: MutableSet<String>) {
    when (node) {
      is JsonObject -> {
        node.entries.forEach { (key, value) ->
          if (key == "var") {
            val varName = (value as? JsonPrimitive)?.content
            if (varName != null) {
              dependencies.add(varName.split(".").first())
            }
          } else {
            extractRecursive(value, dependencies)
          }
        }
      }
      is JsonArray -> {
        node.forEach { extractRecursive(it, dependencies) }
      }
      else -> {}
    }
  }
}
