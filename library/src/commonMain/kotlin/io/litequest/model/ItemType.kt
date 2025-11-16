package io.litequest.model

import kotlinx.serialization.Serializable

@Serializable
enum class ItemType {
  STRING,
  TEXT,
  BOOLEAN,
  DECIMAL,
  INTEGER,
  DATE,
  TIME,
  DATETIME,
  CHOICE,
  OPEN_CHOICE,
  DISPLAY,
  GROUP,
  QUANTITY,
  ;

  companion object {
    fun fromString(value: String): ItemType {
      return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
        ?: throw IllegalArgumentException("Unknown item type: $value")
    }
  }
}
