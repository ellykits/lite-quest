/*
* Copyright 2025 LiteQuest Contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.litequest.demo.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
  @Serializable data object Home : Route

  @Serializable data object SinglePageForm : Route

  @Serializable data object SinglePageSummary : Route

  @Serializable data object SinglePageReadOnly : Route

  @Serializable data object PaginatedForm : Route

  @Serializable data object PaginatedSummary : Route

  @Serializable data object PaginatedReadOnly : Route
}
