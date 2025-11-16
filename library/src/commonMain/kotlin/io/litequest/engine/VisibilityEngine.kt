package io.litequest.engine

import io.litequest.model.Item
import io.litequest.util.TruthinessChecker

class VisibilityEngine(private val evaluator: JsonLogicEvaluator) {
  fun isVisible(
    item: Item,
    dataContext: Map<String, Any?>,
  ): Boolean {
    val visibleIf = item.visibleIf ?: return true

    val result = evaluator.evaluate(visibleIf, dataContext)
    return TruthinessChecker.isTruthy(result)
  }

  fun getVisibleItems(
    items: List<Item>,
    dataContext: Map<String, Any?>,
  ): List<Item> {
    return items.mapNotNull { item ->
      if (isVisible(item, dataContext)) {
        if (item.items.isNotEmpty()) {
          item.copy(items = getVisibleItems(item.items, dataContext))
        } else {
          item
        }
      } else {
        null
      }
    }
  }
}
