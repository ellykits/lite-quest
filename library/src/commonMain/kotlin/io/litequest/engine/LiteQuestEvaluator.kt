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

import io.litequest.model.CalculatedValue
import io.litequest.model.Item
import io.litequest.model.Questionnaire
import io.litequest.model.QuestionnaireResponse
import io.litequest.model.ValidationError
import io.litequest.util.CalculatedExpressionCollector
import io.litequest.util.DataContextBuilder
import kotlinx.serialization.json.JsonElement

class LiteQuestEvaluator(
  private val questionnaire: Questionnaire,
  jsonLogicEvaluator: JsonLogicEvaluator = JsonLogicEvaluator(),
) {
  private val visibilityEngine = VisibilityEngine(jsonLogicEvaluator)
  private val validationEngine = ValidationEngine(jsonLogicEvaluator)
  private val extractionEngine = ExtractionEngine()

  private val allCalculatedValues: List<CalculatedValue> by lazy {
    val itemExpressions = CalculatedExpressionCollector.collect(questionnaire.items)
    questionnaire.calculatedValues + itemExpressions
  }

  private val calculatedValuesEngine: CalculatedValuesEngine by lazy {
    CalculatedValuesEngine(jsonLogicEvaluator, allCalculatedValues)
  }

  fun validateResponse(
    response: QuestionnaireResponse,
    items: List<Item>? = null,
    calculatedValues: Map<String, Any?>? = null,
  ): List<ValidationError> {
    val dataContext = buildDataContext(response, calculatedValues)
    return validationEngine.validateResponse(
      items = items ?: questionnaire.items,
      responseItems = response.items,
      dataContext = dataContext,
    )
  }

  fun getVisibleItems(
    response: QuestionnaireResponse,
    calculatedValues: Map<String, Any?>? = null,
  ): List<Item> {
    val dataContext = buildDataContext(response, calculatedValues)
    return visibilityEngine.getVisibleItems(questionnaire.items, dataContext)
  }

  fun calculateValues(
    response: QuestionnaireResponse,
    dataContext: MutableMap<String, Any?>? = null,
  ): Map<String, Any?> {
    val context = dataContext ?: DataContextBuilder.build(response)
    return calculatedValuesEngine.evaluate(allCalculatedValues, context)
  }

  fun calculateValuesIncremental(
    response: QuestionnaireResponse,
    changedFields: Set<String>,
    currentCalculatedValues: Map<String, Any?>,
  ): Map<String, Any?> {
    val dataContext = DataContextBuilder.build(response)
    dataContext.putAll(currentCalculatedValues)
    return calculatedValuesEngine.evaluateIncremental(dataContext, changedFields)
  }

  fun extractData(response: QuestionnaireResponse): JsonElement? {
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

  fun buildDataContext(
    response: QuestionnaireResponse,
    calculatedValues: Map<String, Any?>? = null,
  ): MutableMap<String, Any?> {
    val dataContext = DataContextBuilder.build(response)
    if (calculatedValues != null) {
      dataContext.putAll(calculatedValues)
    } else if (allCalculatedValues.isNotEmpty()) {
      calculatedValuesEngine.evaluate(allCalculatedValues, dataContext)
    }
    return dataContext
  }
}
