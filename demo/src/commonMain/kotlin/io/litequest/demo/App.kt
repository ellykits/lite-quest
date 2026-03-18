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
package io.litequest.demo

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.litequest.demo.components.SubmissionHandler
import io.litequest.demo.navigation.Route
import io.litequest.demo.screens.HomeScreen
import io.litequest.demo.screens.PaginatedSummaryScreen
import io.litequest.demo.screens.SinglePageSummaryScreen
import io.litequest.demo.theme.DarkColorScheme
import io.litequest.demo.theme.LightColorScheme
import io.litequest.ui.QuestionnaireMode
import io.litequest.ui.QuestionnaireType
import io.litequest.ui.screen.QuestionnaireScreen

@Composable
fun App() {
  MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Route.Home) {
      composable<Route.Home> {
        HomeScreen(
          onNavigate = { route ->
            when (route) {
              "single-form" -> navController.navigate(Route.SinglePageForm)
              "single-summary" -> navController.navigate(Route.SinglePageSummary)
              "single-readonly" -> navController.navigate(Route.SinglePageReadOnly)
              "paginated-form" -> navController.navigate(Route.PaginatedForm)
              "paginated-summary" -> navController.navigate(Route.PaginatedSummary)
              "paginated-readonly" -> navController.navigate(Route.PaginatedReadOnly)
            }
          }
        )
      }

      composable<Route.SinglePageForm> {
        val viewModel: SinglePageViewModel = viewModel { SinglePageViewModel() }
        val submittedJson by viewModel.submittedJson.collectAsState()
        val manager by viewModel.manager.collectAsState()
        var mode by remember { mutableStateOf(QuestionnaireMode.Edit) }
        val questionnaire by viewModel.questionnaire.collectAsState(initial = null)

        questionnaire?.let { form ->
          manager?.let { questionnaireManager ->
            QuestionnaireScreen(
              type = QuestionnaireType.Single(form),
              manager = questionnaireManager,
              mode = mode,
              onSubmit = { viewModel.submit() },
              onModeChange = { newMode -> mode = newMode },
              onDismiss = { navController.popBackStack() },
              showCloseButton = true,
            )
          }
        }
        SubmissionHandler(submittedJson = submittedJson, onDismiss = { viewModel.resetForm() })
      }

      composable<Route.SinglePageSummary> {
        SinglePageSummaryScreen(onBack = { navController.popBackStack() })
      }

      composable<Route.SinglePageReadOnly> {
        SinglePageSummaryScreen(
          onBack = { navController.popBackStack() },
          initialMode = QuestionnaireMode.ReadOnly,
        )
      }

      composable<Route.PaginatedForm> {
        val viewModel: PaginatedViewModel = viewModel { PaginatedViewModel() }
        val submittedJson by viewModel.submittedJson.collectAsState()
        val manager by viewModel.manager.collectAsState()
        var mode by remember { mutableStateOf(QuestionnaireMode.Edit) }
        val questionnaire by viewModel.questionnaire.collectAsState(initial = null)

        questionnaire?.let { pq ->
          manager?.let { questionnaireManager ->
            QuestionnaireScreen(
              type = QuestionnaireType.Paginated(pq),
              manager = questionnaireManager,
              onSubmit = { viewModel.submit() },
              onDismiss = { navController.popBackStack() },
              showCloseButton = true,
              mode = mode,
              onModeChange = { newMode -> mode = newMode },
            )
          }
        }
        SubmissionHandler(submittedJson = submittedJson, onDismiss = { viewModel.resetForm() })
      }

      composable<Route.PaginatedSummary> {
        PaginatedSummaryScreen(onBack = { navController.popBackStack() })
      }

      composable<Route.PaginatedReadOnly> {
        PaginatedSummaryScreen(
          onBack = { navController.popBackStack() },
          initialMode = QuestionnaireMode.ReadOnly,
        )
      }
    }
  }
}
