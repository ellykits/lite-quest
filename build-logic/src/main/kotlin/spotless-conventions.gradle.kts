plugins {
    id("com.diffplug.spotless")
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint("1.0.1").editorConfigOverride(
            mapOf(
                "ktlint_standard_function-naming" to "disabled",
                "ktlint_standard_filename" to "disabled",
            )
        )
        ktfmt().googleStyle()
        trimTrailingWhitespace()
        licenseHeaderFile(rootProject.file("LICENSE"))
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("1.0.1")
        ktfmt().googleStyle()
    }
    format("xml") {
        target("**/*.xml")
        targetExclude("**/build/**/*.xml")
        leadingTabsToSpaces(2)
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("json") {
        target("**/*.json")
        targetExclude("**/build/**/*.json")
        leadingTabsToSpaces(2)
        trimTrailingWhitespace()
    }
}
