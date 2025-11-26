/*
* Copyright 2025 LiteQuest Contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.litequest.i18n

import io.litequest.model.Translations

class TranslationManager(
  private val translations: Translations,
  private val loader: TranslationLoader = createTranslationLoader(),
) {
  private val cache = TranslationCache()
  private var currentLocale: String = translations.defaultLocale

  suspend fun loadLocale(locale: String): Result<Map<String, String>> {
    if (cache.contains(locale)) {
      return Result.success(cache.get(locale)!!)
    }

    val url =
      translations.sources[locale]
        ?: return Result.failure(Exception("No translation source for locale: $locale"))

    return try {
      val translationMap = loader.load(url)
      cache.put(locale, translationMap)
      Result.success(translationMap)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun setLocale(locale: String): Result<Unit> {
    return loadLocale(locale).map { currentLocale = locale }
  }

  fun getCurrentLocale(): String = currentLocale

  suspend fun resolve(
    key: String,
    locale: String = currentLocale,
    useFallback: Boolean = true,
  ): String {
    val translationMap = cache.get(locale) ?: run { loadLocale(locale).getOrNull() ?: emptyMap() }

    val value = translationMap[key]
    if (value != null) return value

    if (useFallback && locale != translations.defaultLocale) {
      val fallbackMap =
        cache.get(translations.defaultLocale)
          ?: run { loadLocale(translations.defaultLocale).getOrNull() ?: emptyMap() }
      return fallbackMap[key] ?: key
    }

    return key
  }

  fun interpolate(template: String, values: Map<String, Any?>): String {
    var result = template
    values.forEach { (key, value) -> result = result.replace("{$key}", value?.toString() ?: "") }
    return result
  }

  suspend fun resolveAndInterpolate(
    key: String,
    values: Map<String, Any?>,
    locale: String = currentLocale,
  ): String {
    val template = resolve(key, locale)
    return interpolate(template, values)
  }
}
