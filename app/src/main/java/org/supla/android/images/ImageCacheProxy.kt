package org.supla.android.images
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

import android.content.Context
import android.graphics.Bitmap
import org.supla.android.data.source.local.UserIconDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCacheProxy @Inject constructor() {
  fun addImage(id: Int, image: UserIconDao.Image) {
    ImageCache.addImage(ImageId(id, image.subId, image.profileId), image.value)
  }

  fun addImage(imageId: ImageId, image: ByteArray) {
    ImageCache.addImage(imageId, image)
  }

  fun bitmapExists(imageId: ImageId): Boolean =
    ImageCache.bitmapExists(imageId)

  fun getBitmap(context: Context, imageId: ImageId): Bitmap? =
    ImageCache.getBitmap(context, imageId)
}
