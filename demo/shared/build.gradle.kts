@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("spotless-conventions")
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  applyDefaultHierarchyTemplate()

  androidLibrary {
    namespace = "io.litequest.demo.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    compilerOptions { jvmTarget = JvmTarget.JVM_11 }
    androidResources { enable = true }
  }

  jvm()

  listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "LiteQuestDemo"
      isStatic = true
    }
  }

  wasmJs {
    browser()
    binaries.library()
  }

  js {
    browser()
    binaries.library()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.library)
      implementation(libs.foundation)
      implementation(libs.material3)
      implementation(libs.components.resources)
      implementation(libs.ui.tooling.preview)
      implementation(libs.androidx.lifecycle.viewmodel.compose)
      implementation(libs.androidx.lifecycle.runtime.compose)
      implementation(libs.androidx.navigation.compose)
      implementation(libs.material.icons.core)
      implementation(libs.lucide)
    }
  }
}
