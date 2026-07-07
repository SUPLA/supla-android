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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.main.topbar.LocalTopBarController
import org.supla.android.tools.SuplaComponentPreview
import org.supla.android.ui.views.buttons.DrawerMenuButton
import org.supla.android.ui.views.buttons.IconButton

@Composable
fun MainTopBar(
  onMenuClick: (() -> Unit)?,
  onProfilesClick: () -> Unit,
) {
  val topBarController = LocalTopBarController.current
  val state = topBarController.state

  TopBarSurface {
    Row(verticalAlignment = Alignment.CenterVertically) {
      onMenuClick?.let {
        DrawerMenuButton(onClick = it)
      }

      TopBarSearchField(
        searchText = state.search?.query ?: "",
        onTextChange = { topBarController.updateSearchValue(it) },
        modifier = Modifier.weight(1f)
      )

      IconButton(
        R.drawable.ic_menu_profiles,
        onClick = onProfilesClick,
        contentDescription = stringResource(R.string.profile_plural)
      )
    }
  }
}

@SuplaComponentPreview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(Modifier.background(MaterialTheme.colorScheme.outline)) {
      MainTopBar(
        onMenuClick = {},
        onProfilesClick = {},
      )
      MainTopBar(
        onMenuClick = null,
        onProfilesClick = {},
      )
      MainTopBar(
        onMenuClick = null,
        onProfilesClick = {},
      )
    }
  }
}
