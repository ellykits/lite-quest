package io.litequest.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ValidationRule(
  val message: String,
  val expression: JsonElement,
)
