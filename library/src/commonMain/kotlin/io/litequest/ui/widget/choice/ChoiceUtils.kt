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
package io.litequest.ui.widget.choice

import io.litequest.model.AnswerOption
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

internal fun handleExclusiveToggle(
  code: String,
  selectedCodes: Set<String>,
  answerOptions: List<AnswerOption>,
): Set<String> {
  val clickedOption = answerOptions.find { it.code == code }
  val isExclusive = clickedOption?.exclusive == true

  return if (selectedCodes.contains(code)) {
    selectedCodes - code
  } else {
    if (isExclusive) {
      setOf(code)
    } else {
      val exclusiveOptions = answerOptions.filter { it.exclusive }.map { it.code }
      (selectedCodes - exclusiveOptions.toSet()) + code
    }
  }
}

internal fun getDisplayText(selectedCodes: Set<String>, answerOptions: List<AnswerOption>): String {
  val selectedOptions = answerOptions.filter { selectedCodes.contains(it.code) }
  return selectedOptions.joinToString(", ") { it.display }
}

internal fun toggleSingleChoice(currentCode: String?, clickedCode: String): String? {
  return if (currentCode == clickedCode) null else clickedCode
}

internal fun multiSelectionToJson(selectedCodes: Set<String>): JsonElement {
  return if (selectedCodes.isEmpty()) {
    JsonNull
  } else {
    JsonArray(selectedCodes.map { JsonPrimitive(it) })
  }
}
