package io.litequest.i18n

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private class DesktopTranslationLoader : TranslationLoader {
  private val client =
    HttpClient(CIO) {
      install(ContentNegotiation) {
        json(
          Json {
            ignoreUnknownKeys = true
            isLenient = true
          },
        )
      }
    }

  override suspend fun load(url: String): Map<String, String> {
    return client.get(url).body()
  }
}

actual fun createTranslationLoader(): TranslationLoader = DesktopTranslationLoader()
