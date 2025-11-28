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

import io.litequest.model.Item
import io.litequest.model.ItemType
import io.litequest.ui.widget.choice.BooleanWidget
import io.litequest.ui.widget.display.DisplayWidget
import io.litequest.ui.widget.numeric.DecimalInputWidget
import io.litequest.ui.widget.numeric.IntegerInputWidget
import io.litequest.ui.widget.text.TextInputWidget

class DefaultWidgetFactory : WidgetFactory {
  private val registry = mutableMapOf<ItemType, (Item) -> ItemWidget>()

  init {
    registerDefaultWidgets()
  }

  override fun createWidget(item: Item): ItemWidget {
    val creator =
      registry[item.type]
        ?: throw IllegalArgumentException("No widget registered for ItemType: ${item.type}")
    return creator(item)
  }

  override fun supports(itemType: ItemType): Boolean {
    return registry.containsKey(itemType)
  }

  fun registerWidget(type: ItemType, creator: (Item) -> ItemWidget) {
    registry[type] = creator
  }

  private fun registerDefaultWidgets() {
    registerWidget(ItemType.TEXT) { TextInputWidget(it) }
    registerWidget(ItemType.DECIMAL) { DecimalInputWidget(it) }
    registerWidget(ItemType.INTEGER) { IntegerInputWidget(it) }
    registerWidget(ItemType.BOOLEAN) { BooleanWidget(it) }
    registerWidget(ItemType.DISPLAY) { DisplayWidget(it) }
  }
}
