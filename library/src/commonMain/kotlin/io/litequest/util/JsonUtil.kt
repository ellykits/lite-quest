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

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

internal object JsonUtil {
  private val json = Json {
    isLenient = true
    encodeDefaults = true
  }

  fun <T> encode(serializer: SerializationStrategy<T>, value: T): JsonElement {
    return json.encodeToJsonElement(serializer, value)
  }

  fun <T> decode(deserializer: DeserializationStrategy<T>, element: JsonElement): T {
    return json.decodeFromJsonElement(deserializer, element)
  }

  fun <T> decodeOrNull(deserializer: DeserializationStrategy<T>, element: JsonElement?): T? {
    return element?.let {
      try {
        decode(deserializer, it)
      } catch (e: Exception) {
        null
      }
    }
  }
}
