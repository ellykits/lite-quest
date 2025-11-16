package io.litequest.util

import io.litequest.model.QuestionnaireResponse
import io.litequest.model.ResponseItem
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

object DataContextBuilder {
  fun build(response: QuestionnaireResponse): MutableMap<String, Any?> {
    val context = mutableMapOf<String, Any?>()
    flattenResponseItems(response.items, context)
    return context
  }

  private fun flattenResponseItems(
    items: List<ResponseItem>,
    context: MutableMap<String, Any?>,
  ) {
    items.forEach { item ->
      if (item.answers.isNotEmpty()) {
        val values = item.answers.map { it.value }
        val processedValue =
          if (values.size == 1) {
            val singleValue = values.first()
            when (singleValue) {
              is JsonArray -> processJsonArray(singleValue)
              is JsonObject -> processJsonObject(singleValue)
              else -> singleValue.toAnyOrNull()
            }
          } else {
            values.map { it.toAnyOrNull() }
          }
        context[item.linkId] = processedValue
      } else if (item.items.isNotEmpty()) {
        flattenResponseItems(item.items, context)
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
