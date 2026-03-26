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

class VisibilityEngine(private val evaluator: JsonLogicEvaluator) {
  fun isVisible(
    item: Item,
    dataContext: Map<String, Any?>,
    instanceContext: Map<String, Any?>? = null,
    groupLinkId: String? = null,
  ): Boolean {
    item.visibleIf ?: return true

    // Scoped Context: Merge instance data and handle prefix stripping for the group
    val scopedContext =
      if (instanceContext != null) {
        val merged = dataContext.toMutableMap()
        merged.putAll(instanceContext)
        if (groupLinkId != null) {
          merged[groupLinkId] = instanceContext
        }
        merged
      } else {
        dataContext
      }

    return evaluator.isTruthy(evaluator.evaluate(item.visibleIf, scopedContext))
  }

  fun getVisibleItems(items: List<Item>, dataContext: Map<String, Any?>): List<Item> {
    return items.mapNotNull { item ->
      if (item.visibleIf == null || isVisible(item, dataContext)) {
        if (item.items.isEmpty()) {
          item
        } else {
          item.copy(items = getVisibleItems(item.items, dataContext))
        }
      } else {
        null
      }
    }
  }

  /** Returns a set of unique paths (e.g. "group.0.field") for all visible items in the response. */
  fun getVisiblePaths(
    items: List<Item>,
    responseItems: List<ResponseItem>,
    dataContext: Map<String, Any?>,
    pathPrefix: String = "",
  ): Set<String> {
    val visiblePaths = mutableSetOf<String>()
    val responseMap = responseItems.associateBy { it.linkId }

    items.forEach { item ->
      if (item.isLayoutContainer()) {
        if (isVisible(item, dataContext)) {
          visiblePaths.addAll(getVisiblePaths(item.items, responseItems, dataContext, pathPrefix))
        }
        return@forEach
      }

      val currentPath = if (pathPrefix.isEmpty()) item.linkId else "$pathPrefix.${item.linkId}"

      if (item.repeats) {
        val responseItem = responseMap[item.linkId]
        responseItem?.answers?.forEachIndexed { index, answer ->
          val rowData =
            (dataContext[item.linkId] as? List<*>)?.getOrNull(index) as? Map<String, Any?>
          val indexedPath = "$currentPath.$index"

          if (isVisible(item, dataContext, rowData, item.linkId)) {
            visiblePaths.add(indexedPath)
            if (item.items.isNotEmpty()) {
              visiblePaths.addAll(
                getVisiblePaths(
                  items = item.items,
                  responseItems = answer.items,
                  dataContext =
                    if (rowData != null) {
                      dataContext + rowData + mapOf(item.linkId to rowData)
                    } else {
                      dataContext
                    },
                  pathPrefix = indexedPath,
                )
              )
            }
          }
        }
      } else {
        if (isVisible(item, dataContext)) {
          visiblePaths.add(currentPath)
          if (item.items.isNotEmpty()) {
            val nestedResponse = responseMap[item.linkId]?.items ?: emptyList()
            visiblePaths.addAll(
              getVisiblePaths(item.items, nestedResponse, dataContext, currentPath)
            )
          }
        }
      }
    }
    return visiblePaths
  }

  private fun Item.isLayoutContainer(): Boolean {
    return type == ItemType.LAYOUT_ROW ||
      type == ItemType.LAYOUT_COLUMN ||
      type == ItemType.LAYOUT_BOX
  }
}
