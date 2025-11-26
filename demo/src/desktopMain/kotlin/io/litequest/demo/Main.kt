package io.litequest.demo

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
  Window(
    onCloseRequest = ::exitApplication,
    title = "LiteQuest Demo",
    state = rememberWindowState(width = 800.dp, height = 900.dp),
  ) {
    App()
  }
}
