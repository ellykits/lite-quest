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

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberCameraLauncher(onResult: (PlatformFile?) -> Unit): CameraLauncherInterface? {
  val launcher = rememberCameraPickerLauncher(onResult = onResult)
  return object : CameraLauncherInterface {
    override fun launch() {
      val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
      when (status) {
        AVAuthorizationStatusAuthorized -> launcher.launch()
        AVAuthorizationStatusNotDetermined -> {
          AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
            if (granted) {
              dispatch_async(dispatch_get_main_queue()) { launcher.launch() }
            }
          }
        }
        else -> {
          launcher.launch()
        }
      }
    }
  }
}

actual val isCameraSupported: Boolean = true
