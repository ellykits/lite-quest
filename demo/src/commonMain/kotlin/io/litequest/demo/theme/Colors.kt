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
package io.litequest.demo.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val CyanBlue = Color(0xFF00BCD4)
private val CyanBlueLight = Color(0xFF62EFFF)
private val CyanBlueDark = Color(0xFF008BA3)
private val DeepBlue = Color(0xFF0288D1)
private val DeepBlueLight = Color(0xFF5EB8FF)
private val DeepBlueDark = Color(0xFF005B9F)
private val Amber = Color(0xFFFFC107)
private val AmberLight = Color(0xFFFFF350)
private val AmberDark = Color(0xFFC79100)

val LightColorScheme =
  lightColorScheme(
    primary = CyanBlue,
    onPrimary = Color.White,
    primaryContainer = CyanBlueLight,
    onPrimaryContainer = Color(0xFF003640),
    secondary = DeepBlue,
    onSecondary = Color.White,
    secondaryContainer = DeepBlueLight,
    onSecondaryContainer = Color(0xFF001D32),
    tertiary = Amber,
    onTertiary = Color(0xFF3E2E00),
    tertiaryContainer = AmberLight,
    onTertiaryContainer = Color(0xFF261900),
    background = Color(0xFFFAFDFD),
    onBackground = Color(0xFF191C1D),
    surface = Color(0xFFFAFDFD),
    onSurface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFFDCE4E9),
    onSurfaceVariant = Color(0xFF40484C),
    surfaceContainer = Color(0xFFECF2F7),
    surfaceContainerHigh = Color(0xFFE0E7EC),
    surfaceContainerHighest = Color(0xFFD5DBE0),
    surfaceContainerLow = Color(0xFFF2F8FD),
    surfaceContainerLowest = Color.White,
    outline = Color(0xFF70787D),
    outlineVariant = Color(0xFFC0C8CD),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
  )

val DarkColorScheme =
  darkColorScheme(
    primary = CyanBlueLight,
    onPrimary = Color(0xFF003640),
    primaryContainer = CyanBlueDark,
    onPrimaryContainer = Color(0xFFB3EBFF),
    secondary = DeepBlueLight,
    onSecondary = Color(0xFF001D32),
    secondaryContainer = DeepBlueDark,
    onSecondaryContainer = Color(0xFFB8D8FF),
    tertiary = AmberLight,
    onTertiary = Color(0xFF3E2E00),
    tertiaryContainer = AmberDark,
    onTertiaryContainer = Color(0xFFFFE6A8),
    background = Color(0xFF0F1415),
    onBackground = Color(0xFFDFE3E4),
    surface = Color(0xFF0F1415),
    onSurface = Color(0xFFDFE3E4),
    surfaceVariant = Color(0xFF40484C),
    onSurfaceVariant = Color(0xFFC0C8CD),
    surfaceContainer = Color(0xFF1B2226),
    surfaceContainerHigh = Color(0xFF252D31),
    surfaceContainerHighest = Color(0xFF30373C),
    surfaceContainerLow = Color(0xFF171C1F),
    surfaceContainerLowest = Color(0xFF0A0F11),
    outline = Color(0xFF8A9297),
    outlineVariant = Color(0xFF40484C),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
  )
