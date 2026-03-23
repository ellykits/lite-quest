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
import io.litequest.ui.widget.choice.ChoiceWidget
import io.litequest.ui.widget.choice.OpenChoiceWidget
import io.litequest.ui.widget.container.BoxLayoutWidget
import io.litequest.ui.widget.container.ColumnLayoutWidget
import io.litequest.ui.widget.container.RowLayoutWidget
import io.litequest.ui.widget.datetime.DatePickerWidget
import io.litequest.ui.widget.datetime.DateTimePickerWidget
import io.litequest.ui.widget.datetime.TimePickerWidget
import io.litequest.ui.widget.display.DisplayWidget
import io.litequest.ui.widget.group.GroupWidget
import io.litequest.ui.widget.group.RepeatingGroupWidget
import io.litequest.ui.widget.media.AttachmentWidget
import io.litequest.ui.widget.media.BarcodeScannerWidget
import io.litequest.ui.widget.media.ImageSelectorWidget
import io.litequest.ui.widget.numeric.DecimalInputWidget
import io.litequest.ui.widget.numeric.IntegerInputWidget
import io.litequest.ui.widget.numeric.QuantityWidget
import io.litequest.ui.widget.text.TextInputWidget

class DefaultWidgetFactory : WidgetFactory {
  private val registry = mutableMapOf<ItemType, (Item) -> ItemWidget>()

  init {
    registerStandardWidgets()
  }

  override fun createWidget(item: Item): ItemWidget {
    return registry[item.type]?.invoke(item)
      ?: error("No widget registered for type: ${item.type.value}")
  }

  fun registerWidget(type: ItemType, factory: (Item) -> ItemWidget) {
    registry[type] = factory
  }

  private fun registerStandardWidgets() {
    registerWidget(ItemType.STRING) { TextInputWidget(it) }
    registerWidget(ItemType.TEXT) { TextInputWidget(it) }
    registerWidget(ItemType.BOOLEAN) { BooleanWidget(it) }
    registerWidget(ItemType.DECIMAL) { DecimalInputWidget(it) }
    registerWidget(ItemType.INTEGER) { IntegerInputWidget(it) }
    registerWidget(ItemType.DATE) { DatePickerWidget(it) }
    registerWidget(ItemType.TIME) { TimePickerWidget(it) }
    registerWidget(ItemType.DATETIME) { DateTimePickerWidget(it) }
    registerWidget(ItemType.CHOICE) { ChoiceWidget(it) }
    registerWidget(ItemType.OPEN_CHOICE) { OpenChoiceWidget(it) }
    registerWidget(ItemType.DISPLAY) { DisplayWidget(it) }
    registerWidget(ItemType.QUANTITY) { QuantityWidget(it) }
    registerWidget(ItemType.BARCODE) { BarcodeScannerWidget(it) }
    registerWidget(ItemType.IMAGE) { ImageSelectorWidget(it) }
    registerWidget(ItemType.ATTACHMENT) { AttachmentWidget(it) }
    registerWidget(ItemType.LAYOUT_ROW) { RowLayoutWidget(it) }
    registerWidget(ItemType.LAYOUT_COLUMN) { ColumnLayoutWidget(it) }
    registerWidget(ItemType.LAYOUT_BOX) { BoxLayoutWidget(it) }
    registerWidget(ItemType.GROUP) { item ->
      if (item.repeats) {
        RepeatingGroupWidget(item)
      } else {
        GroupWidget(item)
      }
    }
  }
}
