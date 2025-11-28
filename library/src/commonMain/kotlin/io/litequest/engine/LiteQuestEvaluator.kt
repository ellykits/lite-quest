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
package io.litequest.engine

import io.litequest.model.Item
import io.litequest.model.Questionnaire
import io.litequest.model.QuestionnaireResponse
import io.litequest.model.ValidationError
import io.litequest.util.DataContextBuilder

class LiteQuestEvaluator(
  private val questionnaire: Questionnaire,
  jsonLogicEvaluator: JsonLogicEvaluator = JsonLogicEvaluator(),
) {
  private val calculatedValuesEngine = CalculatedValuesEngine(jsonLogicEvaluator)
  private val visibilityEngine = VisibilityEngine(jsonLogicEvaluator)
  private val validationEngine = ValidationEngine(jsonLogicEvaluator)
  private val extractionEngine = ExtractionEngine()

  fun validateResponse(
    response: QuestionnaireResponse,
    items: List<Item>? = null,
  ): List<ValidationError> {
    val dataContext = buildDataContext(response)
    return validationEngine.validateResponse(
      items = items ?: questionnaire.items,
      responseItems = response.items,
      dataContext = dataContext,
    )
  }

  fun getVisibleItems(response: QuestionnaireResponse): List<Item> {
    val dataContext = buildDataContext(response)
    return visibilityEngine.getVisibleItems(questionnaire.items, dataContext)
  }

  fun calculateValues(response: QuestionnaireResponse): Map<String, Any?> {
    val dataContext = DataContextBuilder.build(response)
    return calculatedValuesEngine.evaluate(questionnaire.calculatedValues, dataContext)
  }

  fun extractData(response: QuestionnaireResponse): kotlinx.serialization.json.JsonElement? {
    val template = questionnaire.extractionTemplate ?: return null
    val dataContext = buildDataContext(response)
    val calculatedValues = calculateValues(response)

    return extractionEngine.extract(
      response = response,
      template = template,
      calculatedValues = calculatedValues,
      answerMap = dataContext,
    )
  }

  fun buildDataContext(response: QuestionnaireResponse): MutableMap<String, Any?> {
    val dataContext = DataContextBuilder.build(response)
    calculatedValuesEngine.evaluate(questionnaire.calculatedValues, dataContext)
    return dataContext
  }
}
