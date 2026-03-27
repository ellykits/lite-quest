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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.litequest.model.Questionnaire
import io.litequest.model.ValidationError
import io.litequest.state.QuestionnaireManager
import io.litequest.state.QuestionnaireState
import io.litequest.ui.QuestionnaireMode
import io.litequest.ui.QuestionnaireType
import io.litequest.ui.pagination.PageNavigator
import io.litequest.ui.pagination.PaginatedQuestionnaire
import io.litequest.ui.renderer.FormRenderer
import io.litequest.ui.summary.SummaryPage
import io.litequest.ui.validation.ValidationPresentation
import io.litequest.ui.widget.WidgetFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
  type: QuestionnaireType,
  manager: QuestionnaireManager,
  onSubmit: () -> Unit,
  modifier: Modifier = Modifier,
  mode: QuestionnaireMode = QuestionnaireMode.Edit,
  onModeChange: ((QuestionnaireMode) -> Unit)?,
  onDismiss: (() -> Unit)? = null,
  showCloseButton: Boolean = false,
  showDismissDialogOnClose: Boolean = true,
  showValidationDialogOnSubmit: Boolean = true,
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
        showValidationDialogOnSubmit = showValidationDialogOnSubmit,
        showReview = showReview,
        customActions = customActions,
        modifier = modifier,
        widgetFactory = manager.widgetFactory,
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
        showValidationDialogOnSubmit = showValidationDialogOnSubmit,
        showReview = showReview,
        customActions = customActions,
        modifier = modifier,
        mode = mode,
        onModeChange = onModeChange,
        widgetFactory = manager.widgetFactory,
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
  manager: QuestionnaireManager,
  onSubmit: () -> Unit,
  onModeChange: ((QuestionnaireMode) -> Unit)?,
  onDismiss: (() -> Unit)?,
  showCloseButton: Boolean,
  showDismissDialogOnClose: Boolean,
  showValidationDialogOnSubmit: Boolean,
  showReview: Boolean,
  customActions: (@Composable () -> Unit)?,
  modifier: Modifier,
  widgetFactory: WidgetFactory,
) {
  var showDismissDialog by remember { mutableStateOf(false) }
  var showValidationDialog by remember { mutableStateOf(false) }
  var showAllValidationErrors by remember { mutableStateOf(false) }
  var submitAttemptedFieldIds by remember { mutableStateOf(emptySet<String>()) }
  var submitAttemptedFieldPaths by remember { mutableStateOf(emptySet<String>()) }
  var touchedFieldIds by remember { mutableStateOf(emptySet<String>()) }
  var touchedFieldPaths by remember { mutableStateOf(emptySet<String>()) }

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
  if (showValidationDialog) {
    ValidationErrorsDialog(
      errors = state.validationErrors,
      onGoBack = { showValidationDialog = false },
      onSubmitAnyway = {
        showValidationDialog = false
        onSubmit()
      },
    )
  }

  val submitAction = {
    submitAttemptedFieldIds = state.validationErrors.map { it.linkId }.toSet()
    submitAttemptedFieldPaths = state.validationErrors.map { it.path.joinToString(".") }.toSet()
    if (
      ValidationPresentation.shouldShowSubmitValidationDialog(
        showValidationDialogOnSubmit = showValidationDialogOnSubmit,
        mode = mode,
        errors = state.validationErrors,
      )
    ) {
      showAllValidationErrors = true
      showValidationDialog = true
    } else {
      if (mode != QuestionnaireMode.ReadOnly) {
        showAllValidationErrors = true
      }
      onSubmit()
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text =
              when (mode) {
                QuestionnaireMode.Edit -> questionnaire.title
                QuestionnaireMode.Summary -> questionnaire.title
                QuestionnaireMode.ReadOnly -> questionnaire.title
              },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        actions = {
          Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (onModeChange != null) {
              ReviewEditButton(mode = mode, onModeChange = onModeChange, showReview = showReview)
            }
            if (showCloseButton && onDismiss != null && mode != QuestionnaireMode.Summary) {
              IconButton(
                onClick = {
                  if (showDismissDialogOnClose && mode == QuestionnaireMode.Edit) {
                    showDismissDialog = true
                  } else {
                    onDismiss()
                  }
                }
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
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
          DefaultFormActions(onSubmit = submitAction, mode = mode)
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
            onAnswerChange = { linkId, value, text ->
              touchedFieldIds = touchedFieldIds + linkId
              manager.updateAnswer(linkId, value, text)
            },
            touchedFieldIds = touchedFieldIds,
            touchedFieldPaths = touchedFieldPaths,
            showAllValidationErrors = showAllValidationErrors,
            submitAttemptedFieldIds = submitAttemptedFieldIds,
            submitAttemptedFieldPaths = submitAttemptedFieldPaths,
            onRepetitionAdd = { linkId -> manager.addRepetition(linkId) },
            onRepetitionRemove = { linkId, index ->
              touchedFieldPaths =
                reindexRepetitionPathsAfterRemoval(touchedFieldPaths, linkId, index)
              submitAttemptedFieldPaths =
                reindexRepetitionPathsAfterRemoval(submitAttemptedFieldPaths, linkId, index)
              manager.removeRepetition(linkId, index)
            },
            onRepetitionFieldChange = { linkId, index, fieldLinkId, value, text ->
              touchedFieldIds = touchedFieldIds + fieldLinkId
              touchedFieldPaths = touchedFieldPaths + "$linkId.$index.$fieldLinkId"
              manager.updateInRepetition(linkId, index, fieldLinkId, value, text)
            },
            widgetFactory = widgetFactory,
          )
        }
      }
      QuestionnaireMode.ReadOnly,
      QuestionnaireMode.Summary -> {
        SummaryPage(
          state = state,
          paginatedQuestionnaire = null,
          onSubmit = submitAction,
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
  manager: QuestionnaireManager,
  onSubmit: () -> Unit,
  onDismiss: (() -> Unit)?,
  showCloseButton: Boolean,
  showDismissDialogOnClose: Boolean,
  showValidationDialogOnSubmit: Boolean,
  showReview: Boolean,
  customActions: (@Composable () -> Unit)?,
  modifier: Modifier,
  mode: QuestionnaireMode,
  onModeChange: ((QuestionnaireMode) -> Unit)?,
  widgetFactory: WidgetFactory,
) {
  val pageNavigator =
    remember(paginatedQuestionnaire) { PageNavigator(paginatedQuestionnaire.pages) }
  val totalPages = paginatedQuestionnaire.pages.size
  val pageIndex by pageNavigator.currentPageIndex.collectAsState()
  var showDismissDialog by remember { mutableStateOf(false) }
  var showValidationDialog by remember { mutableStateOf(false) }
  var showAllValidationErrors by remember { mutableStateOf(false) }
  var submitAttemptedFieldIds by remember { mutableStateOf(emptySet<String>()) }
  var submitAttemptedFieldPaths by remember { mutableStateOf(emptySet<String>()) }
  var touchedFieldIds by remember { mutableStateOf(emptySet<String>()) }
  var touchedFieldPaths by remember { mutableStateOf(emptySet<String>()) }

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
  if (showValidationDialog) {
    ValidationErrorsDialog(
      errors = state.validationErrors,
      onGoBack = { showValidationDialog = false },
      onSubmitAnyway = {
        showValidationDialog = false
        onSubmit()
      },
    )
  }

  val submitAction = {
    submitAttemptedFieldIds = state.validationErrors.map { it.linkId }.toSet()
    submitAttemptedFieldPaths = state.validationErrors.map { it.path.joinToString(".") }.toSet()
    if (
      ValidationPresentation.shouldShowSubmitValidationDialog(
        showValidationDialogOnSubmit = showValidationDialogOnSubmit,
        mode = mode,
        errors = state.validationErrors,
      )
    ) {
      showAllValidationErrors = true
      showValidationDialog = true
    } else {
      if (mode != QuestionnaireMode.ReadOnly) {
        showAllValidationErrors = true
      }
      onSubmit()
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = paginatedQuestionnaire.title,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            if (mode == QuestionnaireMode.Edit) {
              Text(
                text = "Page ${pageIndex + 1} of $totalPages",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        },
        actions = {
          Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (onModeChange != null) {
              ReviewEditButton(mode = mode, onModeChange = onModeChange, showReview = showReview)
            }
            if (showCloseButton && onDismiss != null && mode != QuestionnaireMode.Summary) {
              IconButton(
                onClick = {
                  if (showDismissDialogOnClose && mode == QuestionnaireMode.Edit) {
                    showDismissDialog = true
                  } else {
                    onDismiss()
                  }
                }
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
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
            onSubmit = submitAction,
            mode = mode,
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
            modifier = Modifier.padding(vertical = 16.dp),
          )

          PagerView(
            pageNavigator = pageNavigator,
            currentPage = pageIndex,
            state = state,
            manager = manager,
            touchedFieldIds = touchedFieldIds,
            touchedFieldPaths = touchedFieldPaths,
            onFieldTouched = { linkId -> touchedFieldIds = touchedFieldIds + linkId },
            onRepetitionFieldTouched = { path -> touchedFieldPaths = touchedFieldPaths + path },
            onRepetitionRemoved = { linkId, index ->
              touchedFieldPaths =
                reindexRepetitionPathsAfterRemoval(touchedFieldPaths, linkId, index)
              submitAttemptedFieldPaths =
                reindexRepetitionPathsAfterRemoval(submitAttemptedFieldPaths, linkId, index)
            },
            showAllValidationErrors = showAllValidationErrors,
            submitAttemptedFieldIds = submitAttemptedFieldIds,
            submitAttemptedFieldPaths = submitAttemptedFieldPaths,
            modifier = Modifier.fillMaxSize(),
            widgetFactory = widgetFactory,
          )
        }
      }
      QuestionnaireMode.ReadOnly,
      QuestionnaireMode.Summary -> {
        SummaryPage(
          state = state,
          paginatedQuestionnaire = paginatedQuestionnaire,
          onSubmit = submitAction,
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
  manager: QuestionnaireManager,
  touchedFieldIds: Set<String>,
  touchedFieldPaths: Set<String>,
  onFieldTouched: (String) -> Unit,
  onRepetitionFieldTouched: (String) -> Unit,
  onRepetitionRemoved: (String, Int) -> Unit,
  showAllValidationErrors: Boolean,
  submitAttemptedFieldIds: Set<String>,
  submitAttemptedFieldPaths: Set<String>,
  modifier: Modifier = Modifier,
  widgetFactory: WidgetFactory,
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
      onAnswerChange = { linkId, value, text ->
        onFieldTouched(linkId)
        manager.updateAnswer(linkId, value, text)
      },
      touchedFieldIds = touchedFieldIds,
      touchedFieldPaths = touchedFieldPaths,
      showAllValidationErrors = showAllValidationErrors,
      submitAttemptedFieldIds = submitAttemptedFieldIds,
      submitAttemptedFieldPaths = submitAttemptedFieldPaths,
      onRepetitionAdd = { linkId -> manager.addRepetition(linkId) },
      onRepetitionRemove = { linkId, index ->
        onRepetitionRemoved(linkId, index)
        manager.removeRepetition(linkId, index)
      },
      onRepetitionFieldChange = { linkId, index, fieldLinkId, value, text ->
        onFieldTouched(fieldLinkId)
        onRepetitionFieldTouched("$linkId.$index.$fieldLinkId")
        manager.updateInRepetition(linkId, index, fieldLinkId, value, text)
      },
      widgetFactory = widgetFactory,
    )
  }
}

@Composable
private fun DefaultFormActions(
  onSubmit: () -> Unit,
  mode: QuestionnaireMode,
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
      if (mode == QuestionnaireMode.Edit && pageNavigator != null && !isFirstPage) {
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
private fun ReviewEditButton(
  mode: QuestionnaireMode,
  onModeChange: (QuestionnaireMode) -> Unit,
  showReview: Boolean,
) {
  if (showReview && mode != QuestionnaireMode.ReadOnly) {
    TextButton(
      onClick = {
        when (mode) {
          QuestionnaireMode.Edit -> onModeChange(QuestionnaireMode.Summary)
          QuestionnaireMode.Summary -> onModeChange(QuestionnaireMode.Edit)
          QuestionnaireMode.ReadOnly -> {}
        }
      },
      shape = MaterialTheme.shapes.small,
    ) {
      Text(
        text = if (mode == QuestionnaireMode.Summary) "Edit" else "Review",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
      )
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

@Composable
private fun ValidationErrorsDialog(
  errors: List<ValidationError>,
  onGoBack: () -> Unit,
  onSubmitAnyway: () -> Unit,
) {
  val scrollState = rememberScrollState()

  Dialog(
    onDismissRequest = onGoBack,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier = Modifier.fillMaxWidth(0.92f).widthIn(max = 760.dp),
      shape = MaterialTheme.shapes.extraLarge,
      tonalElevation = 6.dp,
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text("Validation issues", style = MaterialTheme.typography.headlineSmall)
        Text(
          "Please review the following fields before submitting:",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(
          modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(scrollState),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          errors.forEachIndexed { index, error ->
            val questionText = error.itemText?.takeIf { it.isNotBlank() } ?: error.linkId
            val reason = ValidationPresentation.formatValidationReason(error)
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = MaterialTheme.shapes.medium,
              color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
              ) {
                Text("${index + 1}. $questionText", style = MaterialTheme.typography.titleSmall)
                Text(
                  reason,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.error,
                )
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
          TextButton(onClick = onGoBack) { Text("Go back") }
          Button(onClick = onSubmitAnyway) { Text("Submit anyway") }
        }
      }
    }
  }
}

internal fun reindexRepetitionPathsAfterRemoval(
  paths: Set<String>,
  groupLinkId: String,
  removedIndex: Int,
): Set<String> {
  val prefix = "$groupLinkId."
  return paths.mapNotNullTo(linkedSetOf()) { path ->
    if (!path.startsWith(prefix)) {
      return@mapNotNullTo path
    }

    val remainder = path.removePrefix(prefix)
    val rowIndexText = remainder.substringBefore('.', missingDelimiterValue = "")
    val rowIndex = rowIndexText.toIntOrNull() ?: return@mapNotNullTo path

    when {
      rowIndex == removedIndex -> null
      rowIndex < removedIndex -> path
      else -> "$groupLinkId.${rowIndex - 1}.${remainder.substringAfter('.')}"
    }
  }
}
