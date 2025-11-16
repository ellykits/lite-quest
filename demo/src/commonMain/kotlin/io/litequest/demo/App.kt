package io.litequest.demo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.litequest.demo.navigation.Route
import io.litequest.demo.theme.DarkColorScheme
import io.litequest.demo.theme.LightColorScheme
import io.litequest.ui.QuestionnaireMode
import io.litequest.ui.screen.QuestionnaireScreen
import kotlinx.coroutines.launch

@Composable
fun App() {
  MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme) {
    val navController = rememberNavController()
    NavHost(
      navController = navController,
      startDestination = Route.ModeSelection,
    ) {
      composable<Route.ModeSelection> {
        ModeSelectionScreen(
          onModeSelected = { mode ->
            when (mode) {
              "single" -> navController.navigate(Route.SingleFormMode)
              "pagination" -> navController.navigate(Route.PaginationMode)
              "summary" -> navController.navigate(Route.Summary)
            }
          },
        )
      }

      composable<Route.SingleFormMode> {
        val viewModel: VitalsViewModel = viewModel { VitalsViewModel() }
        val state by viewModel.state.collectAsState()
        val submittedJson by viewModel.submittedJson.collectAsState()
        var mode by remember { mutableStateOf(QuestionnaireMode.Edit) }

        QuestionnaireScreen(
          type = viewModel.type,
          state = state,
          mode = mode,
          onAnswerChange = { linkId, value -> viewModel.updateAnswer(linkId, value) },
          onSubmit = { viewModel.submit() },
          onModeChange = { newMode -> mode = newMode },
          onDismiss = { navController.popBackStack() },
        )
        SubmissionHandler(
          submittedJson = submittedJson,
          onDismiss = { viewModel.resetForm() },
        )
      }

      composable<Route.PaginationMode> {
        val viewModel: PaginatedViewModel = viewModel { PaginatedViewModel() }
        val state by viewModel.state.collectAsState()
        val submittedJson by viewModel.submittedJson.collectAsState()

        QuestionnaireScreen(
          type = viewModel.type,
          state = state,
          onAnswerChange = { linkId, value -> viewModel.updateAnswer(linkId, value) },
          onSubmit = { viewModel.submit() },
          onDismiss = { navController.popBackStack() },
        )
        SubmissionHandler(
          submittedJson = submittedJson,
          onDismiss = { viewModel.resetForm() },
        )
      }

      composable<Route.Summary> {
        SummaryScreen(
          onDismiss = { navController.popBackStack() },
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionScreen(onModeSelected: (String) -> Unit) {
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()
  val modeOptions =
    listOf(
      ModeOption(
        id = "single",
        title = "Single Page Form",
        description = "Fill out form with Edit/Review modes",
        icon = Icons.Default.Edit,
        isPrimary = true,
      ),
      ModeOption(
        id = "pagination",
        title = "Multi-Page Form",
        description = "Paginated form with progress tracking",
        icon = Icons.AutoMirrored.Filled.List,
      ),
      ModeOption(
        id = "summary",
        title = "View Summary Examples",
        description = "Compare single and multi-page summaries",
        icon = Icons.Default.CheckCircle,
      ),
      ModeOption(
        id = "components",
        title = "View Components",
        description = "Explore individual form components",
        icon = Icons.Default.Settings,
      ),
      ModeOption(
        id = "showcase",
        title = "Showcase Form Behaviours",
        description = "Demonstrate advanced form features",
        icon = Icons.Default.PlayArrow,
      ),
    )

  Scaffold(
    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
    ) {
      Text(
        text = "Select Form Mode",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = androidx.compose.ui.text.style.TextAlign.Start,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
      )

      Text(
        text = "Choose how you'd like to interact with the questionnaire form",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = androidx.compose.ui.text.style.TextAlign.Start,
        modifier = Modifier.fillMaxWidth(),
      )

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        items(modeOptions) { option ->
          ModeCard(
            title = option.title,
            description = option.description,
            icon = option.icon,
            isPrimary = option.isPrimary,
            onClick = {
              when (option.id) {
                "components",
                "showcase", -> {
                  coroutineScope.launch { snackbarHostState.showSnackbar("Coming Soon! 🚀") }
                }
                else -> onModeSelected(option.id)
              }
            },
          )
        }
      }
    }
  }
}

@Composable
fun ModeCard(
  title: String,
  description: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isPrimary: Boolean = false,
  onClick: () -> Unit,
) {
  Card(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth().height(80.dp),
    colors =
      CardDefaults.cardColors(
        containerColor =
          if (isPrimary) {
            MaterialTheme.colorScheme.primaryContainer
          } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
          },
      ),
    elevation =
      CardDefaults.cardElevation(
        defaultElevation = 2.dp,
        pressedElevation = 6.dp,
      ),
    shape = MaterialTheme.shapes.large,
  ) {
    Row(
      modifier = Modifier.fillMaxSize().padding(20.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(28.dp),
        tint =
          if (isPrimary) {
            MaterialTheme.colorScheme.onPrimaryContainer
          } else {
            MaterialTheme.colorScheme.primary
          },
      )

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color =
            if (isPrimary) {
              MaterialTheme.colorScheme.onPrimaryContainer
            } else {
              MaterialTheme.colorScheme.onSurface
            },
        )

        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color =
            if (isPrimary) {
              MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
          maxLines = 1,
          overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
fun SubmissionHandler(
  submittedJson: String?,
  onDismiss: () -> Unit,
) {
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
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Surface(
          modifier =
            Modifier.fillMaxSize().clickable(
              interactionSource =
                remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
              indication = null,
            ) { /* Prevent dismiss when clicking on dialog */},
          shape = RectangleShape,
          color = MaterialTheme.colorScheme.surface,
          shadowElevation = 0.dp,
          tonalElevation = 0.dp,
        ) {
          Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
          ) {
            // Header with title and close button
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

            // Content area with scroll
            Column(
              modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
              HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
              )

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
                  fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                  modifier = Modifier.padding(12.dp),
                  lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                )
              }
            }
          }
        }
      }
    }
  }
}
