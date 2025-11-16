package io.litequest.ui.widget.group

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget
import io.litequest.ui.widget.WidgetFactory
import kotlinx.serialization.json.JsonElement

class GroupWidget(
  override val item: Item,
  private val widgetFactory: WidgetFactory,
  private val onValueChange: (String, JsonElement) -> Unit,
  private val values: Map<String, JsonElement?>,
  private val errorMessages: Map<String, String>,
) : ItemWidget {
  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement) -> Unit,
    errorMessage: String?,
  ) {
    var expanded by remember { mutableStateOf(true) }

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors =
        CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = item.text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
          )
          Text(
            text = if (expanded) "▼" else "▶",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        AnimatedVisibility(visible = expanded) {
          Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            item.items.forEach { childItem ->
              val childWidget = widgetFactory.createWidget(childItem)
              childWidget.Render(
                value = values[childItem.linkId],
                onValueChange = { newValue ->
                  this@GroupWidget.onValueChange(childItem.linkId, newValue)
                },
                errorMessage = errorMessages[childItem.linkId],
              )
            }
          }
        }
      }
    }
  }
}
