@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("spotless-conventions")
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.androidMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.maven.publish)
  signing
}

group = "io.github.ellykits.litequest"

version = "1.0.0-beta02"

kotlin {
  applyDefaultHierarchyTemplate()

  androidLibrary {
    namespace = "io.github.ellykits.litequest.library"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    compilerOptions { jvmTarget = JvmTarget.JVM_11 }
    androidResources { enable = true }
  }

  jvm("desktop") { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }

  wasmJs {
    browser()
    binaries.library()
  }

  js {
    browser()
    binaries.library()
  }

  listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "LiteQuest"
      isStatic = true
    }
  }

  sourceSets {
    commonMain.dependencies {
      api(libs.kotlinx.serialization.json)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.kotlinx.datetime)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.content.negotiation)
      implementation(libs.ktor.serialization.kotlinx.json)

      implementation(libs.foundation)
      implementation(libs.material3)
      implementation(libs.components.resources)

      implementation(libs.material.icons.core)
      implementation(libs.lucide)
      implementation(libs.filekit.core)
      implementation(libs.filekit.compose)
      implementation(libs.coil.compose)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.kotlinx.coroutines.test)
    }

    androidMain.dependencies {
      implementation(libs.kscan)
      implementation(libs.ktor.client.android)
      implementation(libs.androidx.activity.compose)
      implementation(libs.androidx.core.ktx)
    }

    val desktopMain by getting {
      dependencies {
        implementation(libs.kscan)
        implementation(libs.ktor.client.cio)
      }
    }

    iosMain.dependencies {
      implementation(libs.kscan)
      implementation(libs.ktor.client.darwin)
    }

    jsMain.dependencies { implementation(libs.ktor.client.js) }

    wasmJsMain.dependencies { implementation(libs.kscan) }
  }
}

signing { isRequired = true }

gradle.taskGraph.whenReady {
  if (allTasks.any { it.name.contains("MavenLocal", ignoreCase = true) }) {
    tasks.withType<Sign>().configureEach { isEnabled = false }
  }
}

mavenPublishing {
  publishToMavenCentral()

  signAllPublications()

  coordinates(group.toString(), "litequest-library", version.toString())

  pom {
    name = "Lite Quest"
    description =
      "A lightweight, FHIR-inspired questionnaire library for Kotlin Multiplatform applications."
    inceptionYear = "2025"
    url = "https://github.com/ellykits/lite-quest"
    licenses {
      license {
        name.set("The Apache License, Version 2.0")
        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
        distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }
    }
    developers {
      developer {
        id.set("ellykits")
        name.set("Elly Kitoto")
        url.set("https://github.com/ellykits")
      }
    }
    scm {
      url.set("https://github.com/ellykits/lite-quest")
      connection.set("scm:git:git://github.com/ellykits/lite-quest.git")
      developerConnection.set("scm:git:ssh://git@github.com/ellykits/lite-quest.git")
    }
  }
}
