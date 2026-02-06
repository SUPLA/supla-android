package org.supla.android.ui.views.forms
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
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.extensions.isNotNull
import org.supla.android.ui.extensions.ifTrue
import org.supla.android.ui.views.Image

@Composable
fun WarningMessage(
  @StringRes textRes: Int,
  modifier: Modifier = Modifier,
  @DrawableRes iconRes: Int = R.drawable.channel_warning_level1,
  withArrow: Boolean = true,
  onClick: (() -> Unit)? = null
) =
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Distance.small),
    modifier = modifier
      .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default)))
      .border(
        width = 1.dp,
        color = colorResource(id = R.color.gray_lighter),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
      )
      .padding(Distance.small)
      .clickable(enabled = onClick.isNotNull) { onClick?.invoke() }
  ) {
    Image(
      drawableId = iconRes,
      modifier = Modifier.size(dimensionResource(R.dimen.icon_big_size))
    )
    Text(
      text = stringResource(textRes),
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.weight(1f)
    )
    withArrow.ifTrue {
      Image(
        drawableId = R.drawable.ic_arrow_right,
        modifier = Modifier.size(dimensionResource(R.dimen.icon_big_size)),
      )
    }
  }
