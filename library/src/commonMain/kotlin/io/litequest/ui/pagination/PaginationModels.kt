package io.litequest.ui.pagination

import io.litequest.model.Item
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class QuestionnairePage(
  val id: String,
  val title: String,
  val description: String? = null,
  val items: List<Item>,
  val order: Int,
  val enableWhen: JsonObject? = null,
)

@Serializable
data class PaginatedQuestionnaire(
  val id: String,
  val title: String,
  val version: String? = null,
  val pages: List<QuestionnairePage>,
)
