package io.litequest.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Answer(
  val value: JsonElement,
)
