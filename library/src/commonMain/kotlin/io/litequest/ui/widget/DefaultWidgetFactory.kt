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
