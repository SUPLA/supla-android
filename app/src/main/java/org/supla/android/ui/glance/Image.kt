package org.supla.android.ui.glance
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

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.layout.ContentScale
import androidx.glance.unit.ColorProvider
import org.supla.android.images.ImageCache
import org.supla.android.images.ImageId

@Composable
fun Image(
  imageId: ImageId,
  modifier: GlanceModifier = GlanceModifier,
  contentScale: ContentScale = ContentScale.Fit,
  contentDescription: String? = null,
  tint: Color? = null
) {
  if (imageId.userImage) {
    androidx.glance.Image(
      provider = ImageProvider(bitmap = ImageCache.getUserImageBitmap(LocalContext.current, imageId)),
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale,
      colorFilter = tint?.let { ColorFilter.tint(ColorProvider(it)) }
    )
  } else {
    androidx.glance.Image(
      provider = ImageProvider(resId = imageId.id),
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale,
      colorFilter = tint?.let { ColorFilter.tint(ColorProvider(it)) }
    )
  }
}
