package org.supla.android.ui.views.buttons.supla
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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R

object SuplaButtonDefaults {
  @Composable
  fun radius() = dimensionResource(id = R.dimen.button_default_size).div(2)

  @Composable
  fun buttonColors(
    border: Color = MaterialTheme.colorScheme.outline,
    borderPressed: Color = MaterialTheme.colorScheme.primary,
    borderDisabled: Color = MaterialTheme.colorScheme.outlineVariant,
    content: Color = MaterialTheme.colorScheme.onBackground,
    contentPressed: Color = MaterialTheme.colorScheme.onBackground,
    contentDisabled: Color = MaterialTheme.colorScheme.outline,
    shadow: Color = DefaultShadowColor,
    shadowPressed: Color = MaterialTheme.colorScheme.primary
  ) = SuplaButtonColors(
    border = border,
    borderPressed = borderPressed,
    borderDisabled = borderDisabled,
    content = content,
    contentPressed = contentPressed,
    contentDisabled = contentDisabled,
    shadow = shadow,
    shadowPressed = shadowPressed
  )

  @Composable
  fun primaryColors(
    border: Color = MaterialTheme.colorScheme.outline,
    borderPressed: Color = MaterialTheme.colorScheme.primary,
    borderDisabled: Color = MaterialTheme.colorScheme.outlineVariant,
    content: Color = MaterialTheme.colorScheme.onBackground,
    contentPressed: Color = MaterialTheme.colorScheme.primary,
    contentDisabled: Color = MaterialTheme.colorScheme.outline,
    shadow: Color = DefaultShadowColor,
    shadowPressed: Color = MaterialTheme.colorScheme.primary
  ) = SuplaButtonColors(
    border = border,
    borderPressed = borderPressed,
    borderDisabled = borderDisabled,
    content = content,
    contentPressed = contentPressed,
    contentDisabled = contentDisabled,
    shadow = shadow,
    shadowPressed = shadowPressed
  )

  @Composable
  fun errorColors(
    border: Color = MaterialTheme.colorScheme.outline,
    borderPressed: Color = MaterialTheme.colorScheme.error,
    borderDisabled: Color = MaterialTheme.colorScheme.outlineVariant,
    content: Color = MaterialTheme.colorScheme.onBackground,
    contentPressed: Color = MaterialTheme.colorScheme.error,
    contentDisabled: Color = MaterialTheme.colorScheme.outline,
    shadow: Color = DefaultShadowColor,
    shadowPressed: Color = MaterialTheme.colorScheme.error
  ) = SuplaButtonColors(
    border = border,
    borderPressed = borderPressed,
    borderDisabled = borderDisabled,
    content = content,
    contentPressed = contentPressed,
    contentDisabled = contentDisabled,
    shadow = shadow,
    shadowPressed = shadowPressed
  )

  @Composable
  fun secondaryColors(
    border: Color = MaterialTheme.colorScheme.outline,
    borderPressed: Color = MaterialTheme.colorScheme.secondary,
    borderDisabled: Color = MaterialTheme.colorScheme.outlineVariant,
    content: Color = MaterialTheme.colorScheme.onBackground,
    contentPressed: Color = MaterialTheme.colorScheme.secondary,
    contentDisabled: Color = MaterialTheme.colorScheme.outline,
    shadow: Color = DefaultShadowColor,
    shadowPressed: Color = MaterialTheme.colorScheme.secondary
  ) = SuplaButtonColors(
    border = border,
    borderPressed = borderPressed,
    borderDisabled = borderDisabled,
    content = content,
    contentPressed = contentPressed,
    contentDisabled = contentDisabled,
    shadow = shadow,
    shadowPressed = shadowPressed
  )

  @Composable
  fun notRoundedShape(
    minWidth: Dp = dimensionResource(id = R.dimen.button_default_size),
    minHeight: Dp = dimensionResource(id = R.dimen.button_default_size)
  ) = SuplaButtonShape(
    topStartRadius = 0.dp,
    topEndRadius = 0.dp,
    bottomStartRadius = 0.dp,
    bottomEndRadius = 0.dp,
    minWidth = minWidth,
    minHeight = minHeight
  )

  @Composable
  fun allRoundedShape(
    radius: Dp = radius(),
    minWidth: Dp = radius.times(2),
    minHeight: Dp = radius.times(2)
  ) = SuplaButtonShape(
    topStartRadius = radius,
    topEndRadius = radius,
    bottomStartRadius = radius,
    bottomEndRadius = radius,
    minWidth = minWidth,
    minHeight = minHeight
  )

  @Composable
  fun topRoundedShape(
    radius: Dp = radius(),
    minWidth: Dp = radius.times(2),
    minHeight: Dp = radius.times(2)
  ) = SuplaButtonShape(
    topStartRadius = radius,
    topEndRadius = radius,
    bottomStartRadius = 0.dp,
    bottomEndRadius = 0.dp,
    minWidth = minWidth,
    minHeight = minHeight
  )

  @Composable
  fun bottomRoundedShape(
    radius: Dp = radius(),
    minWidth: Dp = radius.times(2),
    minHeight: Dp = radius.times(2)
  ) = SuplaButtonShape(
    topStartRadius = 0.dp,
    topEndRadius = 0.dp,
    bottomStartRadius = radius,
    bottomEndRadius = radius,
    minWidth = minWidth,
    minHeight = minHeight
  )

  @Composable
  fun startRoundedShape(
    radius: Dp = radius(),
    minWidth: Dp = radius.times(2),
    minHeight: Dp = radius.times(2)
  ) = SuplaButtonShape(
    topStartRadius = radius,
    topEndRadius = 0.dp,
    bottomStartRadius = radius,
    bottomEndRadius = 0.dp,
    minWidth = minWidth,
    minHeight = minHeight
  )

  @Composable
  fun endRoundedShape(
    radius: Dp = radius(),
    minWidth: Dp = radius.times(2),
    minHeight: Dp = radius.times(2)
  ) = SuplaButtonShape(
    topStartRadius = 0.dp,
    topEndRadius = radius,
    bottomStartRadius = 0.dp,
    bottomEndRadius = radius,
    minWidth = minWidth,
    minHeight = minHeight
  )
}
