package io.litequest.util

import io.litequest.model.QuestionnaireResponse
import io.litequest.model.Subject
import kotlinx.serialization.json.JsonObject

object PathResolver {
  fun resolve(
    root: Any?,
    path: String,
  ): Any? {
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

  private fun getResponseField(
    response: QuestionnaireResponse,
    field: String,
  ): Any? {
    return when (field) {
      "id" -> response.id
      "questionnaireId" -> response.questionnaireId
      "authored" -> response.authored
      "subject" -> response.subject
      "items" -> response.items
      else -> null
    }
  }

  private fun getSubjectField(
    subject: Subject,
    field: String,
  ): Any? {
    return when (field) {
      "id" -> subject.id
      "type" -> subject.type
      else -> null
    }
  }
}
