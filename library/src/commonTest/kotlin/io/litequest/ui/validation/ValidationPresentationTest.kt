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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationPresentationTest {
  private val errorA =
    ValidationError(
      linkId = "fieldA",
      path = listOf("section", "fieldA"),
      message = "Field A.required",
      itemText = "Field A",
    )
  private val errorB =
    ValidationError(
      linkId = "fieldB",
      path = listOf("section", "fieldB"),
      message = "fieldB.range",
      itemText = "Field B",
    )
  private val repeatedErrorA0 =
    ValidationError(
      linkId = "fieldA",
      path = listOf("repeatGroup", "0", "fieldA"),
      message = "Field A.required",
      itemText = "Field A",
    )
  private val repeatedErrorA1 =
    ValidationError(
      linkId = "fieldA",
      path = listOf("repeatGroup", "1", "fieldA"),
      message = "Field A.required",
      itemText = "Field A",
    )

  @Test
  fun visibleValidationErrors_returnsAll_whenShowAllIsTrue() {
    val result =
      ValidationPresentation.visibleValidationErrors(
        errors = listOf(errorA, errorB),
        touchedFieldIds = setOf("fieldA"),
        showAllValidationErrors = true,
      )

    assertEquals(2, result.size)
    assertTrue(result.contains(errorA))
    assertTrue(result.contains(errorB))
  }

  @Test
  fun visibleValidationErrors_whenSubmitAttemptSet_hidesNewUntouchedErrors() {
    val result =
      ValidationPresentation.visibleValidationErrors(
        errors = listOf(errorA, errorB),
        touchedFieldIds = emptySet(),
        showAllValidationErrors = true,
        submitAttemptedFieldIds = setOf("fieldA"),
      )

    assertEquals(1, result.size)
    assertEquals("fieldA", result.first().linkId)
  }

  @Test
  fun visibleValidationErrors_returnsTouchedOnly_whenShowAllIsFalse() {
    val result =
      ValidationPresentation.visibleValidationErrors(
        errors = listOf(errorA, errorB),
        touchedFieldIds = setOf("fieldB"),
        showAllValidationErrors = false,
      )

    assertEquals(1, result.size)
    assertEquals("fieldB", result.first().linkId)
  }

  @Test
  fun visibleValidationErrors_forRepeatedFields_usesPathScopedTouch() {
    val result =
      ValidationPresentation.visibleValidationErrors(
        errors = listOf(repeatedErrorA0, repeatedErrorA1),
        touchedFieldIds = setOf("fieldA"),
        touchedFieldPaths = setOf("repeatGroup.0.fieldA"),
        showAllValidationErrors = false,
      )

    assertEquals(1, result.size)
    assertEquals(listOf("repeatGroup", "0", "fieldA"), result.first().path)
  }

  @Test
  fun visibleValidationErrors_forRepeatedFields_usesPathScopedSubmitAttempt() {
    val result =
      ValidationPresentation.visibleValidationErrors(
        errors = listOf(repeatedErrorA0, repeatedErrorA1),
        touchedFieldIds = emptySet(),
        touchedFieldPaths = emptySet(),
        showAllValidationErrors = true,
        submitAttemptedFieldIds = emptySet(),
        submitAttemptedFieldPaths = setOf("repeatGroup.1.fieldA"),
      )

    assertEquals(1, result.size)
    assertEquals(listOf("repeatGroup", "1", "fieldA"), result.first().path)
  }

  @Test
  fun shouldShowSubmitValidationDialog_false_forReadOnly_evenWhenEnabledAndErrorsExist() {
    val shouldShow =
      ValidationPresentation.shouldShowSubmitValidationDialog(
        showValidationDialogOnSubmit = true,
        mode = QuestionnaireMode.ReadOnly,
        errors = listOf(errorA),
      )

    assertFalse(shouldShow)
  }

  @Test
  fun shouldShowSubmitValidationDialog_true_forEdit_whenEnabledAndErrorsExist() {
    val shouldShow =
      ValidationPresentation.shouldShowSubmitValidationDialog(
        showValidationDialogOnSubmit = true,
        mode = QuestionnaireMode.Edit,
        errors = listOf(errorA),
      )

    assertTrue(shouldShow)
  }

  @Test
  fun formatValidationReason_mapsRequiredMessages_toFriendlyText() {
    val reason1 = ValidationPresentation.formatValidationReason(errorA)
    val reason2 =
      ValidationPresentation.formatValidationReason(
        ValidationError(linkId = "x", path = emptyList(), message = "required")
      )

    assertEquals("This field is required.", reason1)
    assertEquals("This field is required.", reason2)
  }

  @Test
  fun formatValidationReason_keepsCustomMessage_whenNotRequired() {
    val reason = ValidationPresentation.formatValidationReason(errorB)
    assertEquals("fieldB.range", reason)
  }
}
