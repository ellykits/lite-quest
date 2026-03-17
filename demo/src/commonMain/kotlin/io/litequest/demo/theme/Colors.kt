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

// Health-friendly color palette
private val HealthTeal = Color(0xFF00897B)
private val HealthTealLight = Color(0xFF4DB6AC)
private val HealthTealDark = Color(0xFF00695C)
private val HealthBlue = Color(0xFF1976D2)
private val HealthBlueLight = Color(0xFF42A5F5)
private val HealthBlueDark = Color(0xFF0D47A1)
private val HealthOrange = Color(0xFFFF6F00)
private val HealthOrangeLight = Color(0xFFFF9800)
private val HealthOrangeDark = Color(0xFFE65100)

val LightColorScheme =
  lightColorScheme(
    primary = HealthTeal,
    onPrimary = Color.White,
    primaryContainer = HealthTealLight,
    onPrimaryContainer = Color(0xFF003730),
    secondary = HealthBlue,
    onSecondary = Color.White,
    secondaryContainer = HealthBlueLight,
    onSecondaryContainer = Color(0xFF001D35),
    tertiary = HealthOrange,
    onTertiary = Color.White,
    tertiaryContainer = HealthOrangeLight,
    onTertiaryContainer = Color(0xFF2E1500),
    background = Color(0xFFFAFDFD),
    onBackground = Color(0xFF191C1C),
    surface = Color(0xFFFAFDFD),
    onSurface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFFDAE5E3),
    onSurfaceVariant = Color(0xFF3F4947),
    outline = Color(0xFF6F7977),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
  )

val DarkColorScheme =
  darkColorScheme(
    primary = HealthTealLight,
    onPrimary = Color(0xFF003730),
    primaryContainer = HealthTealDark,
    onPrimaryContainer = Color(0xFF70F2E2),
    secondary = HealthBlueLight,
    onSecondary = Color(0xFF001D35),
    secondaryContainer = HealthBlueDark,
    onSecondaryContainer = Color(0xFFA8C8FF),
    tertiary = HealthOrangeLight,
    onTertiary = Color(0xFF2E1500),
    tertiaryContainer = HealthOrangeDark,
    onTertiaryContainer = Color(0xFFFFDCC2),
    background = Color(0xFF191C1C),
    onBackground = Color(0xFFE0E3E2),
    surface = Color(0xFF191C1C),
    onSurface = Color(0xFFE0E3E2),
    surfaceVariant = Color(0xFF3F4947),
    onSurfaceVariant = Color(0xFFBEC9C6),
    outline = Color(0xFF889391),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
  )
