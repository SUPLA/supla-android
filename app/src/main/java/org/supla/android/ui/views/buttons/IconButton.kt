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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import org.supla.android.R

@Composable
fun IconButton(
  icon: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  rotate: Boolean = false,
  tint: Color = MaterialTheme.colors.primary
) =
  IconButton(onClick = onClick, enabled = enabled, modifier = modifier) {
    val iconModifier = Modifier
      .padding(all = dimensionResource(id = R.dimen.distance_small))
      .size(dimensionResource(id = R.dimen.icon_default_size))

    Icon(
      painter = painterResource(id = icon),
      contentDescription = null,
      modifier = if (rotate) iconModifier.rotate(180f) else iconModifier,
      tint = if (enabled) tint else colorResource(id = R.color.disabled)
    )
  }
