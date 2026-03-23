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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

@Composable
internal fun DateTimeTrigger(
  label: String,
  value: String,
  onClick: () -> Unit,
  icon: ImageVector = Icons.Default.DateRange,
  errorMessage: String? = null,
) {
  Box(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = value,
      onValueChange = {},
      readOnly = true,
      label = { Text(label) },
      isError = errorMessage != null,
      supportingText = errorMessage?.let { { Text(it) } },
      trailingIcon = {
        Icon(
          imageVector = icon,
          contentDescription = "Select",
          modifier = Modifier.clickable { onClick() },
        )
      },
      modifier = Modifier.fillMaxWidth().clickable { onClick() },
    )
  }
}

internal fun String.parseLocalDate(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

internal fun String.parseLocalTime(): LocalTime? = runCatching { LocalTime.parse(this) }.getOrNull()

internal fun String.parseLocalDateTime(): LocalDateTime? =
  runCatching {
      val cleaned = this.replace("Z", "").replace("+00:00", "")
      LocalDateTime.parse(cleaned)
    }
    .getOrNull()
