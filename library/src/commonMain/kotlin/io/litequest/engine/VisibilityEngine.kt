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
import io.litequest.util.TruthinessChecker

class VisibilityEngine(private val evaluator: JsonLogicEvaluator) {
  fun isVisible(item: Item, dataContext: Map<String, Any?>): Boolean {
    val visibleIf = item.visibleIf ?: return true

    val result = evaluator.evaluate(visibleIf, dataContext)
    return TruthinessChecker.isTruthy(result)
  }

  fun getVisibleItems(items: List<Item>, dataContext: Map<String, Any?>): List<Item> {
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
