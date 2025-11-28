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
