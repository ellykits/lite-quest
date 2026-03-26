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
package io.litequest.ui.widget.datetime

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

class DatePickerWidget(override val item: Item) : ItemWidget {
  @OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalTime::class,
    FormatStringsInDatetimeFormats::class,
  )
  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement, String?) -> Unit,
    errorMessage: String?,
  ) {
    var showDialog by remember { mutableStateOf(false) }
    val dateString = value?.jsonPrimitive?.content ?: ""

    val displayPattern = item.extension["displayFormat"]?.jsonPrimitive?.content ?: "yyyy-MM-dd"
    val outputPattern = item.extension["outputFormat"]?.jsonPrimitive?.content ?: "yyyy-MM-dd"
    val minDate = item.extension["minDate"]?.jsonPrimitive?.content?.let { LocalDate.parse(it) }
    val maxDate = item.extension["maxDate"]?.jsonPrimitive?.content?.let { LocalDate.parse(it) }

    val displayFormat = LocalDate.Format { byUnicodePattern(displayPattern) }
    val outputFormat = LocalDate.Format { byUnicodePattern(outputPattern) }

    val currentDate =
      dateString.parseLocalDate()
        ?: kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())

    val datePickerState =
      rememberDatePickerState(
        initialSelectedDateMillis = currentDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
      )

    val displayValue =
      dateString
        .takeIf { it.isNotEmpty() }
        ?.let { dateString.parseLocalDate()?.let { displayFormat.format(it) } } ?: ""

    DateTimeTrigger(
      label = item.text,
      value = displayValue,
      onClick = { showDialog = true },
      onClear =
        if (!item.readOnly) {
          { onValueChange(JsonNull, item.text) }
        } else {
          null
        },
      errorMessage = errorMessage,
    )

    if (showDialog) {
      DatePickerDialog(
        onDismissRequest = { showDialog = false },
        confirmButton = {
          TextButton(
            onClick = {
              datePickerState.selectedDateMillis?.let { millis ->
                val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                val formattedDate = outputFormat.format(date)
                onValueChange(JsonPrimitive(formattedDate), item.text)
              }
              showDialog = false
            }
          ) {
            Text("OK")
          }
        },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
      ) {
        DatePicker(state = datePickerState)
      }
    }
  }
}
