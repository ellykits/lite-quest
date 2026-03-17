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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash2
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.launch as coroutineLaunch

private const val MAX_FILE_SIZE = 50 * 1024 * 1024 // 50 MB

/** Generic media picker that optionally includes a camera button for photo capture. */
@Composable
internal fun MediaPickerComponent(
  title: String,
  value: String,
  onValueChange: (String) -> Unit,
  pickerType: FileKitType,
  buttonText: String,
  showCameraOption: Boolean = false,
  errorMessage: String? = null,
) {
  var sizeError by remember { mutableStateOf<String?>(null) }
  var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
  val coroutineScope = rememberCoroutineScope()

  val imageExtensions = listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")

  LaunchedEffect(value) {
    if (value.isEmpty()) {
      imageBytes = null
    }
  }

  fun handleFileSelection(platformFile: PlatformFile) {
    if (platformFile.size() > MAX_FILE_SIZE.toLong()) {
      sizeError = "File size exceeds the 50MB limit"
      imageBytes = null
    } else {
      sizeError = null
      onValueChange(platformFile.name)
      if (platformFile.extension.lowercase() in imageExtensions) {
        coroutineScope.coroutineLaunch {
          try {
            imageBytes = platformFile.readBytes()
          } catch (e: Exception) {
            imageBytes = null
          }
        }
      } else {
        imageBytes = null
      }
    }
  }

  val galleryLauncher =
    rememberFilePickerLauncher(type = pickerType) { file -> file?.let { handleFileSelection(it) } }

  val cameraLauncher = rememberCameraLauncher { file: PlatformFile? ->
    if (file != null) {
      handleFileSelection(file)
    }
  }

  OutlinedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(text = title, style = MaterialTheme.typography.titleMedium)
      Spacer(modifier = Modifier.height(8.dp))

      if (value.isNotEmpty()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
          Text(
            text = "Selected: $value",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
          )
          IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(24.dp)) {
            Icon(
              imageVector = Lucide.Trash2,
              contentDescription = "Remove selection",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(18.dp),
            )
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
      }

      if (isCameraSupported && showCameraOption) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (cameraLauncher != null) {
            Button(
              onClick = { cameraLauncher.launch() },
              modifier = Modifier.weight(1f),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
              Icon(
                imageVector = Lucide.Camera,
                contentDescription = "Camera",
                modifier = Modifier.size(18.dp),
              )
              Text(text = "  Camera", style = MaterialTheme.typography.labelLarge)
            }
          }
          OutlinedButton(onClick = { galleryLauncher.launch() }, modifier = Modifier.weight(1f)) {
            Icon(
              imageVector = Lucide.Image,
              contentDescription = "Gallery",
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = "  Gallery",
              style = MaterialTheme.typography.labelLarge,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      } else {
        Button(onClick = { galleryLauncher.launch() }, modifier = Modifier.wrapContentWidth()) {
          Text(
            if (value.isEmpty()) buttonText else "Change $buttonText",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      val displayError = sizeError ?: errorMessage
      if (displayError != null) {
        Text(
          text = displayError,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(top = 8.dp),
        )
      }

      if (imageBytes != null) {
        Spacer(modifier = Modifier.height(16.dp))
        AsyncImage(
          model = imageBytes,
          contentDescription = "Selected image preview",
          modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
          contentScale = ContentScale.Crop,
        )
      }
    }
  }
}
