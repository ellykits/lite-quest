package io.litequest.model

import kotlinx.serialization.Serializable

@Serializable
data class Subject(
  val id: String,
  val type: String,
)
