package io.litequest.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.litequest.engine.LiteQuestEvaluator
import io.litequest.model.Item
import io.litequest.model.ItemType
import io.litequest.model.Questionnaire
import io.litequest.model.QuestionnaireResponse
import io.litequest.state.QuestionnaireManager
import io.litequest.state.QuestionnaireState
import io.litequest.ui.QuestionnaireType
import io.litequest.ui.pagination.PaginatedQuestionnaire
import io.litequest.ui.pagination.QuestionnairePage
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

class PaginatedViewModel : ViewModel() {
  private val paginatedQuestionnaire = createPaginatedQuestionnaire()
  private val questionnaire = convertToQuestionnaire(paginatedQuestionnaire)
  private val evaluator = LiteQuestEvaluator(questionnaire)
  private val manager = QuestionnaireManager(questionnaire, evaluator)

  val type = QuestionnaireType.Paginated(paginatedQuestionnaire)
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
    }
  }

  fun resetForm() {
    _submittedJson.value = null
  }

  fun prepopulateForSummary() {
    viewModelScope.launch {
      manager.updateAnswer("full-name", JsonPrimitive("Jane Smith"))
      manager.updateAnswer("age", JsonPrimitive(28))
      manager.updateAnswer("weight", JsonPrimitive(62.5))
      manager.updateAnswer("height", JsonPrimitive(1.65))

      val medicationsArray = buildJsonArray {
        add(
          buildJsonObject {
            put("medication-name", JsonPrimitive("Metformin"))
            put("medication-dosage", JsonPrimitive("500mg"))
          },
        )
      }
      manager.updateAnswer("medications", medicationsArray)
      manager.updateAnswer("has-diabetes", JsonPrimitive(true))
    }
  }

  private fun convertToQuestionnaire(paginated: PaginatedQuestionnaire): Questionnaire {
    return Questionnaire(
      id = paginated.id,
      title = paginated.title,
      version = paginated.version,
      items = paginated.pages.flatMap { it.items },
    )
  }

  private fun createPaginatedQuestionnaire(): PaginatedQuestionnaire {
    return PaginatedQuestionnaire(
      id = "patient-vitals-paginated",
      title = "Patient Vitals Form",
      version = "1.0",
      pages =
        listOf(
          QuestionnairePage(
            id = "page-1",
            title = "Patient Information",
            description = "Basic patient details",
            order = 0,
            items =
              listOf(
                Item(
                  linkId = "patient-info",
                  type = ItemType.DISPLAY,
                  text = "Patient Information",
                  required = false,
                  repeats = false,
                  items = emptyList(),
                ),
                Item(
                  linkId = "full-name",
                  type = ItemType.TEXT,
                  text = "Full Name",
                  required = true,
                  repeats = false,
                  items = emptyList(),
                ),
                Item(
                  linkId = "age",
                  type = ItemType.INTEGER,
                  text = "Age (years)",
                  required = true,
                  repeats = false,
                  items = emptyList(),
                ),
              ),
          ),
          QuestionnairePage(
            id = "page-2",
            title = "Vital Signs",
            description = "Patient vital measurements",
            order = 1,
            items =
              listOf(
                Item(
                  linkId = "vitals-section",
                  type = ItemType.DISPLAY,
                  text = "Vital Signs",
                  required = false,
                  repeats = false,
                  items = emptyList(),
                ),
                Item(
                  linkId = "weight",
                  type = ItemType.DECIMAL,
                  text = "Weight (kg)",
                  required = true,
                  repeats = false,
                  items = emptyList(),
                ),
                Item(
                  linkId = "height",
                  type = ItemType.DECIMAL,
                  text = "Height (m)",
                  required = true,
                  repeats = false,
                  items = emptyList(),
                ),
              ),
          ),
          QuestionnairePage(
            id = "page-3",
            title = "Medical History",
            description = "Medications and allergies",
            order = 2,
            items =
              listOf(
                Item(
                  linkId = "medications",
                  type = ItemType.GROUP,
                  text = "Current Medications",
                  required = false,
                  repeats = true,
                  items =
                    listOf(
                      Item(
                        linkId = "medication-name",
                        type = ItemType.TEXT,
                        text = "Medication Name",
                        required = true,
                        repeats = false,
                        items = emptyList(),
                      ),
                      Item(
                        linkId = "medication-dosage",
                        type = ItemType.TEXT,
                        text = "Dosage",
                        required = true,
                        repeats = false,
                        items = emptyList(),
                      ),
                    ),
                ),
                Item(
                  linkId = "has-diabetes",
                  type = ItemType.BOOLEAN,
                  text = "Has Diabetes",
                  required = false,
                  repeats = false,
                  items = emptyList(),
                ),
              ),
          ),
        ),
    )
  }
}
