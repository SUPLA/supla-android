package org.supla.android.usecases.icon
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
syays GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import io.reactivex.rxjava3.core.Completable
import org.supla.android.Trace
import org.supla.android.data.source.RoomUserIconRepository
import org.supla.android.data.source.local.entity.UserIconEntity
import org.supla.android.extensions.TAG
import org.supla.android.images.ImageCacheProxy
import org.supla.android.images.ImageId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadUserIconsIntoCacheUseCase @Inject constructor(
  private val userIconRepository: RoomUserIconRepository,
  private val imageCacheProxy: ImageCacheProxy
) {

  operator fun invoke(): Completable =
    userIconRepository.loadAllIcons()
      .firstElement()
      .map { icons ->
        Trace.d(TAG, "Icons loading started")
        icons.forEach { icon ->
          ImageType.values().forEach { addImage(icon, it) }
        }
        Trace.d(TAG, "Icons loading finished")
      }
      .ignoreElement()

  private fun addImage(icon: UserIconEntity, type: ImageType) {
    val image = type.provider(icon)
    if (image != null && image.isNotEmpty()) {
      imageCacheProxy.addImage(ImageId(icon.remoteId, type.subId, icon.profileId).setNightMode(type.forNightMode), image)
    }
  }

  private enum class ImageType(val subId: Int, val forNightMode: Boolean, val provider: (UserIconEntity) -> ByteArray?) {
    IMAGE1(1, false, { it.image1 }),
    IMAGE2(2, false, { it.image2 }),
    IMAGE3(3, false, { it.image3 }),
    IMAGE4(4, false, { it.image4 }),
    IMAGE1_DARK(1, true, { it.image1Dark }),
    IMAGE2_DARK(2, true, { it.image2Dark }),
    IMAGE3_DARK(3, true, { it.image3Dark }),
    IMAGE4_DARK(4, true, { it.image4Dark })
  }
}
