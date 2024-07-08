package org.supla.android.ui.views.list.components
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

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import org.supla.android.R
import org.supla.android.images.ImageId
import org.supla.android.ui.views.Image

@Composable
fun ListItemIcon(imageId: ImageId, scale: Float, modifier: Modifier = Modifier) {
  Image(
    imageId = imageId,
    contentDescription = null,
    alignment = Alignment.Center,
    modifier = modifier
      .width(dimensionResource(id = R.dimen.channel_img_width).times(scale))
      .height(dimensionResource(id = R.dimen.channel_img_height).times(scale)),
  )
}
