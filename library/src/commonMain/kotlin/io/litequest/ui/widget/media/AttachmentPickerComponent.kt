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
import androidx.compose.material3.CircularProgressIndicator
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
import com.composables.icons.lucide.FileText
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
import io.litequest.model.Attachment
import io.litequest.util.FileStorageHelper
import io.litequest.util.JsonUtil
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

private const val MAX_FILE_SIZE = 50 * 1024 * 1024
private const val MAX_IMAGE_SIZE = 10 * 1024 * 1024

@Composable
internal fun AttachmentPickerComponent(
  title: String,
  attachment: Attachment?,
  onAttachmentChange: (Attachment?) -> Unit,
  pickerType: FileKitType,
  buttonText: String,
  showCameraOption: Boolean = false,
  errorMessage: String? = null,
) {
  var sizeError by remember { mutableStateOf<String?>(null) }
  var imagePreviewBytes by remember(attachment?.url) { mutableStateOf<ByteArray?>(null) }
  var isProcessing by remember { mutableStateOf(false) }
  var processingFileName by remember { mutableStateOf<String?>(null) }
  val coroutineScope = rememberCoroutineScope()

  val imageExtensions = listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")

  LaunchedEffect(attachment?.url) {
    if (attachment != null && imagePreviewBytes == null) {
      val isImage = attachment.contentType.startsWith("image/")
      if (isImage) {
        try {
          val file = PlatformFile(attachment.url)
          imagePreviewBytes = file.readBytes()
        } catch (e: Exception) {
          imagePreviewBytes = null
        }
      }
    }
  }

  fun handleFileSelection(platformFile: PlatformFile) {
    val fileSize = platformFile.size()
    val isImage = platformFile.extension.lowercase() in imageExtensions
    val maxSize = if (isImage) MAX_IMAGE_SIZE else MAX_FILE_SIZE

    if (fileSize > maxSize) {
      val maxSizeMB = maxSize / (1024 * 1024)
      sizeError =
        if (isImage) {
          "Image size exceeds ${maxSizeMB}MB limit"
        } else {
          "File size exceeds ${maxSizeMB}MB limit"
        }
      onAttachmentChange(null)
      imagePreviewBytes = null
      isProcessing = false
      processingFileName = null
    } else {
      sizeError = null
      isProcessing = true
      processingFileName = platformFile.name

      coroutineScope.launch {
        try {
          val newAttachment = FileStorageHelper.createAttachment(platformFile)
          onAttachmentChange(newAttachment)

          if (isImage) {
            imagePreviewBytes = platformFile.readBytes()
          } else {
            imagePreviewBytes = null
          }
          isProcessing = false
          processingFileName = null
        } catch (e: Exception) {
          sizeError = "Failed to process file: ${e.message}"
          onAttachmentChange(null)
          imagePreviewBytes = null
          isProcessing = false
          processingFileName = null
        }
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

      if (isProcessing && processingFileName != null) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Processing: $processingFileName",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.secondary,
            )
            Text(
              text = "Saving file...",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }
        Spacer(modifier = Modifier.height(8.dp))
      } else if (attachment != null) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = attachment.title,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = formatFileSize(attachment.size),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          IconButton(onClick = { onAttachmentChange(null) }, modifier = Modifier.size(40.dp)) {
            Icon(
              imageVector = Lucide.Trash2,
              contentDescription = "Remove attachment",
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
          Icon(
            imageVector = if (pickerType is FileKitType.Image) Lucide.Image else Lucide.FileText,
            contentDescription = buttonText,
            modifier = Modifier.size(18.dp),
          )
          Text(
            "  ${if (attachment == null) buttonText else "Change $buttonText"}",
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

      if (imagePreviewBytes != null) {
        Spacer(modifier = Modifier.height(16.dp))
        AsyncImage(
          model = imagePreviewBytes,
          contentDescription = "Selected image preview",
          modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
          contentScale = ContentScale.Crop,
        )
      }
    }
  }
}

internal fun encodeAttachment(attachment: Attachment?): JsonElement {
  return if (attachment != null) {
    JsonUtil.encode(Attachment.serializer(), attachment)
  } else {
    JsonNull
  }
}

private fun formatFileSize(bytes: Long): String {
  return when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${bytes / (1024 * 1024)} MB"
  }
}
