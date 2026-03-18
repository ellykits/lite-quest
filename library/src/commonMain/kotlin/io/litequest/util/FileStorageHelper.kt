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
package io.litequest.util

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import io.litequest.model.Attachment
import io.litequest.model.QuestionnaireResponse
import io.litequest.model.ResponseItem
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

expect suspend fun copyToAppStorage(file: PlatformFile): PlatformFile

expect suspend fun deleteFromAppStorage(filePath: String): Boolean

object FileStorageHelper {

  suspend fun createAttachment(file: PlatformFile, contentType: String? = null): Attachment {
    val storedFile = copyToAppStorage(file)
    val mimeType = contentType ?: guessMimeType(file.extension)
    return Attachment(
      contentType = mimeType,
      size = storedFile.size(),
      title = storedFile.name,
      url = storedFile.path,
    )
  }

  suspend fun deleteAttachment(attachment: Attachment): Boolean {
    return deleteFromAppStorage(attachment.url)
  }

  @OptIn(ExperimentalEncodingApi::class)
  suspend fun getAttachmentAsBase64(file: PlatformFile): String {
    val bytes = file.readBytes()
    return Base64.encode(bytes)
  }

  @OptIn(ExperimentalEncodingApi::class)
  suspend fun getAttachmentAsDataUri(file: PlatformFile, contentType: String? = null): String {
    val mimeType = contentType ?: guessMimeType(file.extension)
    val base64Data = getAttachmentAsBase64(file)
    return "data:$mimeType;base64,$base64Data"
  }

  fun findAttachment(response: QuestionnaireResponse, linkId: String): Attachment? {
    return findAttachmentInItems(response.items, linkId)
  }

  private fun findAttachmentInItems(items: List<ResponseItem>, linkId: String): Attachment? {
    items.forEach { item ->
      if (item.linkId == linkId) {
        return item.answers.firstOrNull()?.attachment
      }
      if (item.items.isNotEmpty()) {
        val found = findAttachmentInItems(item.items, linkId)
        if (found != null) return found
      }
    }
    return null
  }

  private fun guessMimeType(extension: String): String {
    return when (extension.lowercase()) {
      "jpg",
      "jpeg" -> "image/jpeg"
      "png" -> "image/png"
      "gif" -> "image/gif"
      "webp" -> "image/webp"
      "bmp" -> "image/bmp"
      "pdf" -> "application/pdf"
      "doc" -> "application/msword"
      "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
      "xls" -> "application/vnd.ms-excel"
      "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
      "txt" -> "text/plain"
      "csv" -> "text/csv"
      else -> "application/octet-stream"
    }
  }
}
