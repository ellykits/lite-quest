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
package io.litequest.demo.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.litequest.engine.LiteQuestEvaluator
import io.litequest.model.Item
import io.litequest.model.ItemType
import io.litequest.model.Questionnaire
import io.litequest.model.QuestionnaireResponse
import io.litequest.state.QuestionnaireManager
import io.litequest.ui.screen.EmbeddedQuestionnaire
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmbeddedFormScreen(onBack: () -> Unit) {
  val questionnaire = remember { encounterQuestionnaire() }
  val manager = remember { QuestionnaireManager(questionnaire, LiteQuestEvaluator(questionnaire)) }

  var clinicianName by remember { mutableStateOf("") }
  var facility by remember { mutableStateOf("") }
  var encounterResult by remember { mutableStateOf<EncounterResult?>(null) }

  encounterResult?.let { result ->
    EncounterResultDialog(result = result, onDismiss = { encounterResult = null })
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Embedded Form",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "EmbeddedQuestionnaire inside an existing Scaffold",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        actions = {
          IconButton(onClick = onBack) { Icon(Icons.Default.Close, contentDescription = "Close") }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
          ),
      )
    },
  ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
    ) {
      EmbeddedQuestionnaire(manager = manager, modifier = Modifier.fillMaxWidth())

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

      Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text(
            text = "Encounter Details",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            OutlinedTextField(
              value = clinicianName,
              onValueChange = { clinicianName = it },
              label = { Text("Attending Clinician") },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
              value = facility,
              onValueChange = { facility = it },
              label = { Text("Facility / Location") },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
          }

          Button(
            onClick = {
              val response = manager.state.value.response
              encounterResult =
                EncounterResult(
                  clinicianName = clinicianName,
                  facility = facility,
                  json = buildCombinedJson(response, clinicianName, facility),
                )
            },
            modifier = Modifier.align(Alignment.End),
            shape = MaterialTheme.shapes.large,
          ) {
            Text("Record Encounter", style = MaterialTheme.typography.labelLarge)
          }
        }
      }
    }
  }
}

private data class EncounterResult(
  val clinicianName: String,
  val facility: String,
  val json: String,
)

@Composable
private fun EncounterResultDialog(result: EncounterResult, onDismiss: () -> Unit) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier = Modifier.fillMaxWidth(0.92f),
      shape = MaterialTheme.shapes.extraLarge,
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      tonalElevation = 6.dp,
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Encounter Recorded", style = MaterialTheme.typography.headlineSmall)
          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          LabeledField("Attending Clinician", result.clinicianName.ifBlank { "—" })
          LabeledField("Facility", result.facility.ifBlank { "—" })
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Text(
          text = "Full combined response",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Surface(
          modifier =
            Modifier.fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .verticalScroll(rememberScrollState()),
          color = MaterialTheme.colorScheme.surfaceContainerHighest,
          shape = MaterialTheme.shapes.medium,
        ) {
          Text(
            text = result.json,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(12.dp),
          )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = onDismiss) { Text("Close") }
        }
      }
    }
  }
}

@Composable
private fun LabeledField(label: String, value: String) {
  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(
      text = "$label:",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

private fun buildCombinedJson(
  response: QuestionnaireResponse,
  clinicianName: String,
  facility: String,
): String {
  val prettyJson = Json { prettyPrint = true }
  val responseElement: JsonElement =
    prettyJson.encodeToJsonElement(QuestionnaireResponse.serializer(), response)
  val combined: JsonObject = buildJsonObject {
    put("attendingClinician", clinicianName)
    put("facility", facility)
    put("encounterNote", responseElement)
  }
  return prettyJson.encodeToString(JsonElement.serializer(), combined)
}

private fun encounterQuestionnaire() =
  Questionnaire(
    id = "encounter-note",
    title = "Encounter Note",
    items =
      listOf(
        Item(
          linkId = "patientName",
          type = ItemType.STRING,
          text = "Patient name",
          required = true,
        ),
        Item(linkId = "age", type = ItemType.INTEGER, text = "Age (years)"),
        Item(
          linkId = "symptoms",
          type = ItemType.TEXT,
          text = "Presenting symptoms",
          required = true,
        ),
      ),
  )
