plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.spotless)
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            ktlint(rootProject.libs.versions.ktlint.get()).editorConfigOverride(
                mapOf(
                    "ktlint_standard_function-naming" to "disabled",
                    "ktlint_standard_filename" to "disabled"
                )
            )
            ktfmt().googleStyle()
            trimTrailingWhitespace()
            licenseHeaderFile("LICENSE.txt",)
            endWithNewline()
        }

        kotlinGradle {
            target("*.gradle.kts")
            ktlint(rootProject.libs.versions.ktlint.get())
            ktfmt().googleStyle()
        }

        format("xml") {
            target("**/*.xml")
            leadingTabsToSpaces(2)
            trimTrailingWhitespace()
            endWithNewline()
        }

        format("json") {
            target("**/*.json")
            leadingTabsToSpaces(2)
            trimTrailingWhitespace()
        }
    }
}
