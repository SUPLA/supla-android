package org.supla.android.ui.views
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import org.supla.android.images.ImageCache
import org.supla.android.images.ImageId

@Composable
fun Image(drawableId: Int, modifier: Modifier = Modifier, contentDescription: String? = null, alpha: Float = DefaultAlpha) =
  androidx.compose.foundation.Image(
    painter = painterResource(id = drawableId),
    contentDescription = contentDescription,
    modifier = modifier,
    alpha = alpha
  )

@Composable
fun Image(
  imageId: ImageId,
  modifier: Modifier = Modifier,
  alignment: Alignment = Alignment.Center,
  contentScale: ContentScale = ContentScale.Fit,
  contentDescription: String? = null
) {
  if (imageId.userImage) {
    androidx.compose.foundation.Image(
      bitmap = ImageCache.getUserImageBitmap(LocalContext.current, imageId).asImageBitmap(),
      contentDescription = contentDescription,
      modifier = modifier,
      alignment = alignment,
      contentScale = contentScale
    )
  } else {
    androidx.compose.foundation.Image(
      painter = painterResource(id = imageId.id),
      contentDescription = contentDescription,
      modifier = modifier,
      alignment = alignment,
      contentScale = contentScale
    )
  }
}
