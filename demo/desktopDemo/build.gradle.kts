import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  id("spotless-conventions")
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

kotlin {
  dependencies {
    implementation(projects.demo.shared)
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
  }
}

compose.desktop {
  application {
    mainClass = "io.litequest.demo.MainKt"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "io.litequest.demo"
      packageVersion = "1.0.0"
    }
  }
}

tasks.register("desktopRun") { dependsOn("run") }
