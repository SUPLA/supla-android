package org.supla.android.features.details.windowdetail.base.ui.windowview.controls
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemAction
import org.supla.android.ui.views.buttons.supla.DownControlIcon
import org.supla.android.ui.views.buttons.supla.UpControlIcon
import org.supla.android.ui.views.buttons.supla.UpDownControlButton

@Composable
fun HoldToMoveVerticalButtons(enabled: Boolean, modifier: Modifier = Modifier, onAction: (ShadingSystemAction) -> Unit) =
  UpDownControlButton(
    disabled = !enabled,
    upContent = { UpControlIcon(textColor = it) },
    downContent = { DownControlIcon(textColor = it) },
    upEventHandler = {
      handleEvents(
        onTouchDown = { onAction(ShadingSystemAction.MoveUp) },
        onTouchUp = { onAction(ShadingSystemAction.Stop) }
      )
    },
    downEventHandler = {
      handleEvents(
        onTouchDown = { onAction(ShadingSystemAction.MoveDown) },
        onTouchUp = { onAction(ShadingSystemAction.Stop) }
      )
    },
    modifier = modifier
  )

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .padding(dimensionResource(id = R.dimen.distance_default))
    ) {
      HoldToMoveVerticalButtons(enabled = true) {}
      HoldToMoveVerticalButtons(enabled = false) {}
    }
  }
}
