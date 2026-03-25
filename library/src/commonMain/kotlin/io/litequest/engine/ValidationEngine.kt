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
import io.litequest.model.ItemType
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
      if (item.isLayoutContainer()) {
        if (item.visibleIf == null || visibilityEngine.isVisible(item, dataContext)) {
          errors.addAll(
            validateResponseMap(
              items = item.items,
              responseItems = responseItems,
              dataContext = dataContext,
              path = path,
            )
          )
        }
        return@forEach
      }

      if (item.visibleIf != null && !visibilityEngine.isVisible(item, dataContext)) {
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
          )
        )
      }

      if (item.validations.isNotEmpty()) {
        item.validations.forEach { rule ->
          if (!TruthinessChecker.isTruthy(evaluator.evaluate(rule.expression, dataContext))) {
            errors.add(
              ValidationError(
                linkId = item.linkId,
                path = currentPath,
                message = rule.message,
                itemText = item.text,
              )
            )
          }
        }
      }

      if (item.items.isNotEmpty()) {
        if (item.repeats) {
          responseItem?.answers?.forEachIndexed { index, answer ->
            val nestedResponseMap = answer.items.associateBy { it.linkId }
            val rawRowData =
              (dataContext[item.linkId] as? List<*>)?.getOrNull(index) as? Map<String, Any?>
            val rowContext =
              if (rawRowData != null) {
                dataContext + rawRowData + mapOf(item.linkId to rawRowData)
              } else {
                dataContext
              }
            errors.addAll(
              validateResponseMap(
                items = item.items,
                responseItems = nestedResponseMap,
                dataContext = rowContext,
                path = currentPath + index.toString(),
              )
            )
          }
        } else {
          val nestedResponseMap = responseItem?.items?.associateBy { it.linkId } ?: emptyMap()
          errors.addAll(
            validateResponseMap(
              items = item.items,
              responseItems = nestedResponseMap,
              dataContext = dataContext,
              path = currentPath,
            )
          )
        }
      }
    }

    return errors
  }

  private fun Item.isLayoutContainer(): Boolean {
    return type == ItemType.LAYOUT_ROW ||
      type == ItemType.LAYOUT_COLUMN ||
      type == ItemType.LAYOUT_BOX
  }
}
