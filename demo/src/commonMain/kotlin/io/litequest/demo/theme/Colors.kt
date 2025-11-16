package io.litequest.demo.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Custom color palette
private val PrimaryBlue = Color(0xFF012460)
private val PrimaryBlueLight = Color(0xFF1E3A8A)
private val PrimaryBlueDark = Color(0xFF001845)
private val SecondaryGreen = Color(0xFF4E9349)
private val SecondaryGreenLight = Color(0xFF68B85C)
private val SecondaryGreenDark = Color(0xFF3A7235)

val LightColorScheme =
  lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = Color.White,
    secondary = SecondaryGreen,
    onSecondary = Color.White,
    secondaryContainer = SecondaryGreenLight,
    onSecondaryContainer = Color.White,
    tertiary = SecondaryGreenDark,
    onTertiary = Color.White,
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
  )

val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = Color.White,
    secondary = SecondaryGreenLight,
    onSecondary = Color.White,
    secondaryContainer = SecondaryGreenDark,
    onSecondaryContainer = Color.White,
    tertiary = SecondaryGreen,
    onTertiary = Color.White,
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
  )
