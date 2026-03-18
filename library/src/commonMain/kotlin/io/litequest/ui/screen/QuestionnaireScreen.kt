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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.litequest.model.Questionnaire
import io.litequest.state.QuestionnaireState
import io.litequest.ui.QuestionnaireMode
import io.litequest.ui.QuestionnaireType
import io.litequest.ui.pagination.PageNavigator
import io.litequest.ui.pagination.PaginatedQuestionnaire
import io.litequest.ui.renderer.FormRenderer
import io.litequest.ui.summary.SummaryPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
  type: QuestionnaireType,
  manager: io.litequest.state.QuestionnaireManager,
  onSubmit: () -> Unit,
  modifier: Modifier = Modifier,
  mode: QuestionnaireMode = QuestionnaireMode.Edit,
  onModeChange: ((QuestionnaireMode) -> Unit)?,
  onDismiss: (() -> Unit)? = null,
  showCloseButton: Boolean = false,
  showDismissDialogOnClose: Boolean = true,
  showReview: Boolean = true,
  customActions: (@Composable () -> Unit)? = null,
) {
  val state by manager.state.collectAsState()
  when (type) {
    is QuestionnaireType.Single -> {
      SingleQuestionnaireScreen(
        questionnaire = type.questionnaire,
        state = state,
        mode = mode,
        manager = manager,
        onSubmit = onSubmit,
        onModeChange = onModeChange,
        onDismiss = onDismiss,
        showCloseButton = showCloseButton,
        showDismissDialogOnClose = showDismissDialogOnClose,
        showReview = showReview,
        customActions = customActions,
        modifier = modifier,
      )
    }
    is QuestionnaireType.Paginated -> {
      PaginatedQuestionnaireScreen(
        paginatedQuestionnaire = type.paginatedQuestionnaire,
        state = state,
        manager = manager,
        onSubmit = onSubmit,
        onDismiss = onDismiss,
        showCloseButton = showCloseButton,
        showDismissDialogOnClose = showDismissDialogOnClose,
        showReview = showReview,
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
  questionnaire: Questionnaire,
  state: QuestionnaireState,
  mode: QuestionnaireMode,
  manager: io.litequest.state.QuestionnaireManager,
  onSubmit: () -> Unit,
  onModeChange: ((QuestionnaireMode) -> Unit)?,
  onDismiss: (() -> Unit)?,
  showCloseButton: Boolean,
  showDismissDialogOnClose: Boolean,
  showReview: Boolean,
  customActions: (@Composable () -> Unit)?,
  modifier: Modifier,
) {
  var showDismissDialog by remember { mutableStateOf(false) }

  if (showDismissDialog && onDismiss != null && mode == QuestionnaireMode.Edit) {
    DismissDialog(
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
          Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(vertical = 16.dp),
          ) {
            Text(
              text =
                when (mode) {
                  QuestionnaireMode.Edit -> questionnaire.title
                  QuestionnaireMode.Summary -> "Summary"
                  QuestionnaireMode.ReadOnly -> "Read Only"
                },
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            if (mode == QuestionnaireMode.Summary) {
              Text(
                text = questionnaire.title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            } else {
              questionnaire.version
                ?.takeIf { it.isNotEmpty() }
                ?.let { version ->
                  Text(
                    text = "Version $version",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
            }
          }
        },
        actions = {
          if (showCloseButton && onDismiss != null) {
            IconButton(
              onClick = {
                if (showDismissDialogOnClose && mode == QuestionnaireMode.Edit) {
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
      if (mode != QuestionnaireMode.ReadOnly) {
        if (customActions != null) {
          Surface(
            shadowElevation = 16.dp,
            tonalElevation = 6.dp,
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
          ) {
            customActions()
          }
        } else {
          DefaultFormActions(
            onSubmit = onSubmit,
            mode = mode,
            onModeChange = onModeChange,
            showReview = showReview,
          )
        }
      }
    },
    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    modifier = modifier,
  ) { padding ->
    when (mode) {
      QuestionnaireMode.Edit -> {
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 4.dp)) {
          FormRenderer(
            items = state.visibleItems,
            state = state,
            onAnswerChange = { linkId, value, text -> manager.updateAnswer(linkId, value, text) },
            onRepetitionAdd = { linkId -> manager.addRepetition(linkId) },
            onRepetitionRemove = { linkId, index -> manager.removeRepetition(linkId, index) },
            onRepetitionFieldChange = { linkId, index, fieldLinkId, value, text ->
              manager.updateInRepetition(linkId, index, fieldLinkId, value, text)
            },
          )
        }
      }
      QuestionnaireMode.ReadOnly,
      QuestionnaireMode.Summary -> {
        SummaryPage(
          state = state,
          paginatedQuestionnaire = null,
          onSubmit = onSubmit,
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
  manager: io.litequest.state.QuestionnaireManager,
  onSubmit: () -> Unit,
  onDismiss: (() -> Unit)?,
  showCloseButton: Boolean,
  showDismissDialogOnClose: Boolean,
  showReview: Boolean,
  customActions: (@Composable () -> Unit)?,
  modifier: Modifier,
  mode: QuestionnaireMode,
  onModeChange: ((QuestionnaireMode) -> Unit)?,
) {
  val pageNavigator =
    remember(paginatedQuestionnaire) { PageNavigator(paginatedQuestionnaire.pages) }
  val totalPages = paginatedQuestionnaire.pages.size
  val pageIndex by pageNavigator.currentPageIndex.collectAsState()
  var showDismissDialog by remember { mutableStateOf(false) }

  if (showDismissDialog && onDismiss != null && mode != QuestionnaireMode.Summary) {
    DismissDialog(
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
          Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(vertical = 16.dp),
          ) {
            Text(
              text =
                when (mode) {
                  QuestionnaireMode.Edit -> paginatedQuestionnaire.title
                  QuestionnaireMode.Summary -> "Summary"
                  QuestionnaireMode.ReadOnly -> "Read Only"
                },
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text =
                when (mode) {
                  QuestionnaireMode.Edit -> "Page ${pageIndex + 1} of $totalPages"
                  QuestionnaireMode.ReadOnly,
                  QuestionnaireMode.Summary -> paginatedQuestionnaire.title
                },
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        },
        actions = {
          if (showCloseButton && onDismiss != null) {
            IconButton(
              onClick = {
                if (showDismissDialogOnClose && mode == QuestionnaireMode.Edit) {
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
      if (mode != QuestionnaireMode.ReadOnly) {
        if (customActions != null) {
          Surface(
            shadowElevation = 16.dp,
            tonalElevation = 6.dp,
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
          ) {
            customActions()
          }
        } else {
          DefaultFormActions(
            onSubmit = onSubmit,
            mode = mode,
            onModeChange = onModeChange,
            showReview = showReview,
            pageNavigator = pageNavigator,
            totalPages = totalPages,
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

          PagerView(
            pageNavigator = pageNavigator,
            currentPage = pageIndex,
            state = state,
            manager = manager,
            modifier = Modifier.fillMaxSize(),
          )
        }
      }
      QuestionnaireMode.ReadOnly,
      QuestionnaireMode.Summary -> {
        SummaryPage(
          state = state,
          paginatedQuestionnaire = paginatedQuestionnaire,
          onSubmit = onSubmit,
          modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 4.dp),
        )
      }
    }
  }
}

@Composable
private fun PagerView(
  pageNavigator: PageNavigator,
  currentPage: Int,
  state: QuestionnaireState,
  manager: io.litequest.state.QuestionnaireManager,
  modifier: Modifier = Modifier,
) {
  val pagerState = rememberPagerState(initialPage = currentPage) { pageNavigator.pages.size }

  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.currentPage }.collect { page -> pageNavigator.goToPage(page) }
  }

  LaunchedEffect(currentPage) {
    if (pagerState.currentPage != currentPage) {
      pagerState.animateScrollToPage(currentPage)
    }
  }

  HorizontalPager(state = pagerState, modifier = modifier) { pageIndex ->
    val page = pageNavigator.pages[pageIndex]
    val visiblePageItems =
      state.visibleItems.filter { item ->
        page.items.any { pageItem -> pageItem.linkId == item.linkId }
      }
    FormRenderer(
      items = visiblePageItems,
      state = state,
      onAnswerChange = { linkId, value, text -> manager.updateAnswer(linkId, value, text) },
      onRepetitionAdd = { linkId -> manager.addRepetition(linkId) },
      onRepetitionRemove = { linkId, index -> manager.removeRepetition(linkId, index) },
      onRepetitionFieldChange = { linkId, index, fieldLinkId, value, text ->
        manager.updateInRepetition(linkId, index, fieldLinkId, value, text)
      },
    )
  }
}

@Composable
private fun DefaultFormActions(
  onSubmit: () -> Unit,
  mode: QuestionnaireMode,
  onModeChange: ((QuestionnaireMode) -> Unit)?,
  showReview: Boolean = true,
  pageNavigator: PageNavigator? = null,
  totalPages: Int? = null,
) {
  val currentPageIndex = pageNavigator?.currentPageIndex?.collectAsState()?.value ?: 0
  val isLastPage = pageNavigator != null && totalPages != null && currentPageIndex == totalPages - 1
  val isFirstPage = pageNavigator != null && currentPageIndex == 0

  Column(modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars)) {
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (pageNavigator != null && !isFirstPage) {
        Button(
          onClick = { pageNavigator.goPrevious() },
          modifier = Modifier.height(36.dp),
          shape = MaterialTheme.shapes.large,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(Modifier.width(6.dp))
          Text("Previous", style = MaterialTheme.typography.labelLarge)
        }
      }

      if (mode == QuestionnaireMode.Edit && showReview && onModeChange != null) {
        if (pageNavigator == null || isLastPage) {
          OutlinedButton(
            onClick = { onModeChange(QuestionnaireMode.Summary) },
            modifier = Modifier.height(36.dp),
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
          ) {
            Text("Review", style = MaterialTheme.typography.labelLarge)
          }
        }
      }

      if (mode == QuestionnaireMode.Summary && onModeChange != null) {
        OutlinedButton(
          onClick = { onModeChange(QuestionnaireMode.Edit) },
          modifier = Modifier.height(36.dp),
          shape = MaterialTheme.shapes.large,
          border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        ) {
          Text("Edit", style = MaterialTheme.typography.labelLarge)
        }
      }

      if (mode == QuestionnaireMode.Edit && pageNavigator != null && !isLastPage) {
        Button(
          onClick = { pageNavigator.goNext() },
          modifier = Modifier.height(36.dp),
          shape = MaterialTheme.shapes.large,
        ) {
          Text("Next", style = MaterialTheme.typography.labelLarge)
          Spacer(Modifier.width(6.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
        }
      }

      if (
        (mode == QuestionnaireMode.Edit && (pageNavigator == null || isLastPage)) ||
          mode == QuestionnaireMode.Summary
      ) {
        Button(
          onClick = onSubmit,
          modifier = Modifier.height(36.dp),
          shape = MaterialTheme.shapes.large,
        ) {
          Text("Submit", style = MaterialTheme.typography.labelLarge)
          Spacer(Modifier.width(6.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
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
private fun DismissDialog(
  onDismissRequest: () -> Unit,
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
) {
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
