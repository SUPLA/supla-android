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

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import org.supla.android.R

@Composable
fun IconWrapper(bitmap: Bitmap? = null, painter: Painter? = null, color: Color? = null) {
  bitmap?.let {
    Image(
      bitmap = bitmap.asImageBitmap(),
      contentDescription = null,
      alignment = Alignment.Center,
      modifier = Modifier.size(dimensionResource(id = R.dimen.icon_default_size)),
      colorFilter = color?.let { ColorFilter.tint(color = it) }
    )
  }
  painter?.let {
    Image(
      painter = painter,
      contentDescription = null,
      alignment = Alignment.Center,
      modifier = Modifier.size(dimensionResource(id = R.dimen.icon_default_size)),
      colorFilter = color?.let { ColorFilter.tint(color = it) }
    )
  }
}
