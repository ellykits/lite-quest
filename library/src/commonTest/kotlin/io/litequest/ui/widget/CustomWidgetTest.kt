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
package io.litequest.ui.widget

import androidx.compose.runtime.Composable
import io.litequest.model.Item
import io.litequest.model.ItemType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonElement

class CustomWidgetTest {

  private class CustomWidget(override val item: Item) : ItemWidget {
    @Composable
    override fun Render(
      value: JsonElement?,
      onValueChange: (JsonElement, String?) -> Unit,
      errorMessage: String?,
    ) {
      // No-op for testing
    }
  }

  @Test
  fun testCustomWidgetRegistration() {
    val factory = DefaultWidgetFactory()
    val customType = ItemType("CUSTOM")

    factory.registerWidget(customType) { CustomWidget(it) }

    val item = Item(linkId = "test", type = customType, text = "Test Custom Widget")

    val widget = factory.createWidget(item)
    assertTrue(widget is CustomWidget)
    assertEquals(item, widget.item)
  }

  @Test
  fun testStandardWidgetStillWorks() {
    val factory = DefaultWidgetFactory()
    val item = Item(linkId = "test", type = ItemType.STRING, text = "Test Standard Widget")

    val widget = factory.createWidget(item)
    // Since we can't easily check the private TextInputWidget type from here (it might be
    // internal/private)
    // we just check that it created SOMETHING and didn't throw.
    assertEquals(item, widget.item)
  }
}
