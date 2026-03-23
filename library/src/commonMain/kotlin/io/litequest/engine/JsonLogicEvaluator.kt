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
package io.litequest.engine

import io.litequest.util.asObject
import io.litequest.util.toAnyOrNull
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

open class JsonLogicEvaluator {
  fun evaluate(logic: JsonElement, data: Map<String, Any?>): Any? {
    return evaluateNode(logic, data)
  }

  private fun evaluateNode(node: JsonElement, data: Map<String, Any?>): Any? {
    val obj = node.asObject()
    if (obj != null && obj.size == 1) {
      val (operator, args) = obj.entries.first()
      return evaluateOperation(operator, args, data)
    }

    return node.toAnyOrNull()
  }

  private fun evaluateOperation(
    operator: String,
    args: JsonElement,
    data: Map<String, Any?>,
  ): Any? {
    return when (operator) {
      "literal" -> args.toAnyOrNull()
      "var" -> evaluateVar(args, data)
      "==" -> evaluateEquals(args, data)
      "!=" -> evaluateNotEquals(args, data)
      ">" -> evaluateGreaterThan(args, data)
      ">=" -> evaluateGreaterOrEqual(args, data)
      "<" -> evaluateLessThan(args, data)
      "<=" -> evaluateLessOrEqual(args, data)
      "and" -> evaluateAnd(args, data)
      "or" -> evaluateOr(args, data)
      "!" -> evaluateNot(args, data)
      "if" -> evaluateIf(args, data)
      "+" -> evaluateAdd(args, data)
      "-" -> evaluateSubtract(args, data)
      "*" -> evaluateMultiply(args, data)
      "/" -> evaluateDivide(args, data)
      "%" -> evaluateModulo(args, data)
      "cat" -> evaluateCat(args, data)
      "!!" -> evaluateExists(args, data)
      else -> null
    }
  }

  private fun evaluateVar(args: JsonElement, data: Map<String, Any?>): Any? {
    val varName = (args as? JsonPrimitive)?.content ?: return null
    return resolveNestedPath(varName, data)
  }

  private fun resolveNestedPath(path: String, data: Map<String, Any?>): Any? {
    if (!path.contains('.')) {
      return data[path]
    }

    var current: Any? = data
    for (part in path.split('.')) {
      current =
        when (current) {
          is Map<*, *> -> {
            @Suppress("UNCHECKED_CAST") (current as? Map<String, Any?>)?.get(part)
          }
          else -> return null
        }
    }

    return current
  }

  private fun evaluateEquals(args: JsonElement, data: Map<String, Any?>): Boolean {
    val argsList: List<JsonElement> =
      when (args) {
        is JsonArray -> args
        is JsonObject -> args.values.toList()
        else -> return false
      }
    if (argsList.size < 2) return false

    val left = evaluateNode(argsList[0], data)
    val right = evaluateNode(argsList[1], data)

    return if (left is Number && right is Number) {
      left.toDouble() == right.toDouble()
    } else {
      left == right
    }
  }

  private fun evaluateNotEquals(args: JsonElement, data: Map<String, Any?>): Boolean {
    return !evaluateEquals(args, data)
  }

  private fun evaluateGreaterThan(args: JsonElement, data: Map<String, Any?>): Boolean {
    val argsList: List<JsonElement> =
      when (args) {
        is JsonArray -> args
        is JsonObject -> args.values.toList()
        else -> return false
      }
    if (argsList.size < 2) return false

    val left = (evaluateNode(argsList[0], data) as? Number)?.toDouble() ?: return false
    val right = (evaluateNode(argsList[1], data) as? Number)?.toDouble() ?: return false

    return left > right
  }

  private fun evaluateGreaterOrEqual(args: JsonElement, data: Map<String, Any?>): Boolean {
    val argsList: List<JsonElement> =
      when (args) {
        is JsonArray -> args
        is JsonObject -> args.values.toList()
        else -> return false
      }
    if (argsList.size < 2) return false

    val left = (evaluateNode(argsList[0], data) as? Number)?.toDouble() ?: return false
    val right = (evaluateNode(argsList[1], data) as? Number)?.toDouble() ?: return false

    return left >= right
  }

  private fun evaluateLessThan(args: JsonElement, data: Map<String, Any?>): Boolean {
    val argsList: List<JsonElement> =
      when (args) {
        is JsonArray -> args
        is JsonObject -> args.values.toList()
        else -> return false
      }
    if (argsList.size < 2) return false

    val left = (evaluateNode(argsList[0], data) as? Number)?.toDouble() ?: return false
    val right = (evaluateNode(argsList[1], data) as? Number)?.toDouble() ?: return false

    return left < right
  }

  private fun evaluateLessOrEqual(args: JsonElement, data: Map<String, Any?>): Boolean {
    val argsList: List<JsonElement> =
      when (args) {
        is JsonArray -> args
        is JsonObject -> args.values.toList()
        else -> return false
      }
    if (argsList.size < 2) return false

    val left = (evaluateNode(argsList[0], data) as? Number)?.toDouble() ?: return false
    val right = (evaluateNode(argsList[1], data) as? Number)?.toDouble() ?: return false

    return left <= right
  }

  private fun evaluateAnd(args: JsonElement, data: Map<String, Any?>): Boolean {
    val argsList: List<JsonElement> =
      when (args) {
        is JsonArray -> args
        is JsonObject -> args.values.toList()
        else -> return false
      }
    return argsList.all { isTruthy(evaluateNode(it, data)) }
  }

  private fun evaluateOr(args: JsonElement, data: Map<String, Any?>): Boolean {
    val argsList: List<JsonElement> =
      when (args) {
        is JsonArray -> args
        is JsonObject -> args.values.toList()
        else -> return false
      }
    return argsList.any { isTruthy(evaluateNode(it, data)) }
  }

  private fun evaluateNot(args: JsonElement, data: Map<String, Any?>): Boolean {
    val result = evaluateNode(args, data)
    return !isTruthy(result)
  }

  private fun evaluateIf(args: JsonElement, data: Map<String, Any?>): Any? {
    val argsList: List<JsonElement> =
      when (args) {
        is JsonArray -> args
        is JsonObject -> args.values.toList()
        else -> return null
      }

    var i = 0
    while (i < argsList.size) {
      if (i + 1 >= argsList.size) {
        return evaluateNode(argsList[i], data)
      }

      val condition = evaluateNode(argsList[i], data)
      if (isTruthy(condition)) {
        return evaluateNode(argsList[i + 1], data)
      }

      i += 2
    }

    return null
  }

  private fun evaluateAdd(args: JsonElement, data: Map<String, Any?>): Double? {
    val argsList: List<JsonElement> =
      when (args) {
        is JsonArray -> args
        is JsonObject -> args.values.toList()
        else -> return null
      }
    return argsList.fold(0.0) { acc, arg ->
      acc + ((evaluateNode(arg, data) as? Number)?.toDouble() ?: 0.0)
    }
  }

  private fun evaluateSubtract(args: JsonElement, data: Map<String, Any?>): Double? {
    val argsList: List<JsonElement> =
      when (args) {
        is JsonArray -> args
        is JsonObject -> args.values.toList()
        else -> return null
      }
    if (argsList.isEmpty()) return null

    val first = (evaluateNode(argsList[0], data) as? Number)?.toDouble() ?: return null
    if (argsList.size == 1) return -first

    return argsList.drop(1).fold(first) { acc, arg ->
      acc - ((evaluateNode(arg, data) as? Number)?.toDouble() ?: 0.0)
    }
  }

  private fun evaluateMultiply(args: JsonElement, data: Map<String, Any?>): Double? {
    val argsList: List<JsonElement> =
      when (args) {
        is JsonArray -> args
        is JsonObject -> args.values.toList()
        else -> return null
      }
    return argsList.fold(1.0) { acc, arg ->
      acc * ((evaluateNode(arg, data) as? Number)?.toDouble() ?: 1.0)
    }
  }

  private fun evaluateDivide(args: JsonElement, data: Map<String, Any?>): Double? {
    val argsList: List<JsonElement> =
      when (args) {
        is JsonArray -> args
        is JsonObject -> args.values.toList()
        else -> return null
      }
    if (argsList.size < 2) return null

    val numerator = (evaluateNode(argsList[0], data) as? Number)?.toDouble() ?: return null
    val denominator = (evaluateNode(argsList[1], data) as? Number)?.toDouble() ?: return null

    if (denominator == 0.0) return null
    return numerator / denominator
  }

  private fun evaluateModulo(args: JsonElement, data: Map<String, Any?>): Double? {
    val argsList: List<JsonElement> =
      when (args) {
        is JsonArray -> args
        is JsonObject -> args.values.toList()
        else -> return null
      }
    if (argsList.size < 2) return null

    val left = (evaluateNode(argsList[0], data) as? Number)?.toDouble() ?: return null
    val right = (evaluateNode(argsList[1], data) as? Number)?.toDouble() ?: return null

    if (right == 0.0) return null
    return left % right
  }

  private fun evaluateCat(args: JsonElement, data: Map<String, Any?>): String {
    val argsList: List<JsonElement> =
      when (args) {
        is JsonArray -> args
        is JsonObject -> args.values.toList()
        else -> return ""
      }
    return argsList.joinToString("") { evaluateNode(it, data)?.toString() ?: "" }
  }

  private fun evaluateExists(args: JsonElement, data: Map<String, Any?>): Boolean {
    val varName = (args as? JsonPrimitive)?.content ?: return false
    val value = resolveNestedPath(varName, data)
    return value != null
  }

  private fun isTruthy(value: Any?): Boolean {
    return when (value) {
      null -> false
      is Boolean -> value
      is Number -> value.toDouble() != 0.0
      is String -> value.isNotEmpty()
      else -> true
    }
  }
}
