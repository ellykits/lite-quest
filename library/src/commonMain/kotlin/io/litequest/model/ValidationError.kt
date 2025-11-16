package io.litequest.model

data class ValidationError(
  val linkId: String,
  val path: List<String>,
  val message: String,
  val itemText: String? = null,
)
