package io.litequest.ui.widget

import io.litequest.model.Item
import io.litequest.model.ItemType

interface WidgetFactory {
  fun createWidget(item: Item): ItemWidget

  fun supports(itemType: ItemType): Boolean
}
