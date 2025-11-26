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
