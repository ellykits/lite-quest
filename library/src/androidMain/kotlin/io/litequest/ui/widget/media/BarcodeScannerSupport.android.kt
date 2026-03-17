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

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import org.ncgroup.kscan.BarcodeFormat
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerView

@Composable
actual fun BarcodeScannerView(
  modifier: Modifier,
  onResult: (String) -> Unit,
  onCancel: () -> Unit,
) {
  val context = LocalContext.current
  var hasPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
      hasPermission = isGranted
      if (!isGranted) onCancel()
    }

  LaunchedEffect(Unit) {
    if (!hasPermission) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  if (hasPermission) {
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
}

actual val isBarcodeScannerSupported: Boolean = true
