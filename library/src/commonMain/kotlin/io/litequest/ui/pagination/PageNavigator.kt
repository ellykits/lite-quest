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
