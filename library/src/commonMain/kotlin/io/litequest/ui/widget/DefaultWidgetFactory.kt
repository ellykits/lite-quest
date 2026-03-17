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
import io.litequest.ui.widget.media.PhotoSelectorWidget
import io.litequest.ui.widget.numeric.DecimalInputWidget
import io.litequest.ui.widget.numeric.IntegerInputWidget
import io.litequest.ui.widget.numeric.QuantityWidget
import io.litequest.ui.widget.text.TextInputWidget

class DefaultWidgetFactory : WidgetFactory {
  override fun createWidget(item: Item): ItemWidget {
    return when (item.type) {
      ItemType.STRING,
      ItemType.TEXT -> TextInputWidget(item)
      ItemType.BOOLEAN -> BooleanWidget(item)
      ItemType.DECIMAL -> DecimalInputWidget(item)
      ItemType.INTEGER -> IntegerInputWidget(item)
      ItemType.DATE -> DatePickerWidget(item)
      ItemType.TIME -> TimePickerWidget(item)
      ItemType.DATETIME -> DateTimePickerWidget(item)
      ItemType.CHOICE -> ChoiceWidget(item)
      ItemType.OPEN_CHOICE -> OpenChoiceWidget(item)
      ItemType.DISPLAY -> DisplayWidget(item)
      ItemType.QUANTITY -> QuantityWidget(item)
      ItemType.BARCODE -> BarcodeScannerWidget(item)
      ItemType.PHOTO -> PhotoSelectorWidget(item)
      ItemType.ATTACHMENT -> AttachmentWidget(item)
      ItemType.LAYOUT_ROW -> RowLayoutWidget(item)
      ItemType.LAYOUT_COLUMN -> ColumnLayoutWidget(item)
      ItemType.LAYOUT_BOX -> BoxLayoutWidget(item)
      ItemType.GROUP -> {
        if (item.repeats) {
          RepeatingGroupWidget(item)
        } else {
          GroupWidget(item)
        }
      }
    }
  }
}
