package io.litequest.model

import kotlinx.serialization.Serializable

@Serializable
data class AnswerOption(
  val code: String,
  val display: String,
)
