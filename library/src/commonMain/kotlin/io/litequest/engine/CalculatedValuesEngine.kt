package io.litequest.engine

import io.litequest.model.CalculatedValue

class CalculatedValuesEngine(private val evaluator: JsonLogicEvaluator) {
  fun evaluate(
    calculatedValues: List<CalculatedValue>,
    dataContext: MutableMap<String, Any?>,
  ): Map<String, Any?> {
    val results = mutableMapOf<String, Any?>()
    calculatedValues.forEach { calcValue ->
      val result = evaluator.evaluate(calcValue.expression, dataContext)
      results[calcValue.name] = result
      dataContext[calcValue.name] = result
    }
    return results
  }
}
