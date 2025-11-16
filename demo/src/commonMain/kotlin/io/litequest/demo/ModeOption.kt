package io.litequest.demo

import androidx.compose.ui.graphics.vector.ImageVector

data class ModeOption(
  val id: String,
  val title: String,
  val description: String,
  val icon: ImageVector,
  val isPrimary: Boolean = false,
)
