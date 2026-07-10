package org.supla.android.main.view
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.main.LocalNavigator
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.topbar.Icon
import org.supla.android.main.topbar.LocalTopBarController
import org.supla.android.main.topbar.MockedTopBarController
import org.supla.android.main.topbar.NavigationType
import org.supla.android.main.topbar.TopBarAction
import org.supla.android.main.topbar.TopBarController
import org.supla.android.main.topbar.TopBarIcon
import org.supla.android.main.topbar.TopBarSearchData
import org.supla.android.main.topbar.TopBarSearchState
import org.supla.android.main.topbar.TopBarState
import org.supla.android.tools.SuplaComponentPreview
import org.supla.android.ui.views.buttons.DrawerBackButton
import org.supla.android.ui.views.buttons.DrawerMenuButton
import org.supla.android.ui.views.buttons.IconButton
import org.supla.android.ui.views.texts.HeadlineSmall
import org.supla.core.shared.infrastructure.localizedString

@Composable
fun StandardTopBar() {
  TopBarSurface {
    val topBarController = LocalTopBarController.current
    val topBarState = topBarController.state
    val searchState = topBarState.search

    if (searchState != null && searchState.data.visible) {
      SearchContent(searchState, topBarState, topBarController)
    } else {
      TitleContent(topBarState, topBarController)
    }
  }
}

@Composable
private fun SearchContent(
  searchState: TopBarSearchState,
  topBarState: TopBarState,
  topBarController: TopBarController
) {
  val navigator = LocalNavigator.current
  Row(verticalAlignment = Alignment.CenterVertically) {
    NavigatorIcon(
      navigator = navigator,
      topBarState = topBarState,
      onSearchValue = topBarController::updateSearchValue
    )

    TopBarSearchField(
      searchText = searchState.data.query,
      modifier = Modifier.weight(1f)
    )

    topBarState.action?.Icon()
  }
}

@Composable
private fun TitleContent(
  topBarState: TopBarState,
  topBarController: TopBarController
) {
  val navigator = LocalNavigator.current
  Box {
    NavigatorIcon(
      navigator = navigator,
      topBarState = topBarState,
      onSearchValue = topBarController::updateSearchValue,
      modifier = Modifier.align(Alignment.CenterStart)
    )

    HeadlineSmall(
      text = topBarState.title(),
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = if (topBarState.search == null) 48.dp else 88.dp),
      maxLines = 1
    )

    topBarState.search?.let {
      IconButton(
        R.drawable.ic_search,
        onClick = { topBarController.setSearchVisible(true) },
        contentDescription = stringResource(R.string.general_search),
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .padding(end = if (topBarState.action == null) 0.dp else 40.dp)
          .size(40.dp)
      )
    }

    topBarState.action?.Icon(modifier = Modifier.align(Alignment.CenterEnd))
  }
}

@Composable
private fun NavigatorIcon(
  navigator: MainComposeNavigator?,
  topBarState: TopBarState,
  onSearchValue: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  when (topBarState.navigationType) {
    NavigationType.DRAWER -> {
      val scope = rememberCoroutineScope()
      val drawerState = LocalDrawerState.current
      DrawerMenuButton(
        onClick = { drawerState?.let { scope.launch { it.open() } } },
        modifier = modifier
      )
    }
    NavigationType.BACK ->
      DrawerBackButton(
        onClick = {
          if (topBarState.inSearch) {
            onSearchValue("")
          } else {
            navigator?.back()
          }
        },
        modifier = modifier
      )
    NavigationType.NONE -> {}
  }
}

@SuplaComponentPreview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(Modifier.background(MaterialTheme.colorScheme.outline)) {
      MockedTopBarController(
        title = localizedString(R.string.app_name),
        action = TopBarAction(TopBarIcon.OpenSettings)
      ) {
        StandardTopBar()
      }

      MockedTopBarController(
        title = localizedString(R.string.app_name),
        search = TopBarSearchState(
          data = TopBarSearchData(),
          observer = {}
        ),
        action = TopBarAction(TopBarIcon.OpenSettings)
      ) {
        StandardTopBar()
      }

      MockedTopBarController(
        search = TopBarSearchState(
          data = TopBarSearchData(visible = true),
          observer = {}
        ),
        action = TopBarAction(TopBarIcon.OpenSettings)
      ) {
        StandardTopBar()
      }

      MockedTopBarController(
        search = TopBarSearchState(
          data = TopBarSearchData(),
          observer = {}
        )
      ) {
        StandardTopBar()
      }

      MockedTopBarController(
        search = TopBarSearchState(
          data = TopBarSearchData(visible = true),
          observer = {}
        )
      ) {
        StandardTopBar()
      }
    }
  }
}
