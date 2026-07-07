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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.topbar.Icon
import org.supla.android.main.topbar.LocalTopBarController
import org.supla.android.main.topbar.TopBarAction
import org.supla.android.main.topbar.TopBarController
import org.supla.android.main.topbar.TopBarIcon
import org.supla.android.main.topbar.TopBarSearchState
import org.supla.android.main.topbar.TopBarState
import org.supla.android.tools.SuplaComponentPreview
import org.supla.android.ui.views.buttons.DrawerBackButton
import org.supla.android.ui.views.texts.HeadlineSmall
import org.supla.core.shared.infrastructure.localizedString

@Composable
fun StandardTopBar(navigator: MainComposeNavigator) {
  TopBarSurface {
    val topBarController = LocalTopBarController.current
    val topBarState = topBarController.state

    topBarState.search?.let {
      SearchContent(navigator, it, topBarState, topBarController)
    } ?: TitleContent(navigator, topBarState)
  }
}

@Composable
private fun SearchContent(
  navigator: MainComposeNavigator,
  searchState: TopBarSearchState,
  topBarState: TopBarState,
  topBarController: TopBarController
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    DrawerBackButton(
      onClick = {
        if (searchState.query.isEmpty()) {
          navigator.back()
        } else {
          topBarController.updateSearchValue("")
        }
      }
    )

    TopBarSearchField(
      searchText = searchState.query,
      onTextChange = { topBarController.updateSearchValue(it) },
      modifier = Modifier.weight(1f)
    )

    topBarState.action?.Icon()
  }
}

@Composable
private fun TitleContent(
  navigator: MainComposeNavigator,
  topBarState: TopBarState
) {
  Box {
    DrawerBackButton(
      onClick = { navigator.back() },
      modifier = Modifier.align(Alignment.CenterStart)
    )

    HeadlineSmall(
      text = topBarState.title(),
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = 64.dp),
      maxLines = 1
    )

    topBarState.action?.Icon(modifier = Modifier.align(Alignment.CenterEnd))
  }
}

@SuplaComponentPreview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(Modifier.background(MaterialTheme.colorScheme.outline)) {
      val titleTopBarController = remember {
        TopBarController(
          initialState = TopBarState(
            title = localizedString(R.string.app_name),
            action = TopBarAction(TopBarIcon.OpenSettings)
          )
        )
      }
      CompositionLocalProvider(LocalTopBarController provides titleTopBarController) {
        StandardTopBar(MainComposeNavigator(LocalContext.current))
      }

      val searchTopBarController = remember {
        TopBarController(
          initialState = TopBarState(
            search = TopBarSearchState(
              query = "",
              onQueryChange = {}
            ),
            action = TopBarAction(TopBarIcon.OpenSettings)
          )
        )
      }
      CompositionLocalProvider(LocalTopBarController provides searchTopBarController) {
        StandardTopBar(MainComposeNavigator(LocalContext.current))
      }
    }
  }
}
