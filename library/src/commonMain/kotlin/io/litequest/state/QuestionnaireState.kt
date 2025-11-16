package io.litequest.state

import io.litequest.model.Item
import io.litequest.model.Questionnaire
import io.litequest.model.QuestionnaireResponse
import io.litequest.model.ValidationError

data class QuestionnaireState(
  val questionnaire: Questionnaire,
  val response: QuestionnaireResponse,
  val visibleItems: List<Item>,
  val validationErrors: List<ValidationError>,
  val calculatedValues: Map<String, Any?>,
  val isValid: Boolean,
) {
  companion object {
    fun initial(
      questionnaire: Questionnaire,
      response: QuestionnaireResponse,
      items: List<Item>,
    ): QuestionnaireState {
      return QuestionnaireState(
        questionnaire = questionnaire,
        response = response,
        visibleItems = items,
        validationErrors = emptyList(),
        calculatedValues = emptyMap(),
        isValid = false,
      )
    }
  }
}
