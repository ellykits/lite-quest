package io.litequest.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.litequest.ui.QuestionnaireMode
import io.litequest.ui.screen.QuestionnaireScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Single Page", "Multi-Page")

  val vitalsViewModel: VitalsViewModel = viewModel { VitalsViewModel() }
  val paginatedViewModel: PaginatedViewModel = viewModel { PaginatedViewModel() }

  val vitalsState by vitalsViewModel.state.collectAsState()
  val paginatedState by paginatedViewModel.state.collectAsState()

  LaunchedEffect(Unit) {
    vitalsViewModel.prepopulateForSummary()
    paginatedViewModel.prepopulateForSummary()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Summary Examples",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        },
        actions = {
          IconButton(
            onClick = onDismiss,
            colors =
              androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
              ),
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
            )
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
          ),
      )
    },
    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    modifier = modifier,
  ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding),
    ) {
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.primary,
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = {
              Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
              )
            },
          )
        }
      }

      when (selectedTab) {
        0 -> {
          var singleMode by remember { mutableStateOf(QuestionnaireMode.Summary) }
          val vitalsSubmittedJson by vitalsViewModel.submittedJson.collectAsState()

          QuestionnaireScreen(
            type = vitalsViewModel.type,
            state = vitalsState,
            mode = singleMode,
            onAnswerChange = { linkId, value -> vitalsViewModel.updateAnswer(linkId, value) },
            onSubmit = { vitalsViewModel.submit() },
            onModeChange = { newMode -> singleMode = newMode },
            showCloseButton = false,
            modifier = Modifier.fillMaxSize(),
          )

          SubmissionHandler(
            submittedJson = vitalsSubmittedJson,
            onDismiss = { vitalsViewModel.resetForm() },
          )
        }
        1 -> {
          var paginatedMode by remember { mutableStateOf(QuestionnaireMode.Summary) }
          val paginatedSubmittedJson by paginatedViewModel.submittedJson.collectAsState()

          QuestionnaireScreen(
            type = paginatedViewModel.type,
            state = paginatedState,
            mode = paginatedMode,
            onAnswerChange = { linkId, value -> paginatedViewModel.updateAnswer(linkId, value) },
            onSubmit = { paginatedViewModel.submit() },
            onModeChange = { newMode -> paginatedMode = newMode },
            showCloseButton = false,
            modifier = Modifier.fillMaxSize(),
          )

          SubmissionHandler(
            submittedJson = paginatedSubmittedJson,
            onDismiss = { paginatedViewModel.resetForm() },
          )
        }
      }
    }
  }
}
