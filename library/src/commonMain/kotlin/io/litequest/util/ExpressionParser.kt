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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object ExpressionParser {
  fun parseToJsonLogic(expression: String): JsonElement {
    val trimmed = expression.trim()

    return when {
      trimmed.contains('+') && trimmed.contains('\'') -> parseConcatenation(trimmed)
      trimmed.contains('/') -> parseDivision(trimmed)
      trimmed.contains('*') -> parseMultiplication(trimmed)
      trimmed.contains('+') -> parseAddition(trimmed)
      trimmed.contains('-') -> parseSubtraction(trimmed)
      else -> parseVariable(trimmed)
    }
  }

  private fun parseConcatenation(expression: String): JsonElement {
    val parts = splitByOperator(expression, '+')
    return buildJsonObject {
      put(
        "cat",
        buildJsonObject {
          parts.forEachIndexed { index, part -> put(index.toString(), parseOperand(part.trim())) }
        },
      )
    }
  }

  private fun parseDivision(expression: String): JsonElement {
    val parts = splitByOperator(expression, '/')
    if (parts.size != 2) return parseVariable(expression)

    return buildJsonObject {
      put(
        "/",
        buildJsonObject {
          put("0", parseOperand(parts[0].trim()))
          put("1", parseOperand(parts[1].trim()))
        },
      )
    }
  }

  private fun parseMultiplication(expression: String): JsonElement {
    val parts = splitByOperator(expression, '*')
    return buildJsonObject {
      put(
        "*",
        buildJsonObject {
          parts.forEachIndexed { index, part -> put(index.toString(), parseOperand(part.trim())) }
        },
      )
    }
  }

  private fun parseAddition(expression: String): JsonElement {
    val parts = splitByOperator(expression, '+')
    return buildJsonObject {
      put(
        "+",
        buildJsonObject {
          parts.forEachIndexed { index, part -> put(index.toString(), parseOperand(part.trim())) }
        },
      )
    }
  }

  private fun parseSubtraction(expression: String): JsonElement {
    val parts = splitByOperator(expression, '-')
    if (parts.size != 2) return parseVariable(expression)

    return buildJsonObject {
      put(
        "-",
        buildJsonObject {
          put("0", parseOperand(parts[0].trim()))
          put("1", parseOperand(parts[1].trim()))
        },
      )
    }
  }

  private fun parseOperand(operand: String): JsonElement {
    val trimmed = operand.trim()

    return when {
      trimmed.startsWith('\'') && trimmed.endsWith('\'') -> {
        buildJsonObject { put("literal", trimmed.substring(1, trimmed.length - 1)) }
      }
      trimmed.startsWith('(') && trimmed.endsWith(')') -> {
        parseToJsonLogic(trimmed.substring(1, trimmed.length - 1))
      }
      trimmed.toDoubleOrNull() != null -> {
        buildJsonObject { put("literal", trimmed.toDouble()) }
      }
      trimmed.contains('/') -> parseDivision(trimmed)
      trimmed.contains('*') -> parseMultiplication(trimmed)
      trimmed.contains('+') -> parseAddition(trimmed)
      trimmed.contains('-') -> parseSubtraction(trimmed)
      else -> parseVariable(trimmed)
    }
  }

  private fun parseVariable(varName: String): JsonElement {
    return buildJsonObject { put("var", varName.trim()) }
  }

  private fun splitByOperator(expression: String, operator: Char): List<String> {
    val parts = mutableListOf<String>()
    var current = StringBuilder()
    var parenDepth = 0
    var inString = false

    for (char in expression) {
      when {
        char == '\'' -> inString = !inString
        !inString && char == '(' -> parenDepth++
        !inString && char == ')' -> parenDepth--
        !inString && parenDepth == 0 && char == operator -> {
          parts.add(current.toString())
          current = StringBuilder()
          continue
        }
      }
      current.append(char)
    }
    parts.add(current.toString())
    return parts
  }
}
