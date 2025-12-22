package org.supla.android.features.details.rgbanddimmer.common.ui
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance

@Composable
fun ValuesCard(
  modifier: Modifier = Modifier,
  horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
  enabled: Boolean = false,
  onClick: () -> Unit = {},
  content: @Composable RowScope.() -> Unit
) =
  Row(modifier = modifier) {
    Spacer(Modifier.weight(1f))
    Row(
      horizontalArrangement = horizontalArrangement,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .clickable(enabled = enabled) { onClick() }
        .background(
          color = MaterialTheme.colorScheme.surface,
          shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
        )
        .padding(vertical = Distance.small, horizontal = Distance.default),
      content = content
    )
    Spacer(Modifier.weight(1f))
  }
