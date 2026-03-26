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
package io.litequest.ui.widget.choice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull

class ChoiceUtilsTest {
  @Test
  fun toggleSingleChoice_returnsNull_whenClickingSelectedAgain() {
    val result = toggleSingleChoice(currentCode = "m", clickedCode = "m")
    assertNull(result)
  }

  @Test
  fun toggleSingleChoice_returnsClickedCode_whenDifferentSelection() {
    val result = toggleSingleChoice(currentCode = "m", clickedCode = "f")
    assertEquals("f", result)
  }

  @Test
  fun multiSelectionToJson_returnsJsonNull_whenEmpty() {
    val result = multiSelectionToJson(emptySet())
    assertEquals(JsonNull, result)
  }

  @Test
  fun multiSelectionToJson_returnsArray_whenNotEmpty() {
    val result = multiSelectionToJson(setOf("a", "b"))
    assertIs<JsonArray>(result)
    assertEquals(2, result.size)
  }
}
