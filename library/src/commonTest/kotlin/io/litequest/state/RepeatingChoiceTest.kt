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

import io.litequest.engine.LiteQuestEvaluator
import io.litequest.model.AnswerOption
import io.litequest.model.Item
import io.litequest.model.ItemType
import io.litequest.model.Questionnaire
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

class RepeatingChoiceTest {

  @Test
  fun aMultiSelectChoiceKeepsEveryTick() {
    val questionnaire =
      Questionnaire(
        id = "tax",
        version = "1.0",
        title = "Tax",
        items =
          listOf(
            Item(
              linkId = "taxCodes",
              type = ItemType.CHOICE,
              text = "Tax codes",
              repeats = true,
              answerOptions = listOf(AnswerOption("vat", "VAT 16%"), AnswerOption("levy", "Levy 2%")),
            )
          ),
      )
    val manager = QuestionnaireManager(questionnaire, LiteQuestEvaluator(questionnaire))
    val ticked = JsonArray(listOf(JsonPrimitive("vat"), JsonPrimitive("levy")))

    manager.updateAnswer("taxCodes", ticked)

    val answer = manager.state.value.response.items.single { it.linkId == "taxCodes" }.answers
    assertEquals(listOf(ticked), answer.map { it.value })
  }
}
