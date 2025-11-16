package io.litequest.engine

import io.litequest.util.asObject
import io.litequest.util.toAnyOrNull
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

open class JsonLogicEvaluator {
  fun evaluate(
    logic: JsonElement,
    data: Map<String, Any?>,
  ): Any? {
    return evaluateNode(logic, data)
  }

  private fun evaluateNode(
    node: JsonElement,
    data: Map<String, Any?>,
  ): Any? {
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
      else -> null
    }
  }

  private fun evaluateVar(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Any? {
    val varName = (args as? JsonPrimitive)?.content ?: return null
    return data[varName]
  }

  private fun evaluateEquals(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Boolean {
    val argsList = args.asObject()?.values?.toList() ?: return false
    if (argsList.size < 2) return false
    val left = evaluateNode(argsList[0], data)
    val right = evaluateNode(argsList[1], data)
    return left == right
  }

  private fun evaluateNotEquals(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Boolean {
    return !evaluateEquals(args, data)
  }

  private fun evaluateGreaterThan(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Boolean {
    val argsList = args.asObject()?.values?.toList() ?: return false
    if (argsList.size < 2) return false
    val left = (evaluateNode(argsList[0], data) as? Number)?.toDouble() ?: return false
    val right = (evaluateNode(argsList[1], data) as? Number)?.toDouble() ?: return false
    return left > right
  }

  private fun evaluateGreaterOrEqual(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Boolean {
    val argsList = args.asObject()?.values?.toList() ?: return false
    if (argsList.size < 2) return false
    val left = (evaluateNode(argsList[0], data) as? Number)?.toDouble() ?: return false
    val right = (evaluateNode(argsList[1], data) as? Number)?.toDouble() ?: return false
    return left >= right
  }

  private fun evaluateLessThan(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Boolean {
    val argsList = args.asObject()?.values?.toList() ?: return false
    if (argsList.size < 2) return false
    val left = (evaluateNode(argsList[0], data) as? Number)?.toDouble() ?: return false
    val right = (evaluateNode(argsList[1], data) as? Number)?.toDouble() ?: return false
    return left < right
  }

  private fun evaluateLessOrEqual(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Boolean {
    val argsList = args.asObject()?.values?.toList() ?: return false
    if (argsList.size < 2) return false
    val left = (evaluateNode(argsList[0], data) as? Number)?.toDouble() ?: return false
    val right = (evaluateNode(argsList[1], data) as? Number)?.toDouble() ?: return false
    return left <= right
  }

  private fun evaluateAnd(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Boolean {
    val argsList = args.asObject()?.values?.toList() ?: return false
    return argsList.all { arg ->
      val result = evaluateNode(arg, data)
      isTruthy(result)
    }
  }

  private fun evaluateOr(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Boolean {
    val argsList = args.asObject()?.values?.toList() ?: return false
    return argsList.any { arg ->
      val result = evaluateNode(arg, data)
      isTruthy(result)
    }
  }

  private fun evaluateNot(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Boolean {
    val result = evaluateNode(args, data)
    return !isTruthy(result)
  }

  private fun evaluateIf(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Any? {
    val argsList = args.asObject()?.values?.toList() ?: return null

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

  private fun evaluateAdd(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Double? {
    val argsList = args.asObject()?.values?.toList() ?: return null
    return argsList.fold(0.0) { acc, arg ->
      val value = (evaluateNode(arg, data) as? Number)?.toDouble() ?: 0.0
      acc + value
    }
  }

  private fun evaluateSubtract(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Double? {
    val argsList = args.asObject()?.values?.toList() ?: return null
    if (argsList.isEmpty()) return null

    val first = (evaluateNode(argsList[0], data) as? Number)?.toDouble() ?: return null
    if (argsList.size == 1) return -first

    return argsList.drop(1).fold(first) { acc, arg ->
      val value = (evaluateNode(arg, data) as? Number)?.toDouble() ?: 0.0
      acc - value
    }
  }

  private fun evaluateMultiply(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Double? {
    val argsList = args.asObject()?.values?.toList() ?: return null
    return argsList.fold(1.0) { acc, arg ->
      val value = (evaluateNode(arg, data) as? Number)?.toDouble() ?: 1.0
      acc * value
    }
  }

  private fun evaluateDivide(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Double? {
    val argsList = args.asObject()?.values?.toList() ?: return null
    if (argsList.size < 2) return null

    val numerator = (evaluateNode(argsList[0], data) as? Number)?.toDouble() ?: return null
    val denominator = (evaluateNode(argsList[1], data) as? Number)?.toDouble() ?: return null

    if (denominator == 0.0) return null
    return numerator / denominator
  }

  private fun evaluateModulo(
    args: JsonElement,
    data: Map<String, Any?>,
  ): Double? {
    val argsList = args.asObject()?.values?.toList() ?: return null
    if (argsList.size < 2) return null

    val left = (evaluateNode(argsList[0], data) as? Number)?.toDouble() ?: return null
    val right = (evaluateNode(argsList[1], data) as? Number)?.toDouble() ?: return null

    if (right == 0.0) return null
    return left % right
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
