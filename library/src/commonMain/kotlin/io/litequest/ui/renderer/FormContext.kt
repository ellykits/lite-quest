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
package io.litequest.ui.renderer

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import io.litequest.ui.widget.WidgetFactory
import kotlinx.serialization.json.JsonElement

@Stable
data class FormContext(
  val values: Map<String, JsonElement?>,
  val onValueChange: (String, JsonElement, String?) -> Unit,
  val errorMessages: Map<String, String>,
  val pathErrorMessages: Map<String, String> = emptyMap(),
  val widgetFactory: WidgetFactory,
  val repetitions: Map<String, List<Map<String, JsonElement?>>> = emptyMap(),
  val onRepetitionAdd: ((String) -> Unit)? = null,
  val onRepetitionRemove: ((String, Int) -> Unit)? = null,
  val onRepetitionFieldChange: ((String, Int, String, JsonElement, String?) -> Unit)? = null,
)

val LocalFormContext =
  compositionLocalOf<FormContext> {
    error("No FormContext provided. Make sure to use FormRenderer.")
  }
