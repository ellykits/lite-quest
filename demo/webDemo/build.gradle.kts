@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  id("spotless-conventions")
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

kotlin {
  wasmJs {
    browser { commonWebpackConfig { outputFileName = "liteQuestDemo.js" } }
    binaries.executable()
  }

  js {
    browser { commonWebpackConfig { outputFileName = "liteQuestDemo.js" } }
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.demo.shared)
      implementation(libs.components.resources)
      implementation(compose.ui)
      implementation(compose.foundation)
    }
  }
}
