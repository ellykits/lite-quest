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
package io.litequest.ui.validation

import io.litequest.model.ValidationError
import io.litequest.ui.QuestionnaireMode

internal object ValidationPresentation {
  fun visibleValidationErrors(
    errors: List<ValidationError>,
    touchedFieldIds: Set<String>,
    showAllValidationErrors: Boolean,
    submitAttemptedFieldIds: Set<String> = emptySet(),
  ): List<ValidationError> {
    return if (showAllValidationErrors) {
      if (submitAttemptedFieldIds.isEmpty()) {
        errors
      } else {
        errors.filter {
          submitAttemptedFieldIds.contains(it.linkId) || touchedFieldIds.contains(it.linkId)
        }
      }
    } else {
      errors.filter { touchedFieldIds.contains(it.linkId) }
    }
  }

  fun shouldShowSubmitValidationDialog(
    showValidationDialogOnSubmit: Boolean,
    mode: QuestionnaireMode,
    errors: List<ValidationError>,
  ): Boolean {
    return showValidationDialogOnSubmit && mode != QuestionnaireMode.ReadOnly && errors.isNotEmpty()
  }

  fun formatValidationReason(error: ValidationError): String {
    val message = error.message.trim()
    return when {
      message.endsWith(".required") -> "This field is required."
      message.equals("required", ignoreCase = true) -> "This field is required."
      else -> message
    }
  }
}
