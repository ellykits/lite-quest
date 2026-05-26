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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ClipboardCheck
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import io.litequest.demo.components.GridCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
  val snackbarHostState = remember { SnackbarHostState() }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) { padding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier = Modifier.widthIn(max = 800.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = "LiteQuest Demo",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
          )
          Text(
            text = "Explore questionnaire forms and summaries",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
          Text(
            text = "Single Page Forms",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            GridCard(
              title = "Single form",
              icon = Lucide.Pencil,
              onClick = { onNavigate("single-form") },
              modifier = Modifier.weight(1f),
            )
            GridCard(
              title = "Single form summary",
              icon = Lucide.ClipboardCheck,
              onClick = { onNavigate("single-summary") },
              modifier = Modifier.weight(1f),
            )
            GridCard(
              title = "Read only form",
              icon = Lucide.Eye,
              onClick = { onNavigate("single-readonly") },
              modifier = Modifier.weight(1f),
            )
          }

          Text(
            text = "Paginated Forms",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            GridCard(
              title = "Fill paginated form",
              icon = Lucide.Pencil,
              onClick = { onNavigate("paginated-form") },
              modifier = Modifier.weight(1f),
            )
            GridCard(
              title = "View paginated summary",
              icon = Lucide.ClipboardCheck,
              onClick = { onNavigate("paginated-summary") },
              modifier = Modifier.weight(1f),
            )
            GridCard(
              title = "Read only paginated form",
              icon = Lucide.Eye,
              onClick = { onNavigate("paginated-readonly") },
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }
  }
}
