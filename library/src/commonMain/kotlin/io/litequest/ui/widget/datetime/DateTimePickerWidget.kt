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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

class DateTimePickerWidget(override val item: Item) : ItemWidget {
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
    val dateTimeString = value?.jsonPrimitive?.content ?: ""
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val displayPattern =
      item.extension["displayFormat"]?.jsonPrimitive?.content ?: "yyyy-MM-dd HH:mm"
    val outputPattern =
      item.extension["outputFormat"]?.jsonPrimitive?.content ?: "yyyy-MM-dd'T'HH:mm:ss"

    val displayFormat = LocalDateTime.Format { byUnicodePattern(displayPattern) }
    val outputFormat = LocalDateTime.Format { byUnicodePattern(outputPattern) }

    val currentDateTime =
      dateTimeString.parseLocalDateTime()
        ?: kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    val datePickerState =
      rememberDatePickerState(
        initialSelectedDateMillis =
          currentDateTime.date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
      )
    val timePickerState =
      rememberTimePickerState(
        initialHour = currentDateTime.hour,
        initialMinute = currentDateTime.minute,
      )

    val displayValue =
      dateTimeString
        .takeIf { it.isNotEmpty() }
        ?.let { dateTimeString.parseLocalDateTime()?.let { displayFormat.format(it) } } ?: ""

    DateTimeTrigger(
      label = item.text,
      value = displayValue,
      onClick = { showDatePicker = true },
      errorMessage = errorMessage,
    )

    if (showDatePicker) {
      DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = {
          TextButton(
            onClick = {
              showDatePicker = false
              showTimePicker = true
            }
          ) {
            Text("Next")
          }
        },
        dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
      ) {
        DatePicker(state = datePickerState)
      }
    }

    if (showTimePicker) {
      androidx.compose.material3.AlertDialog(
        onDismissRequest = { showTimePicker = false },
        confirmButton = {
          TextButton(
            onClick = {
              showTimePicker = false
              val dateMillis = datePickerState.selectedDateMillis ?: return@TextButton
              val instant = Instant.fromEpochMilliseconds(dateMillis)
              val date = instant.toLocalDateTime(TimeZone.UTC).date

              val localDateTime =
                LocalDateTime(
                  date.year,
                  date.month,
                  date.day,
                  timePickerState.hour,
                  timePickerState.minute,
                )
              val formattedDateTime = outputFormat.format(localDateTime)
              onValueChange(JsonPrimitive(formattedDateTime), item.text)
            }
          ) {
            Text("OK")
          }
        },
        dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
        text = { TimePicker(state = timePickerState) },
      )
    }
  }
}
