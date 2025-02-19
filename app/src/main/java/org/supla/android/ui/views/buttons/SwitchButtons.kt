package org.supla.android.ui.views.buttons
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.extensions.disabledOverlay
import org.supla.android.images.ImageId
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.buttons.supla.SuplaButton
import org.supla.android.ui.views.buttons.supla.SuplaButtonColors
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults

data class SwitchButtonState(
  val icon: ImageId?,
  val textRes: Int,
  val pressed: Boolean
)

@Composable
fun SwitchButtons(
  leftButton: SwitchButtonState?,
  rightButton: SwitchButtonState?,
  disabled: Boolean = false,
  leftButtonClick: () -> Unit = {},
  rightButtonClick: () -> Unit = {}
) =
  Row(
    horizontalArrangement = Arrangement.spacedBy(Distance.default),
    modifier = Modifier.padding(all = Distance.default)
  ) {
    leftButton?.let {
      Button(
        icon = it.icon,
        text = stringResource(id = it.textRes),
        colors = SuplaButtonDefaults.errorColors(),
        disabled = disabled,
        pressed = it.pressed,
        onClick = leftButtonClick,
        modifier = Modifier.weight(1f)
      )
    }
    rightButton?.let {
      Button(
        icon = it.icon,
        text = stringResource(id = it.textRes),
        colors = SuplaButtonDefaults.primaryColors(),
        disabled = disabled,
        pressed = it.pressed,
        onClick = rightButtonClick,
        modifier = Modifier.weight(1f)
      )
    }
  }

@Composable
private fun Button(
  text: String,
  icon: ImageId?,
  modifier: Modifier = Modifier,
  disabled: Boolean = false,
  pressed: Boolean = false,
  colors: SuplaButtonColors = SuplaButtonDefaults.buttonColors(),
  onClick: () -> Unit
) {
  val shape = SuplaButtonDefaults.allRoundedShape()
  SuplaButton(
    onClick = onClick,
    modifier = modifier.disabledOverlay(disabled, radius = shape.topEndRadius),
    disabled = disabled,
    active = pressed,
    colors = colors,
    shape = shape
  ) {
    Row(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = Distance.small),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
    ) {
      icon?.let {
        Image(
          imageId = it,
          contentDescription = null,
          alignment = Alignment.Center,
          modifier = Modifier.size(dimensionResource(id = R.dimen.icon_default_size)),
        )
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = it,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
      )
    }
  }
}
