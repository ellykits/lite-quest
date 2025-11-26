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
package io.litequest.ui.pagination

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PageNavigator(val pages: List<QuestionnairePage>) {
  private val _currentPageIndex = MutableStateFlow(0)
  val currentPageIndex: StateFlow<Int> = _currentPageIndex.asStateFlow()

  val currentPage: QuestionnairePage
    get() = pages[_currentPageIndex.value]

  fun canGoNext(): Boolean {
    return _currentPageIndex.value < pages.size - 1
  }

  fun canGoPrevious(): Boolean = _currentPageIndex.value > 0

  fun goNext() {
    if (_currentPageIndex.value < pages.size - 1) {
      _currentPageIndex.value++
    }
  }

  fun goPrevious() {
    if (_currentPageIndex.value > 0) {
      _currentPageIndex.value--
    }
  }

  fun isLastPage(): Boolean = _currentPageIndex.value == pages.size - 1

  fun isFirstPage(): Boolean = _currentPageIndex.value == 0

  fun goToPage(index: Int) {
    if (index in pages.indices) {
      _currentPageIndex.value = index
    }
  }
}
