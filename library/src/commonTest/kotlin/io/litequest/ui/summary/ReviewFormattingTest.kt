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
package io.litequest.ui.summary

import androidx.compose.runtime.Composable
import io.litequest.model.AnswerOption
import io.litequest.model.Item
import io.litequest.model.ItemType
import io.litequest.ui.widget.DefaultWidgetFactory
import io.litequest.ui.widget.ItemWidget
import io.litequest.ui.widget.WidgetFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonElement

private val lookupType = ItemType("LOOKUP")

private class LookupStubWidget(override val item: Item) : ItemWidget {
  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement, String?) -> Unit,
    errorMessage: String?,
  ) = Unit

  override fun formatForReview(value: Any): String? = (value as? Map<*, *>)?.get("name")?.toString()
}

private class StubFactory : WidgetFactory {
  private val defaultFactory = DefaultWidgetFactory()

  override fun createWidget(item: Item): ItemWidget =
    when (item.type) {
      lookupType -> LookupStubWidget(item)
      else -> defaultFactory.createWidget(item)
    }
}

class ReviewFormattingTest {

  private val lookupItem = Item(linkId = "vendor", type = lookupType, text = "Vendor")

  @Test
  fun overridingWidgetDecidesTheReviewText() {
    val value = mapOf("id" to "abc-123", "name" to "Acme Supplies", "prefix" to "VN-")

    assertEquals("Acme Supplies", resolveDisplayText(value, lookupItem, StubFactory()))
  }

  @Test
  fun decliningWidgetFallsBackToLibraryFormatting() {
    val item =
      Item(
        linkId = "status",
        type = ItemType.CHOICE,
        text = "Status",
        answerOptions = listOf(AnswerOption(code = "OPEN", display = "Open")),
      )

    assertEquals("Open", resolveDisplayText("OPEN", item, StubFactory()))
  }

  @Test
  fun noFactoryKeepsExistingFormatting() {
    val value = mapOf("value" to "12", "unit" to "kg")
    val item = Item(linkId = "weight", type = ItemType.QUANTITY, text = "Weight")

    assertEquals("12 kg", resolveDisplayText(value, item, null))
  }

  @Test
  fun repeatingGroupChildrenHonourTheOverride() {
    val group =
      Item(
        linkId = "lines",
        type = ItemType.GROUP,
        text = "Lines",
        repeats = true,
        items = listOf(lookupItem, Item(linkId = "qty", type = ItemType.INTEGER, text = "Quantity")),
      )
    val instance =
      mapOf("vendor" to mapOf("id" to "abc-123", "name" to "Acme Supplies"), "qty" to 4)

    assertEquals(
      listOf("Vendor" to "Acme Supplies", "Quantity" to "4"),
      repetitionDisplayValues(group, instance, StubFactory()).map { (item, text) ->
        item.text to text
      },
    )
  }
}
