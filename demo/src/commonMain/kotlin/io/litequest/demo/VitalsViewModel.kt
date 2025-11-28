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
import io.litequest.engine.LiteQuestEvaluator
import io.litequest.model.Questionnaire
import io.litequest.model.QuestionnaireResponse
import io.litequest.state.QuestionnaireManager
import io.litequest.state.QuestionnaireState
import io.litequest.ui.QuestionnaireType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class VitalsViewModel : ViewModel() {
  private val questionnaire = loadQuestionnaireFromJson()
  private val evaluator = LiteQuestEvaluator(questionnaire)
  private val manager = QuestionnaireManager(questionnaire, evaluator)

  val type = QuestionnaireType.Single(questionnaire)
  val state: StateFlow<QuestionnaireState> = manager.state

  private val _submittedJson = MutableStateFlow<String?>(null)
  val submittedJson: StateFlow<String?> = _submittedJson.asStateFlow()

  private val json = Json { prettyPrint = true }

  fun updateAnswer(linkId: String, value: JsonElement) {
    viewModelScope.launch { manager.updateAnswer(linkId, value) }
  }

  fun submit() {
    viewModelScope.launch {
      val response = manager.getResponse()
      val jsonString = json.encodeToString<QuestionnaireResponse>(response)
      _submittedJson.value = jsonString
      println("✓ Form submitted: ${response.id}")
      println("✓ Valid: ${manager.isValid()}")
      val extracted = manager.extractData()
      println("✓ Extracted data: $extracted")
    }
  }

  fun resetForm() {
    _submittedJson.value = null
  }

  fun prepopulateForSummary() {
    populateWithSampleData()
  }

  private fun populateWithSampleData() {
    viewModelScope.launch {
      manager.updateAnswer("full-name", JsonPrimitive("John Doe"))
      manager.updateAnswer("age", JsonPrimitive(35))
      manager.updateAnswer("weight", JsonPrimitive(75.5))
      manager.updateAnswer("height", JsonPrimitive(1.75))

      // Create medications group with proper structure
      val medicationsArray = buildJsonArray {
        add(
          buildJsonObject {
            put("medication-name", JsonPrimitive("Aspirin"))
            put("medication-dosage", JsonPrimitive("100mg"))
            put("medication-frequency", JsonPrimitive("Daily"))
          }
        )
      }
      manager.updateAnswer("medications", medicationsArray)

      // Create allergies group with proper structure
      val allergiesArray = buildJsonArray {
        add(
          buildJsonObject {
            put("allergen", JsonPrimitive("Peanuts"))
            put("reaction", JsonPrimitive("Rash"))
          }
        )
      }
      manager.updateAnswer("allergies", allergiesArray)
      manager.updateAnswer("has-diabetes", JsonPrimitive(false))
    }
  }

  private fun loadQuestionnaireFromJson(): Questionnaire {
    val jsonString =
      """
      {
        "id": "patient-vitals",
        "title": "Patient Vitals Form",
        "description": "Collect patient vital signs and medical history",
        "items": [
          {
            "linkId": "patient-info",
            "type": "DISPLAY",
            "text": "Patient Information",
            "required": false,
            "repeats": false,
            "items": []
          },
          {
            "linkId": "full-name",
            "type": "TEXT",
            "text": "Full Name",
            "required": true,
            "repeats": false,
            "items": []
          },
          {
            "linkId": "age",
            "type": "INTEGER",
            "text": "Age (years)",
            "required": true,
            "repeats": false,
            "items": []
          },
          {
            "linkId": "vitals-section",
            "type": "DISPLAY",
            "text": "Vital Signs",
            "required": false,
            "repeats": false,
            "items": []
          },
          {
            "linkId": "weight",
            "type": "DECIMAL",
            "text": "Weight (kg)",
            "required": true,
            "repeats": false,
            "items": []
          },
          {
            "linkId": "height",
            "type": "DECIMAL",
            "text": "Height (m)",
            "required": true,
            "repeats": false,
            "items": []
          },
          {
            "linkId": "medications",
            "type": "GROUP",
            "text": "Current Medications",
            "required": false,
            "repeats": true,
            "items": [
              {
                "linkId": "medication-name",
                "type": "TEXT",
                "text": "Medication Name",
                "required": true,
                "repeats": false,
                "items": []
              },
              {
                "linkId": "medication-dosage",
                "type": "TEXT",
                "text": "Dosage",
                "required": true,
                "repeats": false,
                "items": []
              },
              {
                "linkId": "medication-frequency",
                "type": "TEXT",
                "text": "Frequency",
                "required": false,
                "repeats": false,
                "items": []
              }
            ]
          },
          {
            "linkId": "allergies",
            "type": "GROUP",
            "text": "Allergies",
            "required": false,
            "repeats": true,
            "items": [
              {
                "linkId": "allergen",
                "type": "TEXT",
                "text": "Allergen",
                "required": true,
                "repeats": false,
                "items": []
              },
              {
                "linkId": "reaction",
                "type": "TEXT",
                "text": "Reaction",
                "required": false,
                "repeats": false,
                "items": []
              }
            ]
          },
          {
            "linkId": "has-diabetes",
            "type": "BOOLEAN",
            "text": "Has Diabetes",
            "required": false,
            "repeats": false,
            "items": []
          }
        ]
      }
      """
        .trimIndent()
    return Json.decodeFromString<Questionnaire>(jsonString)
  }
}
