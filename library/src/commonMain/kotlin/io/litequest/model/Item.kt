package io.litequest.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Item(
  val linkId: String,
  val type: ItemType,
  val text: String,
  val required: Boolean = false,
  val repeats: Boolean = false,
  val visibleIf: JsonElement? = null,
  val answerOptions: List<AnswerOption> = emptyList(),
  val validations: List<ValidationRule> = emptyList(),
  val items: List<Item> = emptyList(),
)
