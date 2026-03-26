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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

@Composable
internal fun DateTimeTrigger(
  label: String,
  value: String,
  onClick: () -> Unit,
  onClear: (() -> Unit)? = null,
  icon: ImageVector = Icons.Default.DateRange,
  errorMessage: String? = null,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
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
      modifier = Modifier.weight(1f).clickable { onClick() },
    )

    if (onClear != null && value.isNotEmpty()) {
      Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
      ) {
        IconButton(onClick = onClear, modifier = Modifier.size(40.dp)) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Clear value",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
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
