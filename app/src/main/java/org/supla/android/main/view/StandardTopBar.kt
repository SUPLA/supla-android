@file:OptIn(ExperimentalMaterial3Api::class)

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

import android.view.Surface
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.main.LocalNavigator
import org.supla.android.main.topbar.Icon
import org.supla.android.main.topbar.LocalTopBarController
import org.supla.android.main.topbar.MockedTopBarController
import org.supla.android.main.topbar.NavigationType
import org.supla.android.main.topbar.TopBarAction
import org.supla.android.main.topbar.TopBarIcon
import org.supla.android.main.topbar.TopBarSearchData
import org.supla.android.main.topbar.TopBarSearchState
import org.supla.android.tools.SuplaComponentPreview
import org.supla.android.ui.views.buttons.DrawerBackButton
import org.supla.android.ui.views.buttons.DrawerMenuButton
import org.supla.android.ui.views.buttons.IconButton
import org.supla.android.ui.views.texts.HeadlineSmall
import org.supla.core.shared.infrastructure.localizedString

@Composable
fun StandardTopBar(
  useNavigationBarPadding: Boolean,
  scrollBehavior: TopAppBarScrollBehavior? = null
) {
  val rotation = LocalView.current.display?.rotation
  val navigationBarInsets = WindowInsets.navigationBars
  val cameraInsets = WindowInsets.displayCutout
  val insets =
    when {
      (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && useNavigationBarPadding ->
        navigationBarInsets.add(cameraInsets)
      rotation == Surface.ROTATION_90 -> cameraInsets
      rotation == Surface.ROTATION_270 -> navigationBarInsets
      else -> null
    }
  val modifier = insets?.let { Modifier.windowInsetsPadding(it) } ?: Modifier

  val topBarController = LocalTopBarController.current
  val topBarState = topBarController.state
  val searchState = topBarState.search
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(searchState?.data?.visible) {
    if (searchState?.data?.visible == true) {
      focusRequester.requestFocus()
    }
  }

  CenterAlignedTopAppBar(
    modifier = modifier,
    expandedHeight = dimensionResource(R.dimen.top_bar_height),
    navigationIcon = {
      NavigatorIcon()
    },
    title = {
      if (searchState != null && searchState.data.visible) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          TopBarSearchField(
            searchText = searchState.data.query,
            selectionResetKey = searchState.observer,
            modifier = Modifier
              .weight(1f)
              .focusRequester(focusRequester)
          )
        }
      } else {
        HeadlineSmall(
          text = topBarState.title(),
          maxLines = 1,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.clickable(
            enabled = searchState != null,
            onClick = { topBarController.setSearchVisible(true) }
          )
        )
      }
    },
    actions = {
      if (searchState != null && !searchState.data.visible) {
        IconButton(
          R.drawable.ic_search,
          onClick = { topBarController.setSearchVisible(true) },
          contentDescription = stringResource(R.string.general_search),
          modifier = Modifier
            .size(40.dp),
          tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }
      topBarState.action?.Icon()
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer
    ),
    scrollBehavior = scrollBehavior
  )
}

@Composable
private fun NavigatorIcon(
  modifier: Modifier = Modifier
) {
  val topBarController = LocalTopBarController.current
  val topBarState = topBarController.state
  val navigator = LocalNavigator.current
  when {
    topBarController.searchActive ->
      DrawerBackButton(
        onClick = { topBarController.setSearchVisible(false) },
        modifier = modifier
      )
    topBarState.navigationType == NavigationType.DRAWER -> {
      val scope = rememberCoroutineScope()
      val drawerState = LocalDrawerState.current
      DrawerMenuButton(
        onClick = { drawerState?.let { scope.launch { it.open() } } },
        modifier = modifier
      )
    }
    topBarState.navigationType == NavigationType.BACK ->
      DrawerBackButton(
        onClick = {
          if (topBarState.inSearch) {
            topBarController.updateSearchValue("")
          } else {
            navigator?.back()
          }
        },
        modifier = modifier
      )
    topBarState.navigationType == NavigationType.NONE -> {}
  }
}

@SuplaComponentPreview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(
      modifier = Modifier.background(MaterialTheme.colorScheme.outline),
      verticalArrangement = Arrangement.spacedBy(Distance.tiny)
    ) {
      MockedTopBarController(
        title = localizedString(R.string.app_name),
        action = TopBarAction(TopBarIcon.OpenSettings)
      ) {
        StandardTopBar(false)
      }

      MockedTopBarController(
        title = localizedString(R.string.app_name),
        search = TopBarSearchState(
          data = TopBarSearchData(),
          observer = {}
        ),
        action = TopBarAction(TopBarIcon.OpenSettings)
      ) {
        StandardTopBar(false)
      }

      MockedTopBarController(
        search = TopBarSearchState(
          data = TopBarSearchData(visible = true),
          observer = {}
        ),
        action = TopBarAction(TopBarIcon.OpenSettings)
      ) {
        StandardTopBar(false)
      }

      MockedTopBarController(
        search = TopBarSearchState(
          data = TopBarSearchData(),
          observer = {}
        )
      ) {
        StandardTopBar(false)
      }

      MockedTopBarController(
        search = TopBarSearchState(
          data = TopBarSearchData(visible = true),
          observer = {}
        )
      ) {
        StandardTopBar(false)
      }
    }
  }
}
