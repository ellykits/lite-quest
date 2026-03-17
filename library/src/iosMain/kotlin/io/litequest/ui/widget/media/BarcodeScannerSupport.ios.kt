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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.ncgroup.kscan.BarcodeFormat
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerView
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType

@Composable
actual fun BarcodeScannerView(
  modifier: Modifier,
  onResult: (String) -> Unit,
  onCancel: () -> Unit,
) {
  var permissionStatus by remember {
    mutableStateOf(AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo))
  }

  LaunchedEffect(Unit) {
    if (permissionStatus == AVAuthorizationStatusNotDetermined) {
      AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
        permissionStatus =
          if (granted) AVAuthorizationStatusAuthorized else AVAuthorizationStatusDenied
      }
    }
  }

  when (permissionStatus) {
    AVAuthorizationStatusAuthorized -> {
      ScannerView(
        modifier = modifier,
        codeTypes = listOf(BarcodeFormat.FORMAT_ALL_FORMATS),
        result = { resultValue ->
          when (resultValue) {
            is BarcodeResult.OnSuccess -> {
              val data = resultValue.barcode.data
              if (data.isNotEmpty()) {
                onResult(data)
              }
            }
            is BarcodeResult.OnFailed,
            is BarcodeResult.OnCanceled -> {
              onCancel()
            }
          }
        },
      )
    }
    AVAuthorizationStatusDenied,
    AVAuthorizationStatusRestricted -> {
      Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Camera permission denied. Please enable it in Settings.")
      }
    }
    else -> {
      Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Requesting camera permission...")
      }
    }
  }
}

actual val isBarcodeScannerSupported: Boolean = true
