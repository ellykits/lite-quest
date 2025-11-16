package io.litequest.demo.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
  @Serializable data object ModeSelection : Route

  @Serializable data object SingleFormMode : Route

  @Serializable data object PaginationMode : Route

  @Serializable data object Summary : Route
}
