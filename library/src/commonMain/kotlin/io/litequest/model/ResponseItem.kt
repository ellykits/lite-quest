package io.litequest.model

import kotlinx.serialization.Serializable

@Serializable
data class ResponseItem(
  val linkId: String,
  val answers: List<Answer> = emptyList(),
  val items: List<ResponseItem> = emptyList(),
)
