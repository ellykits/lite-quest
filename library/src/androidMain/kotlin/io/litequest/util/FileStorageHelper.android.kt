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
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import java.io.File
import java.lang.ref.WeakReference
import java.util.UUID

private var androidContext: WeakReference<android.content.Context?> = WeakReference(null)

fun setAndroidContext(context: android.content.Context) {
  androidContext = WeakReference(context.applicationContext)
}

actual suspend fun copyToAppStorage(file: PlatformFile): PlatformFile {
  val context = androidContext.get()

  val appFilesDir = context?.let { File(it.filesDir, "attachments") }
  appFilesDir?.exists()?.let {
    if (!it) {
      appFilesDir.mkdirs()
    }
  }

  val fileName = "${UUID.randomUUID()}_${file.name}"
  val destFile = File(appFilesDir, fileName)
  destFile.writeBytes(file.readBytes())

  return PlatformFile(destFile.absolutePath)
}

actual suspend fun deleteFromAppStorage(filePath: String): Boolean {
  return try {
    val file = File(filePath)
    file.delete()
  } catch (e: Exception) {
    false
  }
}
