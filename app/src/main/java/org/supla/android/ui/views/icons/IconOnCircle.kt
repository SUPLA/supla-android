package org.supla.android.ui.views.icons
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.supla.android.R

sealed interface IconType {
  val iconRes: Int
  val iconColor: Color
    @Composable get
  val backgroundColor: Color
    @Composable get
}

data object Warning : IconType {
  override val iconRes: Int = R.drawable.ic_warning
  override val iconColor: Color
    @Composable get() = MaterialTheme.colorScheme.tertiary
  override val backgroundColor: Color
    @Composable get() = MaterialTheme.colorScheme.tertiaryContainer
}

data object Error : IconType {
  override val iconRes: Int = R.drawable.ic_warning
  override val iconColor: Color
    @Composable get() = MaterialTheme.colorScheme.error
  override val backgroundColor: Color
    @Composable get() = MaterialTheme.colorScheme.errorContainer
}

data object Info : IconType {
  override val iconRes: Int = R.drawable.ic_check_filled
  override val iconColor: Color
    @Composable get() = MaterialTheme.colorScheme.primary
  override val backgroundColor: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant
}

@Composable
fun IconOnCircle(
  type: IconType,
  modifier: Modifier = Modifier
) =
  Box(
    modifier = modifier
      .size(80.dp)
      .background(
        color = type.backgroundColor,
        shape = RoundedCornerShape(40.dp)
      )
  ) {
    Icon(
      painter = painterResource(type.iconRes),
      contentDescription = null,
      tint = type.iconColor,
      modifier = Modifier.align(Alignment.Center)
    )
  }
