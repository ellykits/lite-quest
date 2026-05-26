plugins {
  id("spotless-conventions")
  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.kotlinSerialization) apply false
  alias(libs.plugins.kotlinJvm) apply false
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.androidMultiplatformLibrary) apply false
  alias(libs.plugins.composeMultiplatform) apply false
  alias(libs.plugins.composeCompiler) apply false
  // Declared here so all subprojects share one classloader scope for Spotless's build service
  alias(libs.plugins.spotless) apply false
}
