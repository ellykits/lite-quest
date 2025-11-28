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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.litequest.model.Item
import io.litequest.model.ItemType
import io.litequest.model.QuestionnaireResponse
import io.litequest.state.QuestionnaireState
import io.litequest.ui.pagination.PaginatedQuestionnaire
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SummaryPage(
  state: QuestionnaireState,
  modifier: Modifier = Modifier,
  paginatedQuestionnaire: PaginatedQuestionnaire? = null,
) {
  val shouldShowPageHeaders =
    paginatedQuestionnaire?.let { paginated -> paginated.pages.any { page -> page.items.size > 1 } }
      ?: false

  LazyColumn(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    if (paginatedQuestionnaire != null && shouldShowPageHeaders) {
      paginatedQuestionnaire.pages.forEach { page ->
        item(key = page.id) {
          PageCard(
            pageTitle = page.title,
            pageNumber = page.order + 1,
            items = page.items,
            response = state.response,
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
            state.questionnaire.items.forEach { item ->
              SummaryItem(item = item, response = state.response)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PageCard(
  pageTitle: String,
  pageNumber: Int,
  items: List<Item>,
  response: QuestionnaireResponse,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = MaterialTheme.shapes.large,
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
      Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Box(
            modifier =
              Modifier.size(32.dp)
                .background(
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shape = CircleShape,
                ),
            contentAlignment = androidx.compose.ui.Alignment.Center,
          ) {
            Text(
              text = pageNumber.toString(),
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
          }
          Text(
            text = "Page",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Spacer(Modifier.width(12.dp))
        Text(
          text = pageTitle,
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      Spacer(Modifier.height(16.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
      Spacer(Modifier.height(16.dp))

      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items.forEach { item -> SummaryItem(item = item, response = response) }
      }
    }
  }
}

@Composable
private fun SummaryItem(item: Item, response: QuestionnaireResponse, level: Int = 0) {
  val responseItem = response.items.find { it.linkId == item.linkId }
  val value = responseItem?.answers?.firstOrNull()?.value

  when {
    item.type == ItemType.GROUP && item.repeats -> {
      val groupValue = value as? JsonArray

      if (groupValue != null && groupValue.isNotEmpty()) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          if (item.text.isNotEmpty()) {
            Text(
              text = item.text,
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.primary,
            )
          }

          groupValue.forEachIndexed { index, instanceValue ->
            if (instanceValue is JsonObject) {
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                  CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                  ),
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
                      SummaryFieldItem(
                        label = childItem.text,
                        value = childValue.jsonPrimitive.content,
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    item.type == ItemType.GROUP -> {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        if (item.text.isNotEmpty()) {
          Text(
            text = item.text,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
          )
        }

        item.items.forEach { childItem ->
          SummaryItem(item = childItem, response = response, level = level + 1)
        }
      }
    }
    item.type == ItemType.DISPLAY -> {
      /* Not necessary */
    }
    value != null -> {
      SummaryFieldItem(label = item.text, value = value.jsonPrimitive.content)
    }
  }
}

@Composable
private fun SummaryFieldItem(label: String, value: String, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}
