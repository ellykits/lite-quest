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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

class TimePickerWidget(override val item: Item) : ItemWidget {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement, String?) -> Unit,
    errorMessage: String?,
  ) {
    var showDialog by remember { mutableStateOf(false) }
    val timeString = value?.jsonPrimitive?.content ?: ""

    val use24Hour = item.extension["use24Hour"]?.jsonPrimitive?.content?.toBoolean() ?: true

    val currentTime =
      timeString.parseLocalTime()
        ?: kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    val timePickerState =
      rememberTimePickerState(
        initialHour = currentTime.hour,
        initialMinute = currentTime.minute,
        is24Hour = use24Hour,
      )

    DateTimeTrigger(
      label = item.text,
      value = timeString,
      onClick = { showDialog = true },
      icon = Lucide.Clock,
      errorMessage = errorMessage,
    )

    if (showDialog) {
      AlertDialog(
        onDismissRequest = { showDialog = false },
        confirmButton = {
          TextButton(
            onClick = {
              val hour = timePickerState.hour.toString().padStart(2, '0')
              val minute = timePickerState.minute.toString().padStart(2, '0')
              val second = "00"
              val formattedTime = "$hour:$minute:$second"
              onValueChange(JsonPrimitive(formattedTime), item.text)
              showDialog = false
            }
          ) {
            Text("OK")
          }
        },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
        text = {
          Column {
            Text(
              text = "Select Time",
              style = MaterialTheme.typography.labelLarge,
              modifier = Modifier.padding(bottom = 16.dp),
            )
            TimePicker(state = timePickerState)
          }
        },
      )
    }
  }
}
