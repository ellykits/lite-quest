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
package io.litequest.demo.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun SubmissionHandler(submittedJson: String?, onDismiss: () -> Unit) {
  SubmissionResultDialog(
    showDialog = submittedJson != null,
    jsonResponse = submittedJson ?: "",
    onDismissRequest = onDismiss,
  )
}

@Composable
fun SubmissionResultDialog(
  showDialog: Boolean,
  jsonResponse: String,
  onDismissRequest: () -> Unit,
) {
  if (showDialog) {
    Dialog(
      onDismissRequest = onDismissRequest,
      properties =
        DialogProperties(
          dismissOnBackPress = true,
          dismissOnClickOutside = false,
          usePlatformDefaultWidth = false,
        ),
    ) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
          modifier =
            Modifier.fillMaxSize().clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
            ) {},
          shape = RectangleShape,
          color = MaterialTheme.colorScheme.surface,
          shadowElevation = 0.dp,
          tonalElevation = 0.dp,
        ) {
          Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(32.dp),
                )
                Column {
                  Text(
                    text = "Form Submitted",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = "Your response has been recorded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp).clickable { onDismissRequest() },
              )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
              modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

              Text(
                text = "Response Data",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 8.dp),
              )

              Surface(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = MaterialTheme.shapes.medium,
              ) {
                Text(
                  text = jsonResponse,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.padding(12.dp),
                  lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
              Button(onClick = onDismissRequest) { Text(text = "Close") }
            }
          }
        }
      }
    }
  }
}
