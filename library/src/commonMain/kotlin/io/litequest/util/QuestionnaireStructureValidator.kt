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

import io.litequest.model.Item

object QuestionnaireStructureValidator {
  fun requireUniqueLinkIds(items: List<Item>) {
    val seenByLinkId = mutableMapOf<String, String>()
    val duplicates = mutableListOf<String>()

    fun walk(nodeItems: List<Item>, pathPrefix: String) {
      nodeItems.forEachIndexed { index, item ->
        val currentPath =
          if (pathPrefix.isEmpty()) {
            "${item.linkId}[#$index]"
          } else {
            "$pathPrefix > ${item.linkId}[#$index]"
          }

        val previousPath = seenByLinkId[item.linkId]
        if (previousPath != null) {
          duplicates.add("'${item.linkId}' at $previousPath and $currentPath")
        } else {
          seenByLinkId[item.linkId] = currentPath
        }

        if (item.items.isNotEmpty()) {
          walk(item.items, currentPath)
        }
      }
    }

    walk(items, pathPrefix = "")

    require(duplicates.isEmpty()) {
      "Duplicate linkId values are not allowed. Found: ${duplicates.joinToString("; ")}"
    }
  }
}
