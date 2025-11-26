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
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

fun JsonElement.toAnyOrNull(): Any? {
  return when (this) {
    is JsonNull -> null
    is JsonPrimitive -> {
      booleanOrNull ?: longOrNull ?: doubleOrNull ?: content
    }
    is JsonArray -> map { it.toAnyOrNull() }
    is JsonObject -> mapValues { it.value.toAnyOrNull() }
  }
}

fun JsonElement.asString(): String? {
  return (this as? JsonPrimitive)?.content
}

fun JsonElement.asDouble(): Double? {
  return (this as? JsonPrimitive)?.doubleOrNull
}

fun JsonElement.asInt(): Int? {
  return (this as? JsonPrimitive)?.intOrNull
}

fun JsonElement.asBoolean(): Boolean? {
  return (this as? JsonPrimitive)?.booleanOrNull
}

fun JsonElement.asObject(): JsonObject? {
  return this as? JsonObject
}

fun JsonElement.asArray(): JsonArray? {
  return this as? JsonArray
}
