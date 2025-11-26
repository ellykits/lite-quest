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
package io.litequest.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.litequest.state.QuestionnaireState
import io.litequest.ui.QuestionnaireMode
import io.litequest.ui.QuestionnaireType
import io.litequest.ui.pagination.PageNavigator
import io.litequest.ui.pagination.PaginatedQuestionnaire
import io.litequest.ui.renderer.FormRenderer
import io.litequest.ui.summary.SummaryPage
import kotlinx.serialization.json.JsonElement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
  type: QuestionnaireType,
  state: QuestionnaireState,
  onAnswerChange: (String, JsonElement) -> Unit,
  onSubmit: () -> Unit,
  modifier: Modifier = Modifier,
  mode: QuestionnaireMode = QuestionnaireMode.Edit,
  onModeChange: ((QuestionnaireMode) -> Unit)? = null,
  onDismiss: (() -> Unit)? = null,
  showCloseButton: Boolean = true,
  showDismissDialogOnClose: Boolean = true,
  customActions: (@Composable () -> Unit)? = null,
) {
  when (type) {
    is QuestionnaireType.Single -> {
      SingleQuestionnaireScreen(
        questionnaire = type.questionnaire,
        state = state,
        mode = mode,
        onAnswerChange = onAnswerChange,
        onSubmit = onSubmit,
        onModeChange = onModeChange,
        onDismiss = onDismiss,
        showCloseButton = showCloseButton,
        showDismissDialogOnClose = showDismissDialogOnClose,
        customActions = customActions,
        modifier = modifier,
      )
    }
    is QuestionnaireType.Paginated -> {
      PaginatedQuestionnaireScreen(
        paginatedQuestionnaire = type.paginatedQuestionnaire,
        state = state,
        onAnswerChange = onAnswerChange,
        onSubmit = onSubmit,
        onDismiss = onDismiss,
        showCloseButton = showCloseButton,
        showDismissDialogOnClose = showDismissDialogOnClose,
        customActions = customActions,
        modifier = modifier,
        mode = mode,
        onModeChange = onModeChange,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleQuestionnaireScreen(
  questionnaire: io.litequest.model.Questionnaire,
  state: QuestionnaireState,
  mode: QuestionnaireMode,
  onAnswerChange: (String, JsonElement) -> Unit,
  onSubmit: () -> Unit,
  onModeChange: ((QuestionnaireMode) -> Unit)?,
  onDismiss: (() -> Unit)?,
  showCloseButton: Boolean,
  showDismissDialogOnClose: Boolean,
  customActions: (@Composable () -> Unit)?,
  modifier: Modifier,
) {
  var showDismissDialog by remember { mutableStateOf(false) }

  if (showDismissDialog && onDismiss != null) {
    DismissDialog(
      showDialog = true,
      onDismissRequest = { showDismissDialog = false },
      onConfirm = {
        showDismissDialog = false
        onDismiss()
      },
      onCancel = { showDismissDialog = false },
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text =
                when (mode) {
                  QuestionnaireMode.Edit -> questionnaire.title
                  QuestionnaireMode.Summary -> "Summary"
                },
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            if (mode == QuestionnaireMode.Summary) {
              Text(
                text = questionnaire.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            } else {
              questionnaire.version
                ?.takeIf { it.isNotEmpty() }
                ?.let { version ->
                  Text(
                    text = "Version $version",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
            }
          }
        },
        actions = {
          if (mode == QuestionnaireMode.Summary && onModeChange != null) {
            OutlinedButton(
              onClick = { onModeChange(QuestionnaireMode.Edit) },
              modifier = Modifier.height(36.dp),
              shape = MaterialTheme.shapes.medium,
              border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                modifier = Modifier.size(18.dp),
              )
              Spacer(Modifier.width(6.dp))
              Text("Edit", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.width(8.dp))
          }
          if (showCloseButton && onDismiss != null) {
            IconButton(
              onClick = {
                if (showDismissDialogOnClose) {
                  showDismissDialog = true
                } else {
                  onDismiss()
                }
              },
              colors =
                IconButtonDefaults.iconButtonColors(
                  contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
            ) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
          ),
      )
    },
    bottomBar = {
      if (mode == QuestionnaireMode.Edit) {
        if (customActions != null) {
          Surface(
            shadowElevation = 16.dp,
            tonalElevation = 6.dp,
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape =
              androidx.compose.foundation.shape.RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
          ) {
            customActions()
          }
        } else {
          DefaultSingleFormActions(mode = mode, onModeChange = onModeChange, onSubmit = onSubmit)
        }
      }
    },
    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    modifier = modifier,
  ) { padding ->
    when (mode) {
      QuestionnaireMode.Edit -> {
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 4.dp)) {
          FormRenderer(items = state.visibleItems, state = state, onAnswerChange = onAnswerChange)
        }
      }
      QuestionnaireMode.Summary -> {
        SummaryPage(
          state = state,
          paginatedQuestionnaire = null,
          modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 4.dp),
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaginatedQuestionnaireScreen(
  paginatedQuestionnaire: PaginatedQuestionnaire,
  state: QuestionnaireState,
  onAnswerChange: (String, JsonElement) -> Unit,
  onSubmit: () -> Unit,
  onDismiss: (() -> Unit)?,
  showCloseButton: Boolean,
  showDismissDialogOnClose: Boolean,
  customActions: (@Composable () -> Unit)?,
  modifier: Modifier,
  mode: QuestionnaireMode = QuestionnaireMode.Edit,
  onModeChange: ((QuestionnaireMode) -> Unit)? = null,
) {
  val pageNavigator =
    remember(paginatedQuestionnaire) { PageNavigator(paginatedQuestionnaire.pages) }
  val totalPages = paginatedQuestionnaire.pages.size
  val pageIndex by pageNavigator.currentPageIndex.collectAsState()
  var showDismissDialog by remember { mutableStateOf(false) }

  if (showDismissDialog && onDismiss != null) {
    DismissDialog(
      showDialog = true,
      onDismissRequest = { showDismissDialog = false },
      onConfirm = {
        showDismissDialog = false
        onDismiss()
      },
      onCancel = { showDismissDialog = false },
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text =
                when (mode) {
                  QuestionnaireMode.Edit -> paginatedQuestionnaire.title
                  QuestionnaireMode.Summary -> "Summary"
                },
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text =
                when (mode) {
                  QuestionnaireMode.Edit -> "Page ${pageIndex + 1} of $totalPages"
                  QuestionnaireMode.Summary -> paginatedQuestionnaire.title
                },
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        actions = {
          if (mode == QuestionnaireMode.Summary && onModeChange != null) {
            OutlinedButton(
              onClick = { onModeChange(QuestionnaireMode.Edit) },
              modifier = Modifier.height(36.dp),
              shape = MaterialTheme.shapes.medium,
              border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                modifier = Modifier.size(18.dp),
              )
              Spacer(Modifier.width(6.dp))
              Text("Edit", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.width(8.dp))
          }
          if (showCloseButton && onDismiss != null) {
            IconButton(
              onClick = {
                if (showDismissDialogOnClose) {
                  showDismissDialog = true
                } else {
                  onDismiss()
                }
              },
              colors =
                IconButtonDefaults.iconButtonColors(
                  contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
            ) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
          ),
      )
    },
    bottomBar = {
      if (mode == QuestionnaireMode.Edit) {
        if (customActions != null) {
          Surface(
            shadowElevation = 16.dp,
            tonalElevation = 6.dp,
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape =
              androidx.compose.foundation.shape.RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
          ) {
            customActions()
          }
        } else {
          DefaultPaginatedFormActions(
            pageNavigator = pageNavigator,
            totalPages = totalPages,
            onSubmit = onSubmit,
          )
        }
      }
    },
    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    modifier = modifier,
  ) { padding ->
    when (mode) {
      QuestionnaireMode.Edit -> {
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
          LinearProgressIndicator(
            progress = { (pageIndex + 1) / totalPages.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
          )

          PageIndicators(
            currentPage = pageIndex,
            totalPages = totalPages,
            modifier = Modifier.padding(vertical = 20.dp),
          )

          SwipeablePager(
            pageNavigator = pageNavigator,
            currentPage = pageIndex,
            totalPages = totalPages,
            state = state,
            onAnswerChange = onAnswerChange,
            modifier = Modifier.fillMaxSize(),
          )
        }
      }
      QuestionnaireMode.Summary -> {
        SummaryPage(
          state = state,
          paginatedQuestionnaire = paginatedQuestionnaire,
          modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 4.dp),
        )
      }
    }
  }
}

@Composable
private fun DefaultSingleFormActions(
  mode: QuestionnaireMode,
  onModeChange: ((QuestionnaireMode) -> Unit)?,
  onSubmit: () -> Unit,
) {
  Surface(
    shadowElevation = 16.dp,
    tonalElevation = 6.dp,
    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
    color = MaterialTheme.colorScheme.surfaceContainer,
    shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
    ) {
      when (mode) {
        QuestionnaireMode.Edit -> {
          if (onModeChange != null) {
            OutlinedButton(
              onClick = { onModeChange(QuestionnaireMode.Summary) },
              modifier = Modifier.height(40.dp),
              shape = MaterialTheme.shapes.large,
              border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            ) {
              Text(
                "Review",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 8.dp),
              )
            }
          }
        }
        QuestionnaireMode.Summary -> {
          if (onModeChange != null) {
            OutlinedButton(
              onClick = { onModeChange(QuestionnaireMode.Edit) },
              modifier = Modifier.height(40.dp),
              shape = MaterialTheme.shapes.large,
              border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            ) {
              Text(
                "Edit",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 8.dp),
              )
            }
          }
        }
      }

      Button(
        onClick = onSubmit,
        modifier = Modifier.height(40.dp),
        shape = MaterialTheme.shapes.large,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
      ) {
        Text(
          "Submit",
          style = MaterialTheme.typography.labelLarge,
          modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(Modifier.width(8.dp))
        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
      }
    }
  }
}

@Composable
private fun DefaultPaginatedFormActions(
  pageNavigator: PageNavigator,
  totalPages: Int,
  onSubmit: () -> Unit,
) {
  val currentPage by pageNavigator.currentPageIndex.collectAsState()
  val isFirstPage = currentPage == 0
  val isLastPage = currentPage == totalPages - 1

  Surface(
    shadowElevation = 16.dp,
    tonalElevation = 6.dp,
    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
    color = MaterialTheme.colorScheme.surfaceContainer,
    shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (!isFirstPage) {
        Button(
          onClick = { pageNavigator.goPrevious() },
          modifier = Modifier.height(40.dp),
          shape = MaterialTheme.shapes.large,
          elevation =
            ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
        ) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
          Spacer(Modifier.width(8.dp))
          Text(
            "Previous",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 4.dp),
          )
        }
      }

      if (isLastPage) {
        Button(
          onClick = onSubmit,
          modifier = Modifier.height(40.dp),
          shape = MaterialTheme.shapes.large,
          elevation =
            ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
        ) {
          Text(
            "Submit",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 4.dp),
          )
          Spacer(Modifier.width(8.dp))
          Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
        }
      } else {
        Button(
          onClick = { pageNavigator.goNext() },
          modifier = Modifier.height(40.dp),
          shape = MaterialTheme.shapes.large,
          elevation =
            ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
        ) {
          Text(
            "Next",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 4.dp),
          )
          Spacer(Modifier.width(8.dp))
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
      }
    }
  }
}

@Composable
private fun PageIndicators(currentPage: Int, totalPages: Int, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    repeat(totalPages) { index ->
      Box(
        modifier =
          Modifier.size(if (index == currentPage) 12.dp else 8.dp)
            .clip(CircleShape)
            .background(
              if (index == currentPage) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.outlineVariant
              }
            )
      )
      if (index < totalPages - 1) {
        Spacer(Modifier.width(12.dp))
      }
    }
  }
}

@Composable
private fun SwipeablePager(
  pageNavigator: PageNavigator,
  currentPage: Int,
  totalPages: Int,
  state: QuestionnaireState,
  onAnswerChange: (String, JsonElement) -> Unit,
  modifier: Modifier = Modifier,
) {
  var offsetX by remember { mutableFloatStateOf(0f) }

  Box(
    modifier =
      modifier.pointerInput(currentPage) {
        detectDragGestures(
          onDragEnd = {
            val threshold = size.width * 0.3f
            when {
              offsetX > threshold && currentPage > 0 -> {
                pageNavigator.goPrevious()
              }
              offsetX < -threshold && currentPage < totalPages - 1 -> {
                pageNavigator.goNext()
              }
            }
            offsetX = 0f
          }
        ) { _, dragAmount ->
          val newOffset = offsetX + dragAmount.x
          offsetX =
            when {
              currentPage == 0 && newOffset > 0 -> newOffset * 0.3f
              currentPage == totalPages - 1 && newOffset < 0 -> newOffset * 0.3f
              else -> newOffset
            }
        }
      }
  ) {
    val page = pageNavigator.pages[currentPage]
    FormRenderer(items = page.items, state = state, onAnswerChange = onAnswerChange)
  }
}

@Composable
private fun DismissDialog(
  showDialog: Boolean,
  onDismissRequest: () -> Unit,
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
) {
  if (showDialog) {
    AlertDialog(
      onDismissRequest = onDismissRequest,
      title = { Text("Dismiss Form?", style = MaterialTheme.typography.headlineSmall) },
      text = {
        Text(
          "Are you sure you want to dismiss this form? Any unsaved changes may be lost.",
          style = MaterialTheme.typography.bodyLarge,
        )
      },
      confirmButton = { Button(onClick = onConfirm) { Text("Yes, Dismiss") } },
      dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } },
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      shape = MaterialTheme.shapes.extraLarge,
    )
  }
}
