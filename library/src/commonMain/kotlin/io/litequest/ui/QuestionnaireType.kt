package io.litequest.ui

import io.litequest.model.Questionnaire
import io.litequest.ui.pagination.PaginatedQuestionnaire

sealed class QuestionnaireType {
  data class Single(val questionnaire: Questionnaire) : QuestionnaireType()

  data class Paginated(val paginatedQuestionnaire: PaginatedQuestionnaire) : QuestionnaireType()
}
