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
package io.litequest.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.litequest.demo.components.RatingWidget
import io.litequest.engine.LiteQuestEvaluator
import io.litequest.model.ItemType
import io.litequest.model.Questionnaire
import io.litequest.model.QuestionnaireResponse
import io.litequest.state.QuestionnaireManager
import io.litequest.ui.widget.DefaultWidgetFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import lite_quest.demo.generated.resources.Res

@OptIn(ExperimentalCoroutinesApi::class)
class SinglePageViewModel : ViewModel() {
  private val _questionnaire = MutableStateFlow<Questionnaire?>(null)
  val questionnaire: StateFlow<Questionnaire?> = _questionnaire.asStateFlow()

  private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
  }

  init {
    loadQuestionnaire()
  }

  private fun loadQuestionnaire() {
    viewModelScope.launch {
      try {
        val bytes = Res.readBytes("files/single_page_questionnaire_sample.json")
        _questionnaire.value = json.decodeFromString<Questionnaire>(bytes.decodeToString())
      } catch (e: Exception) {
        println("Error loading questionnaire: ${e.message}")
      }
    }
  }

  val manager: StateFlow<QuestionnaireManager?> =
    _questionnaire
      .filterNotNull()
      .flatMapLatest { q ->
        val evaluator = LiteQuestEvaluator(q)
        val factory =
          DefaultWidgetFactory().apply { registerWidget(ItemType("RATING")) { RatingWidget(it) } }
        MutableStateFlow(QuestionnaireManager(q, evaluator, widgetFactory = factory))
      }
      .stateIn(viewModelScope, SharingStarted.Eagerly, null)

  private val _submittedJson = MutableStateFlow<String?>(null)
  val submittedJson: StateFlow<String?> = _submittedJson.asStateFlow()

  fun submit() {
    manager.value?.let { manager ->
      val response = manager.submit()
      _submittedJson.value = json.encodeToString<QuestionnaireResponse>(response)
    }
  }

  fun resetForm() {
    _submittedJson.value = null
  }

  fun populateForSummary() {
    viewModelScope.launch {
      runCatching {
        val bytes = Res.readBytes("files/single_page_response_sample.json")
        val response = json.decodeFromString<QuestionnaireResponse>(bytes.decodeToString())
        manager.value?.setResponse(response)
      }
    }
  }
}
