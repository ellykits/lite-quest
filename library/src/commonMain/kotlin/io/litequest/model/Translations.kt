package io.litequest.model

import kotlinx.serialization.Serializable

@Serializable
data class Translations(
  val defaultLocale: String,
  val sources: Map<String, String>,
)
