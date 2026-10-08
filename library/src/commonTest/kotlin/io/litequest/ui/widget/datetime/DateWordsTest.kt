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
package io.litequest.ui.widget.datetime

import kotlin.test.Test
import kotlin.test.assertEquals

class DateWordsTest {
  @Test
  fun singleDigitDayHasNoLeadingZero() {
    assertEquals("Thu 8 Oct 2026", formatDateWords("2026-10-08"))
  }

  @Test
  fun weekdaysAndMonths() {
    assertEquals("Thu 1 Jan 2026", formatDateWords("2026-01-01"))
    assertEquals("Sun 27 Dec 2026", formatDateWords("2026-12-27"))
    assertEquals("Sun 29 Feb 2088", formatDateWords("2088-02-29"))
  }

  @Test
  fun dateTimeKeepsTime() {
    assertEquals("Thu 8 Oct 2026 14:05", formatDateTimeWords("2026-10-08T14:05:00"))
    assertEquals("Thu 8 Oct 2026 09:00", formatDateTimeWords("2026-10-08T09:00:00Z"))
  }

  @Test
  fun unparseableValueIsReturnedAsIs() {
    assertEquals("soon", formatDateWords("soon"))
  }
}
