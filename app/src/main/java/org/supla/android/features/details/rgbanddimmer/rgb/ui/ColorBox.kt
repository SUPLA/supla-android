package org.supla.android.features.details.rgbanddimmer.rgb.ui
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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import org.supla.android.R

@Composable
fun ColorBox(color: Color?) {
  if (color == null) {
    UnknownColorBox()
  } else {
    KnownColorBox(color)
  }
}

@Composable
private fun KnownColorBox(color: Color) =
  Box(
    modifier = Modifier
      .size(dimensionResource(R.dimen.icon_default_size))
      .border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_small))
      )
      .background(color = color, shape = RoundedCornerShape(dimensionResource(R.dimen.radius_small)))
  )

@Composable
private fun UnknownColorBox() =
  Box(
    modifier = Modifier
      .size(dimensionResource(R.dimen.icon_default_size))
      .border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_small))
      )
  ) {
    Text(
      text = "?",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.error,
      modifier = Modifier.align(Alignment.Center)
    )
  }
