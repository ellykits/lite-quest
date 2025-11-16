package io.litequest.engine

import io.litequest.model.QuestionnaireResponse
import io.litequest.util.PathResolver
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ExtractionEngine {
  fun extract(
    response: QuestionnaireResponse,
    template: JsonElement,
    calculatedValues: Map<String, Any?>,
    answerMap: Map<String, Any?>,
  ): JsonElement {
    return processTemplate(template, response, calculatedValues, answerMap)
  }

  private fun processTemplate(
    node: JsonElement,
    response: QuestionnaireResponse,
    calculatedValues: Map<String, Any?>,
    answerMap: Map<String, Any?>,
  ): JsonElement {
    return when (node) {
      is JsonObject -> {
        if (isSourceMapping(node)) {
          extractFromSource(node, response, calculatedValues, answerMap)
        } else {
          buildJsonObject {
            node.entries.forEach { (key, value) ->
              val processed = processTemplate(value, response, calculatedValues, answerMap)
              put(key, processed)
            }
          }
        }
      }
      is JsonArray -> {
        JsonArray(node.map { processTemplate(it, response, calculatedValues, answerMap) })
      }
      else -> node
    }
  }

  private fun isSourceMapping(obj: JsonObject): Boolean {
    return obj.containsKey("source")
  }

  private fun extractFromSource(
    mapping: JsonObject,
    response: QuestionnaireResponse,
    calculatedValues: Map<String, Any?>,
    answerMap: Map<String, Any?>,
  ): JsonElement {
    val source = mapping["source"]?.let { (it as? JsonPrimitive)?.content } ?: return JsonNull

    return when (source) {
      "answer" -> {
        val linkId = mapping["linkId"]?.let { (it as? JsonPrimitive)?.content }
        if (linkId != null) {
          val value = answerMap[linkId]
          toJsonElement(value)
        } else {
          JsonNull
        }
      }
      "calculatedValue" -> {
        val name = mapping["name"]?.let { (it as? JsonPrimitive)?.content }
        if (name != null) {
          val value = calculatedValues[name]
          toJsonElement(value)
        } else {
          JsonNull
        }
      }
      "metadata" -> {
        val path = mapping["path"]?.let { (it as? JsonPrimitive)?.content }
        if (path != null) {
          val value = PathResolver.resolve(response, path)
          toJsonElement(value)
        } else {
          JsonNull
        }
      }
      else -> JsonNull
    }
  }

  private fun toJsonElement(value: Any?): JsonElement {
    return when (value) {
      null -> JsonNull
      is Boolean -> JsonPrimitive(value)
      is Number -> JsonPrimitive(value)
      is String -> JsonPrimitive(value)
      is List<*> -> JsonArray(value.map { toJsonElement(it) })
      is Map<*, *> ->
        buildJsonObject { value.forEach { (k, v) -> put(k.toString(), toJsonElement(v)) } }
      else -> JsonPrimitive(value.toString())
    }
  }
}
