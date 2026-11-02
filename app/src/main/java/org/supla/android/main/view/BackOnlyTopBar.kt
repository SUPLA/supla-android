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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaComponentPreview
import org.supla.android.ui.views.buttons.DrawerBackButton
import org.supla.android.ui.views.texts.HeadlineSmall

@Composable
fun BackOnlyTopBar(
  onBackClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.primaryContainer)
      .statusBarsPadding()
      .padding(horizontal = Distance.default, vertical = Distance.small),
  ) {
    DrawerBackButton(
      onClick = onBackClick,
      modifier = Modifier.align(Alignment.CenterStart)
    )

    HeadlineSmall(
      text = stringResource(R.string.app_name),
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = 64.dp),
      maxLines = 1,
      color = MaterialTheme.colorScheme.onPrimaryContainer
    )
  }
}

@SuplaComponentPreview
@Composable
private fun Preview() {
  SuplaTheme {
    Box(Modifier.background(MaterialTheme.colorScheme.outline)) {
      BackOnlyTopBar(
        onBackClick = {}
      )
    }
  }
}
