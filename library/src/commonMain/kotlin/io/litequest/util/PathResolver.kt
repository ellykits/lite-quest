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
package io.litequest.util

import io.litequest.model.QuestionnaireResponse
import io.litequest.model.Subject
import kotlinx.serialization.json.JsonObject

object PathResolver {
  fun resolve(root: Any?, path: String): Any? {
    if (root == null) return null

    val parts = path.split(".")
    var current: Any? = root

    for (part in parts) {
      current =
        when (current) {
          is Map<*, *> -> current[part]
          is JsonObject -> current[part]?.toAnyOrNull()
          is QuestionnaireResponse -> getResponseField(current, part)
          is Subject -> getSubjectField(current, part)
          else -> null
        }
      if (current == null) break
    }

    return current
  }

  private fun getResponseField(response: QuestionnaireResponse, field: String): Any? {
    return when (field) {
      "id" -> response.id
      "questionnaireId" -> response.questionnaireId
      "authored" -> response.authored
      "subject" -> response.subject
      "items" -> response.items
      else -> null
    }
  }

  private fun getSubjectField(subject: Subject, field: String): Any? {
    return when (field) {
      "id" -> subject.id
      "type" -> subject.type
      else -> null
    }
  }
}
