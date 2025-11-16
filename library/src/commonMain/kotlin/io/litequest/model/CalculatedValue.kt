package io.litequest.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CalculatedValue(
  val name: String,
  val expression: JsonElement,
)
