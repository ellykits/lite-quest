@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.maven.publish)
}

group = "io.litequest"

version = "1.0.0-alpha01"

kotlin {
  androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }

  jvm("desktop") { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }

  wasmJs {
    browser()
    binaries.library()
  }

  listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "LiteQuest"
      isStatic = true
    }
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.kotlinx.serialization.json)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.datetime)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.serialization.kotlinx.json)

        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material3)
        implementation(compose.ui)

        implementation(libs.material.icons.core)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
      }
    }

    val androidMain by getting { dependencies { implementation(libs.ktor.client.android) } }

    val desktopMain by getting { dependencies { implementation(libs.ktor.client.cio) } }

    val wasmJsMain by getting { dependencies { implementation(libs.ktor.client.js) } }

    val iosX64Main by getting
    val iosArm64Main by getting
    val iosSimulatorArm64Main by getting
    val iosMain by creating {
      dependsOn(commonMain)
      iosX64Main.dependsOn(this)
      iosArm64Main.dependsOn(this)
      iosSimulatorArm64Main.dependsOn(this)

      dependencies { implementation(libs.ktor.client.darwin) }
    }
  }
}

android {
  namespace = "io.litequest.library"
  compileSdk = 36

  defaultConfig { minSdk = 24 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

// Prepare for publishing
val localRepo: Directory = project.layout.buildDirectory.get().dir("liteQuestRepo")

publishing {
  repositories { maven { url = localRepo.asFile.toURI() } }
  publications {
    withType<MavenPublication> {
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
      }
    }
  }
}

val deleteRepoTask =
  tasks.register<Delete>("deleteLocalRepo") {
    description =
      "Deletes the local repository to get rid of stale artifacts before local publishing"
    this.delete(localRepo)
  }

tasks.named("publishAllPublicationsToMavenRepository").configure { dependsOn(deleteRepoTask) }

tasks.register("zipRepo", Zip::class) {
  description = "Create a zip of the maven repository"
  this.destinationDirectory.set(project.layout.buildDirectory.dir("repoZip"))
  archiveBaseName.set("lite-quest")

  // Hint to gradle that the repo files are produced by the task named publish.
  // This establishes a dependency from the zipRepo task to the task named publish.
  this.from(tasks.named("publish").map { _ -> localRepo })
}
