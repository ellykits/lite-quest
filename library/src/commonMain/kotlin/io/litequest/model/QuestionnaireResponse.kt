package io.litequest.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestionnaireResponse(
  val id: String,
  val questionnaireId: String,
  val authored: String,
  val subject: Subject? = null,
  val items: List<ResponseItem>,
)
