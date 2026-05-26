import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("spotless-conventions")
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

dependencies {
  implementation(projects.demo.shared)
  implementation(projects.library)
  implementation(libs.androidx.activity.compose)
}

android {
  namespace = "io.litequest.demo"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "io.litequest.demo"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0.0"
  }

  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }

  buildTypes { getByName("release") { isMinifyEnabled = false } }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}
