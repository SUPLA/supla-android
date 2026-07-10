package org.supla.android.main.topbar
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.supla.android.BuildConfig
import org.supla.core.shared.infrastructure.LocalizedString
import timber.log.Timber

class TopBarController(initialState: TopBarState = TopBarState()) {
  var state by mutableStateOf(initialState)
    private set

  private val _events = MutableSharedFlow<TopBarEvent>()
  val events = _events.asSharedFlow()

  private var owner: TopBarOwner? = null

  suspend fun emit(event: TopBarEvent) {
    _events.emit(event)
  }

  fun setTopBar(
    owner: TopBarOwner,
    state: TopBarState,
  ) {
    val current = this.owner

    if (current != null && current.instance !== owner.instance && current.screenKey == owner.screenKey) {
      if (BuildConfig.DEBUG) {
        error("Multiple ManageTopBar detected! Only one view model is allowed to manage the top bar!")
      } else {
        Timber.e("Multiple ManageTopBar detected!")
      }
    }

    this.owner = owner
    this.state = state
  }

  fun updateSearchValue(value: String) {
    state.search?.let { searchState ->
      state = state.copy(search = searchState.copy(data = searchState.data.copy(query = value)))
      searchState.observer(TopBarSearchEvent.QueryChange(value))
    }
  }

  fun updateTitle(title: LocalizedString) {
    state = state.copy(title = title)
  }

  fun setAction(action: TopBarAction?) {
    state = state.copy(action = action)
  }

  fun clear(owner: TopBarOwner) {
    if (this.owner?.instance == owner.instance) {
      this.owner = null
      state = TopBarState()
    }
  }

  fun setSearchVisible(visible: Boolean) {
    state.search?.let { searchState ->
      state = state.copy(search = searchState.copy(data = searchState.data.copy(visible = visible)))
      searchState.observer(TopBarSearchEvent.VisibilityChange(visible))
    }
  }
}

data class TopBarOwner(
  val screenKey: Any,
  val instance: Any = Any()
)

private val DefaultTopBarController = TopBarController()
val LocalTopBarController = staticCompositionLocalOf { DefaultTopBarController }
val LocalTopBarScreenKey = staticCompositionLocalOf<Any> { error("LocalTopBarScreenKey not provided!") }

/**
 * Prepared for previews.
 */
@Composable
fun MockedTopBarController(
  title: LocalizedString = LocalizedString.Empty,
  searchQuery: String = "",
  searchVisible: Boolean = false,
  action: TopBarAction? = null,
  content: @Composable () -> Unit
) {
  MockedTopBarController(
    title = title,
    search = TopBarSearchState(
      data = TopBarSearchData(searchQuery, searchVisible),
      observer = {}
    ),
    action = action,
    content = content
  )
}

@Composable
fun MockedTopBarController(
  title: LocalizedString = LocalizedString.Empty,
  search: TopBarSearchState? = null,
  action: TopBarAction? = null,
  content: @Composable () -> Unit
) {
  val topBarControllerWithSearch = TopBarController()
  topBarControllerWithSearch.setTopBar(
    owner = TopBarOwner(""),
    state = TopBarState(
      title = title,
      search = search,
      action = action
    )
  )

  CompositionLocalProvider(
    value = LocalTopBarController provides topBarControllerWithSearch,
    content = content
  )
}
