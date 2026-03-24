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
package io.litequest.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class ItemType(val value: String) {
  companion object {
    val STRING = ItemType("STRING")
    val TEXT = ItemType("TEXT")
    val BOOLEAN = ItemType("BOOLEAN")
    val DECIMAL = ItemType("DECIMAL")
    val INTEGER = ItemType("INTEGER")
    val DATE = ItemType("DATE")
    val TIME = ItemType("TIME")
    val DATETIME = ItemType("DATETIME")
    val CHOICE = ItemType("CHOICE")
    val OPEN_CHOICE = ItemType("OPEN_CHOICE")
    val DISPLAY = ItemType("DISPLAY")
    val GROUP = ItemType("GROUP")
    val QUANTITY = ItemType("QUANTITY")
    val BARCODE = ItemType("BARCODE")
    val IMAGE = ItemType("IMAGE")
    val ATTACHMENT = ItemType("ATTACHMENT")
    val LAYOUT_ROW = ItemType("LAYOUT_ROW")
    val LAYOUT_COLUMN = ItemType("LAYOUT_COLUMN")
    val LAYOUT_BOX = ItemType("LAYOUT_BOX")
  }
}
