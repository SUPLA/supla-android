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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.supla.android.R

@Composable
fun IconButtonFilled(
  modifier: Modifier = Modifier,
  @DrawableRes icon: Int,
  disabled: Boolean = false,
  enabledColor: Color = MaterialTheme.colors.primary,
  iconTint: Color? = null,
  onClick: () -> Unit
) {
  Surface(
    color = if (disabled) colorResource(id = R.color.disabled) else enabledColor,
    shape = CircleShape,
    modifier = modifier
      .width(dimensionResource(id = R.dimen.button_default_size))
      .height(dimensionResource(id = R.dimen.button_default_size))
      .shadow(elevation = 4.dp, shape = CircleShape)
      .clickable(
        onClick = { onClick() },
        indication = if (disabled) null else rememberRipple(),
        interactionSource = remember { MutableInteractionSource() }
      )
  ) {
    val colorFilter = if (iconTint == null) null else {
      ColorFilter.tint(iconTint)
    }
    Image(
      painter = painterResource(id = icon),
      contentDescription = null,
      contentScale = ContentScale.Inside,
      modifier = Modifier
        .width(dimensionResource(id = R.dimen.icon_default_size))
        .height(dimensionResource(id = R.dimen.icon_default_size)),
      colorFilter = colorFilter
    )
  }
}
