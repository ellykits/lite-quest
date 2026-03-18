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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Lucide
import io.litequest.model.Item
import io.litequest.model.ItemType
import io.litequest.state.QuestionnaireState
import io.litequest.ui.pagination.PaginatedQuestionnaire
import io.litequest.util.DataContextBuilder
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SummaryPage(
  state: QuestionnaireState,
  modifier: Modifier = Modifier,
  paginatedQuestionnaire: PaginatedQuestionnaire? = null,
  onSubmit: (() -> Unit)? = null,
) {
  val shouldShowPageHeaders =
    paginatedQuestionnaire?.let { paginated -> paginated.pages.any { page -> page.items.size > 1 } }
      ?: false

  val flatAnswers =
    remember(state.response, state.calculatedValues) {
      val answers = DataContextBuilder.build(state.response).toMutableMap()
      answers.putAll(state.calculatedValues)
      answers
    }

  LazyColumn(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    if (paginatedQuestionnaire != null && shouldShowPageHeaders) {
      paginatedQuestionnaire.pages.forEach { page ->
        item(key = page.id) {
          PaginatedPageCard(
            pageTitle = page.title,
            pageNumber = page.order + 1,
            items = page.items,
            flatAnswers = flatAnswers,
            visibleItems = state.visibleItems,
          )
        }
      }
    } else {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = MaterialTheme.shapes.large,
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            state.visibleItems.forEach { item ->
              SummaryItem(item = item, flatAnswers = flatAnswers)
            }
          }
        }
      }
    }

    if (onSubmit != null) {
      item {
        Button(
          onClick = onSubmit,
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
          enabled = state.isValid,
        ) {
          Text("Submit")
        }
      }
    }
  }
}

@Composable
private fun PaginatedPageCard(
  pageTitle: String,
  pageNumber: Int,
  items: List<Item>,
  flatAnswers: Map<String, Any?>,
  visibleItems: List<Item>,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    colors =
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    shape = MaterialTheme.shapes.extraLarge,
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 20.dp, vertical = 16.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
            modifier =
              Modifier.size(40.dp)
                .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = pageNumber.toString(),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onPrimary,
            )
          }
          Column {
            Text(
              text = pageTitle,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
              text = "Page $pageNumber",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
          }
        }
      }

      Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        items.forEach { item ->
          if (visibleItems.any { it.linkId == item.linkId }) {
            SummaryItem(item = item, flatAnswers = flatAnswers)
          }
        }
      }
    }
  }
}

@Composable
private fun SummaryItem(item: Item, flatAnswers: Map<String, Any?>, level: Int = 0) {
  val value = flatAnswers[item.linkId]

  when (item.type) {
    ItemType.GROUP -> {
      if (item.repeats) {
        if (value != null) {
          RenderRepeatingGroup(item, value)
        }
      } else {
        RenderNonRepeatingGroup(item, flatAnswers, level)
      }
    }
    ItemType.LAYOUT_ROW,
    ItemType.LAYOUT_COLUMN,
    ItemType.LAYOUT_BOX -> {
      item.items.forEach { childItem ->
        SummaryItem(item = childItem, flatAnswers = flatAnswers, level = level)
      }
    }
    ItemType.DISPLAY -> {}
    else -> {
      if (value != null) {
        val displayValue = getChoiceDisplayValue(value, item)
        SummaryFieldItem(label = item.text, value = displayValue, type = item.type, item = item)
      }
    }
  }
}

@Composable
private fun RenderRepeatingGroup(item: Item, value: Any?) {
  if (value !is List<*> || value.isEmpty()) return

  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    if (item.text.isNotEmpty()) {
      Text(
        text = item.text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
      )
    }

    value.forEachIndexed { index, instanceValue ->
      if (instanceValue is Map<*, *>) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            ),
          shape = MaterialTheme.shapes.medium,
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              text = "${item.text} #${index + 1}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            item.items.forEach { childItem ->
              val childValue = instanceValue[childItem.linkId]
              if (childValue != null) {
                val displayValue = getChoiceDisplayValue(childValue, childItem)
                SummaryFieldItem(
                  label = childItem.text,
                  value = displayValue,
                  type = childItem.type,
                  item = childItem,
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RenderNonRepeatingGroup(item: Item, flatAnswers: Map<String, Any?>, level: Int) {
  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    if (item.text.isNotEmpty()) {
      Text(
        text = item.text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
      )
    }

    item.items.forEach { childItem ->
      SummaryItem(item = childItem, flatAnswers = flatAnswers, level = level + 1)
    }
  }
}

private fun hasAnyChildValues(item: Item, flatAnswers: Map<String, Any?>): Boolean {
  return item.items.any { childItem ->
    when (childItem.type) {
      ItemType.DISPLAY -> false
      ItemType.GROUP,
      ItemType.LAYOUT_ROW,
      ItemType.LAYOUT_COLUMN,
      ItemType.LAYOUT_BOX -> hasAnyChildValues(childItem, flatAnswers)
      else -> flatAnswers[childItem.linkId] != null
    }
  }
}

@Composable
private fun SummaryFieldItem(
  label: String,
  value: Any,
  type: ItemType,
  modifier: Modifier = Modifier,
  item: Item? = null,
) {
  val displayValue = formatValueForDisplay(value, type, item)
  val icon = getIconForType(type)

  if (label.isEmpty()) {
    return
  }

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.primary,
        )
      }
      Text(
        text = displayValue,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

private fun formatValueForDisplay(value: Any, type: ItemType, item: Item? = null): String {
  return when (type) {
    ItemType.BOOLEAN -> if (value == true) "Yes" else "No"
    ItemType.CHOICE,
    ItemType.OPEN_CHOICE -> {
      when (value) {
        is List<*> -> value.joinToString(", ") { it.toString() }
        else -> value.toString()
      }
    }
    ItemType.QUANTITY -> {
      when (value) {
        is Map<*, *> -> {
          val amount = value["value"]?.toString() ?: ""
          val unit = value["unit"]?.toString() ?: ""
          if (unit.isNotEmpty()) "$amount $unit" else amount
        }
        else -> value.toString()
      }
    }
    ItemType.DATE -> formatDate(value.toString(), item)
    ItemType.TIME -> formatTime(value.toString(), item)
    ItemType.DATETIME -> formatDateTime(value.toString(), item)
    ItemType.DECIMAL -> {
      when (value) {
        is Double -> formatDecimal(value)
        is Number -> formatDecimal(value.toDouble())
        else -> value.toString()
      }
    }
    ItemType.INTEGER -> value.toString()
    ItemType.STRING,
    ItemType.TEXT -> value.toString()
    ItemType.IMAGE,
    ItemType.ATTACHMENT -> formatAttachment(value)
    ItemType.BARCODE -> value.toString()
    else -> value.toString()
  }
}

private fun formatAttachment(value: Any): String {
  return when (value) {
    is Map<*, *> -> {
      val title = value["title"]?.toString() ?: "Attachment"
      val size = value["size"]?.toString()?.toLongOrNull()
      if (size != null) {
        "$title (${formatFileSize(size)})"
      } else {
        title
      }
    }
    else -> value.toString()
  }
}

private fun formatFileSize(bytes: Long): String {
  return when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${bytes / (1024 * 1024)} MB"
  }
}

private fun formatDecimal(value: Double): String {
  val intPart = value.toLong()
  val decimalPart = ((value - intPart) * 100).toLong().absoluteValue
  return "$intPart.${decimalPart.toString().padStart(2, '0')}"
}

private fun formatDate(dateString: String, item: Item?): String {
  return dateString
}

private fun formatTime(timeString: String, item: Item?): String {
  return timeString
}

private fun formatDateTime(dateTimeString: String, item: Item?): String {
  val cleaned = dateTimeString.replace("Z", "").replace("+00:00", "")
  return cleaned.replace("T", " ")
}

private fun getIconForType(type: ItemType): ImageVector? {
  return when (type) {
    ItemType.IMAGE -> Lucide.Image
    ItemType.ATTACHMENT -> Lucide.FileText
    else -> null
  }
}
