package io.litequest.i18n

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TranslationCacheTest {
  @Test
  fun testPutAndGet() {
    val cache = TranslationCache()
    val translations = mapOf("key1" to "value1", "key2" to "value2")

    cache.put("en", translations)
    val result = cache.get("en")

    assertEquals(translations, result)
  }

  @Test
  fun testGetNonExistent() {
    val cache = TranslationCache()

    val result = cache.get("es")

    assertNull(result)
  }

  @Test
  fun testContains() {
    val cache = TranslationCache()
    val translations = mapOf("key1" to "value1")

    cache.put("en", translations)

    assertTrue(cache.contains("en"))
    assertFalse(cache.contains("es"))
  }

  @Test
  fun testClear() {
    val cache = TranslationCache()
    cache.put("en", mapOf("key1" to "value1"))
    cache.put("es", mapOf("key1" to "valor1"))

    cache.clear()

    assertFalse(cache.contains("en"))
    assertFalse(cache.contains("es"))
  }

  @Test
  fun testOverwriteExisting() {
    val cache = TranslationCache()
    cache.put("en", mapOf("key1" to "old"))
    cache.put("en", mapOf("key1" to "new"))

    val result = cache.get("en")

    assertEquals(mapOf("key1" to "new"), result)
  }

  @Test
  fun testMultipleLocales() {
    val cache = TranslationCache()
    cache.put("en", mapOf("greeting" to "Hello"))
    cache.put("es", mapOf("greeting" to "Hola"))
    cache.put("fr", mapOf("greeting" to "Bonjour"))

    assertEquals("Hello", cache.get("en")!!["greeting"])
    assertEquals("Hola", cache.get("es")!!["greeting"])
    assertEquals("Bonjour", cache.get("fr")!!["greeting"])
  }
}
