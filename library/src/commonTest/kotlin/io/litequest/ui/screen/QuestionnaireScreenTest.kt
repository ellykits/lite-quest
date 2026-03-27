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

import kotlin.test.Test
import kotlin.test.assertEquals

class QuestionnaireScreenTest {
  @Test
  fun reindexRepetitionPathsAfterRemoval_dropsRemovedRow_and_shiftsLaterRows() {
    val result =
      reindexRepetitionPathsAfterRemoval(
        paths = setOf("household.0.name", "household.1.name", "household.2.name", "other.field"),
        groupLinkId = "household",
        removedIndex = 1,
      )

    assertEquals(setOf("household.0.name", "household.1.name", "other.field"), result)
  }
}
