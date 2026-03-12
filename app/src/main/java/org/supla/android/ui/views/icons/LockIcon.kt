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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import org.supla.android.R

@Composable
fun LockIcon(
  modifier: Modifier = Modifier,
  contentDescription: String? = null,
  color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  size: Dp = dimensionResource(R.dimen.icon_small_size),
  filled: Boolean = false
) =
  Image(
    imageVector = if (filled) Icons.Filled.Lock else Icons.Outlined.Lock,
    contentDescription = contentDescription,
    modifier = modifier.size(size),
    colorFilter = ColorFilter.tint(color)
  )
