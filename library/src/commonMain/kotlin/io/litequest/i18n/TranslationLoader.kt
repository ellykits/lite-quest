package io.litequest.i18n

interface TranslationLoader {
  suspend fun load(url: String): Map<String, String>
}

expect fun createTranslationLoader(): TranslationLoader
