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
package io.litequest.state

actual fun loadProjectFileText(path: String): String {
  val classLoader = Thread.currentThread().contextClassLoader
  val candidates =
    listOf(path, "composeResources/io.github.ellykits.litequest.library.generated.resources/$path")
  val stream =
    candidates.firstNotNullOfOrNull { candidate -> classLoader.getResourceAsStream(candidate) }
  requireNotNull(stream) { "Resource not found on test classpath: $path" }
  return stream.use { it.readBytes().decodeToString() }
}
