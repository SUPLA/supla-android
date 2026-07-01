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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.suplaCard
import org.supla.android.main.topbar.Icon
import org.supla.android.main.topbar.LocalTopBarController
import org.supla.android.main.topbar.TopBarController
import org.supla.android.main.topbar.TopBarIcon
import org.supla.android.main.topbar.TopBarState
import org.supla.android.tools.SuplaComponentPreview
import org.supla.android.ui.views.buttons.DrawerBackButton
import org.supla.android.ui.views.texts.HeadlineSmall
import org.supla.core.shared.infrastructure.localizedString

@Composable
fun StandardTopBar(
  onBackClick: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(horizontal = Distance.default, vertical = Distance.small)
      .suplaCard(),
    shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default)),
    color = MaterialTheme.colorScheme.surface
  ) {
    Box(
      modifier = Modifier,
    ) {
      val topBarController = LocalTopBarController.current
      val topBarState by topBarController.state.collectAsStateWithLifecycle()

      DrawerBackButton(
        onClick = onBackClick,
        modifier = Modifier.align(Alignment.CenterStart)
      )

      HeadlineSmall(
        text = topBarState.title(),
        modifier = Modifier
          .align(Alignment.Center)
          .padding(horizontal = 64.dp),
        maxLines = 1
      )

      topBarState.icons.forEach { icon ->
        icon.Icon(topBarController, modifier = Modifier.align(Alignment.CenterEnd))
      }
    }
  }
}

@SuplaComponentPreview
@Composable
private fun Preview() {
  val topBarController = remember {
    TopBarController(
      initialState = TopBarState(
        icons = listOf(TopBarIcon.OpenSettings),
        title = localizedString(R.string.app_name)
      )
    )
  }
  CompositionLocalProvider(LocalTopBarController provides topBarController) {
    SuplaTheme {
      Box(Modifier.background(MaterialTheme.colorScheme.outline)) {
        StandardTopBar(
          onBackClick = {},
        )
      }
    }
  }
}
