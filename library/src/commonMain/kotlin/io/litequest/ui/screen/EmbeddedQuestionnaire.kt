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
package io.litequest.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.litequest.state.QuestionnaireManager
import io.litequest.ui.layout.EmbeddedVerticalLayoutStrategy
import io.litequest.ui.renderer.FormRenderer

/**
 * Renders questionnaire fields inline within an existing UI — no Scaffold, no TopAppBar, no summary
 * or dismiss handling. Sizes to fit its items so the host screen controls scroll and layout.
 * Validation errors appear as the user interacts with each field.
 *
 * Read the live response via [QuestionnaireManager.state] to collect answers at any time.
 */
@Composable
fun EmbeddedQuestionnaire(manager: QuestionnaireManager, modifier: Modifier = Modifier) {
  val state by manager.state.collectAsState()
  var touchedFieldIds by remember { mutableStateOf(emptySet<String>()) }
  var touchedFieldPaths by remember { mutableStateOf(emptySet<String>()) }

  FormRenderer(
    items = state.visibleItems,
    state = state,
    onAnswerChange = { linkId, value, text ->
      touchedFieldIds = touchedFieldIds + linkId
      manager.updateAnswer(linkId, value, text)
    },
    touchedFieldIds = touchedFieldIds,
    touchedFieldPaths = touchedFieldPaths,
    showAllValidationErrors = false,
    submitAttemptedFieldIds = emptySet(),
    submitAttemptedFieldPaths = emptySet(),
    onRepetitionAdd = { linkId -> manager.addRepetition(linkId) },
    onRepetitionRemove = { linkId, index ->
      touchedFieldPaths = reindexRepetitionPathsAfterRemoval(touchedFieldPaths, linkId, index)
      manager.removeRepetition(linkId, index)
    },
    onRepetitionFieldChange = { linkId, index, fieldLinkId, value, text ->
      touchedFieldIds = touchedFieldIds + fieldLinkId
      touchedFieldPaths = touchedFieldPaths + "$linkId.$index.$fieldLinkId"
      manager.updateInRepetition(linkId, index, fieldLinkId, value, text)
    },
    widgetFactory = manager.widgetFactory,
    layoutStrategy = EmbeddedVerticalLayoutStrategy,
    modifier = modifier,
  )
}
