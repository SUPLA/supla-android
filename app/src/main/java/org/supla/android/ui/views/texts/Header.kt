package org.supla.android.ui.views.texts
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
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.ui.views.buttons.IconButton

@Composable
fun Header(
  @StringRes textRes: Int,
  modifier: Modifier = Modifier,
  @DrawableRes iconRes: Int? = null,
  onClose: () -> Unit = {}
) =
  Row(
    verticalAlignment = Alignment.Bottom,
    modifier = modifier
  ) {
    Text(
      stringResource(textRes),
      style = MaterialTheme.typography.titleLarge,
      modifier = Modifier.weight(1f)
    )
    iconRes?.let {
      IconButton(
        icon = it,
        onClick = onClose,
        iconSize = dimensionResource(R.dimen.icon_default_size),
        tint = MaterialTheme.colorScheme.onBackground
      )
    }
  }
