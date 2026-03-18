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
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
actual suspend fun copyToAppStorage(file: PlatformFile): PlatformFile {
  val fileManager = NSFileManager.defaultManager
  val documentsPath =
    fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).first()
      as platform.Foundation.NSURL

  val attachmentsDir = documentsPath.URLByAppendingPathComponent("attachments")!!
  fileManager.createDirectoryAtURL(attachmentsDir, true, null, null)

  val fileName = "${NSUUID().UUIDString}_${file.name}"
  val destUrl = attachmentsDir.URLByAppendingPathComponent(fileName)!!
  val destPath = destUrl.path!!

  val bytes = file.readBytes()
  platform.Foundation.NSData.create(bytes = bytes, length = bytes.size.toULong())
    ?.writeToFile(destPath, true)

  return PlatformFile(destPath)
}
