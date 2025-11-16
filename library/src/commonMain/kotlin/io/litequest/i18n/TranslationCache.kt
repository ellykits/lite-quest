package io.litequest.i18n

class TranslationCache {
  private val cache = mutableMapOf<String, Map<String, String>>()

  fun get(locale: String): Map<String, String>? {
    return cache[locale]
  }

  fun put(
    locale: String,
    translations: Map<String, String>,
  ) {
    cache[locale] = translations
  }

  fun clear() {
    cache.clear()
  }

  fun contains(locale: String): Boolean {
    return cache.containsKey(locale)
  }
}
