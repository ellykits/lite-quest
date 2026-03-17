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

import io.litequest.model.QuestionnaireResponse
import io.litequest.model.ResponseItem
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

object DataContextBuilder {
  fun build(response: QuestionnaireResponse): MutableMap<String, Any?> {
    val context = mutableMapOf<String, Any?>()
    buildContext(response.items, context)
    return context
  }

  private fun buildContext(items: List<ResponseItem>, context: MutableMap<String, Any?>) {
    items.forEach { item ->
      if (item.answers.isNotEmpty() && item.answers.first().items.isNotEmpty()) {
        val repetitions =
          item.answers.map { answer ->
            val repetitionContext = mutableMapOf<String, Any?>()
            buildContext(answer.items, repetitionContext)
            repetitionContext
          }
        context[item.linkId] = repetitions
      } else if (item.answers.isNotEmpty()) {
        val values = item.answers.mapNotNull { it.value }
        val processedValue =
          if (values.size == 1) {
            when (val singleValue = values.first()) {
              is JsonArray -> processJsonArray(singleValue)
              is JsonObject -> processJsonObject(singleValue)
              else -> singleValue.toAnyOrNull()
            }
          } else {
            values.map { it.toAnyOrNull() }
          }
        context[item.linkId] = processedValue
      }

      if (item.items.isNotEmpty() && item.answers.isEmpty()) {
        val nestedContext = mutableMapOf<String, Any?>()
        buildContext(item.items, nestedContext)

        if (nestedContext.isNotEmpty()) {
          nestedContext.forEach { (key, value) ->
            if (!key.contains('.') && !context.containsKey(key)) {
              context[key] = value
            }
          }
          context[item.linkId] = nestedContext
        }
      }
    }
  }

  private fun processJsonArray(jsonArray: JsonArray): List<Map<String, Any?>> {
    return jsonArray.mapNotNull { element ->
      if (element is JsonObject) {
        processJsonObject(element)
      } else {
        null
      }
    }
  }

  private fun processJsonObject(jsonObject: JsonObject): Map<String, Any?> {
    return jsonObject.mapValues { (_, value) ->
      when (value) {
        is JsonArray -> processJsonArray(value)
        is JsonObject -> processJsonObject(value)
        else -> value.toAnyOrNull()
      }
    }
  }
}
