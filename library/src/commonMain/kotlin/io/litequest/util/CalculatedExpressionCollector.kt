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
package io.litequest.util

import io.litequest.model.CalculatedValue
import io.litequest.model.Item

object CalculatedExpressionCollector {
  fun collect(items: List<Item>): List<CalculatedValue> {
    val calculatedValues = mutableListOf<CalculatedValue>()
    collectRecursive(items, calculatedValues)
    return calculatedValues
  }

  private fun collectRecursive(items: List<Item>, result: MutableList<CalculatedValue>) {
    items.forEach { item ->
      if (item.calculatedExpression != null) {
        result.add(CalculatedValue(name = item.linkId, expression = item.calculatedExpression))
      }

      if (item.items.isNotEmpty()) {
        collectRecursive(item.items, result)
      }
    }
  }
}
