package io.litequest.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Questionnaire(
  val id: String,
  val version: String? = null,
  val title: String,
  val description: String? = null,
  val translations: Translations? = null,
  val calculatedValues: List<CalculatedValue> = emptyList(),
  val extractionTemplate: JsonElement? = null,
  val items: List<Item>,
)
