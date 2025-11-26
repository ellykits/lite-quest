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
package io.litequest.util

import io.litequest.model.QuestionnaireResponse
import io.litequest.model.Subject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PathResolverTest {
  @Test
  fun testResolveSimplePath() {
    val subject = Subject(id = "patient-123", type = "Patient")

    val result = PathResolver.resolve(subject, "id")

    assertEquals("patient-123", result)
  }

  @Test
  fun testResolveNestedPath() {
    val response =
      QuestionnaireResponse(
        id = "resp-1",
        questionnaireId = "q1",
        authored = "2025-11-07",
        subject = Subject(id = "patient-123", type = "Patient"),
        items = emptyList(),
      )

    val result = PathResolver.resolve(response, "subject.id")

    assertEquals("patient-123", result)
  }

  @Test
  fun testResolveInvalidPath() {
    val subject = Subject(id = "patient-123", type = "Patient")

    val result = PathResolver.resolve(subject, "nonexistent")

    assertNull(result)
  }

  @Test
  fun testResolveWithNullRoot() {
    val result = PathResolver.resolve(null, "some.path")

    assertNull(result)
  }

  @Test
  fun testResolveMapPath() {
    val data = mapOf("user" to mapOf("name" to "John", "age" to 25))

    val result = PathResolver.resolve(data, "user")

    assertEquals(mapOf("name" to "John", "age" to 25), result)
  }

  @Test
  fun testResolveResponseFields() {
    val response =
      QuestionnaireResponse(
        id = "resp-1",
        questionnaireId = "q1",
        authored = "2025-11-07T10:00:00Z",
        items = emptyList(),
      )

    assertEquals("resp-1", PathResolver.resolve(response, "id"))
    assertEquals("q1", PathResolver.resolve(response, "questionnaireId"))
    assertEquals("2025-11-07T10:00:00Z", PathResolver.resolve(response, "authored"))
  }
}
