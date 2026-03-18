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
import java.util.UUID

actual suspend fun copyToAppStorage(file: PlatformFile): PlatformFile {
  val appFilesDir = File(System.getProperty("user.home"), ".litequest/attachments")
  appFilesDir.mkdirs()

  val fileName = "${UUID.randomUUID()}_${file.name}"
  val destFile = File(appFilesDir, fileName)
  destFile.writeBytes(file.readBytes())

  return PlatformFile(destFile.absolutePath)
}
