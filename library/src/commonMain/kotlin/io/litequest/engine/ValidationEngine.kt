package io.litequest.engine

import io.litequest.model.Item
import io.litequest.model.ResponseItem
import io.litequest.model.ValidationError
import io.litequest.util.TruthinessChecker

class ValidationEngine(private val evaluator: JsonLogicEvaluator) {
  private val visibilityEngine = VisibilityEngine(evaluator)

  fun validateResponse(
    items: List<Item>,
    responseItems: List<ResponseItem>,
    dataContext: Map<String, Any?>,
    path: List<String> = emptyList(),
  ): List<ValidationError> {
    val responseMap = responseItems.associateBy { it.linkId }
    return validateResponseMap(items, responseMap, dataContext, path)
  }

  private fun validateResponseMap(
    items: List<Item>,
    responseItems: Map<String, ResponseItem>,
    dataContext: Map<String, Any?>,
    path: List<String> = emptyList(),
  ): List<ValidationError> {
    val errors = mutableListOf<ValidationError>()

    items.forEach { item ->
      if (!visibilityEngine.isVisible(item, dataContext)) {
        return@forEach
      }

      val responseItem = responseItems[item.linkId]
      val currentPath = path + item.linkId

      if (item.required && (responseItem == null || responseItem.answers.isEmpty())) {
        errors.add(
          ValidationError(
            linkId = item.linkId,
            path = currentPath,
            message = "${item.text}.required",
            itemText = item.text,
          ),
        )
      }

      item.validations.forEach { rule ->
        val result = evaluator.evaluate(rule.expression, dataContext)
        if (!TruthinessChecker.isTruthy(result)) {
          errors.add(
            ValidationError(
              linkId = item.linkId,
              path = currentPath,
              message = rule.message,
              itemText = item.text,
            ),
          )
        }
      }

      if (item.items.isNotEmpty()) {
        val nestedResponseMap = responseItem?.items?.associateBy { it.linkId } ?: emptyMap()
        errors.addAll(
          validateResponseMap(item.items, nestedResponseMap, dataContext, currentPath),
        )
      }
    }

    return errors
  }
}
