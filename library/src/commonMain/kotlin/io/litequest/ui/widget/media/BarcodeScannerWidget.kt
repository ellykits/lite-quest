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
package io.litequest.ui.widget.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ScanBarcode
import io.litequest.model.Item
import io.litequest.ui.widget.ItemWidget
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

class BarcodeScannerWidget(override val item: Item) : ItemWidget {
  @Composable
  override fun Render(
    value: JsonElement?,
    onValueChange: (JsonElement, String?) -> Unit,
    errorMessage: String?,
  ) {
    var showScanner by remember { mutableStateOf(false) }
    val barcode = remember(value) { value?.jsonPrimitive?.content ?: "" }

    Box(modifier = Modifier.fillMaxWidth()) {
      OutlinedTextField(
        value = barcode,
        onValueChange = { onValueChange(JsonPrimitive(it), item.text) },
        label = { Text(item.text) },
        isError = errorMessage != null,
        supportingText = errorMessage?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !item.readOnly,
        readOnly = item.readOnly,
        trailingIcon = {
          if (!item.readOnly) {
            IconButton(onClick = { showScanner = true }) {
              Icon(Lucide.ScanBarcode, contentDescription = "Scan Barcode")
            }
          }
        },
      )

      if (showScanner && isBarcodeScannerSupported) {
        androidx.compose.ui.window.Dialog(
          onDismissRequest = { showScanner = false },
          properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        ) {
          Box(
            modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black)
          ) {
            BarcodeScannerView(
              modifier = Modifier.fillMaxSize(),
              onResult = { result ->
                showScanner = false
                onValueChange(JsonPrimitive(result), item.text)
              },
              onCancel = { showScanner = false },
            )
          }
        }
      }
    }
  }
}
