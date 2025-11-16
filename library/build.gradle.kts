plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

group = "io.litequest"

version = "1.0.0"

kotlin {
  androidTarget { compilations.all { kotlinOptions { jvmTarget = "11" } } }

  jvm("desktop") { compilations.all { kotlinOptions { jvmTarget = "11" } } }

  wasmJs { browser() }

  listOf(
      iosX64(),
      iosArm64(),
      iosSimulatorArm64(),
    )
    .forEach { iosTarget ->
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
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
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
  namespace = "io.litequest"
  compileSdk = 34

  defaultConfig { minSdk = 24 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}
