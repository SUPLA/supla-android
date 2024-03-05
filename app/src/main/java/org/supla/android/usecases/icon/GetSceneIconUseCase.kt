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
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import org.supla.android.R
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.images.ImageCacheProxy
import org.supla.android.images.ImageId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetSceneIconUseCase @Inject constructor(
  private val imageCacheProxy: ImageCacheProxy
) {

  private val icons = intArrayOf(
    R.drawable.scene0, R.drawable.scene1,
    R.drawable.scene2, R.drawable.scene3,
    R.drawable.scene4, R.drawable.scene5,
    R.drawable.scene6, R.drawable.scene7,
    R.drawable.scene8, R.drawable.scene9,
    R.drawable.scene10, R.drawable.scene11,
    R.drawable.scene12, R.drawable.scene13,
    R.drawable.scene14, R.drawable.scene15,
    R.drawable.scene16, R.drawable.scene17,
    R.drawable.scene18, R.drawable.scene19
  )

  private val iconsNightMode = intArrayOf(
    R.drawable.scene0_nightmode, R.drawable.scene1_nightmode,
    R.drawable.scene2_nightmode, R.drawable.scene3_nightmode,
    R.drawable.scene4_nightmode, R.drawable.scene5_nightmode,
    R.drawable.scene6_nightmode, R.drawable.scene7_nightmode,
    R.drawable.scene8_nightmode, R.drawable.scene9_nightmode,
    R.drawable.scene10_nightmode, R.drawable.scene11_nightmode,
    R.drawable.scene12_nightmode, R.drawable.scene13_nightmode,
    R.drawable.scene14_nightmode, R.drawable.scene15_nightmode,
    R.drawable.scene16_nightmode, R.drawable.scene17_nightmode,
    R.drawable.scene18_nightmode, R.drawable.scene19_nightmode
  )

  operator fun invoke(sceneEntity: SceneEntity, nightMode: Boolean = false): ImageId {
    if (sceneEntity.userIcon != 0) {
      val id = ImageId(sceneEntity.userIcon, 1, sceneEntity.profileId!!.toLong())
      if (imageCacheProxy.bitmapExists(id)) {
        return id
      }
    }

    val icons = if (nightMode) {
      iconsNightMode
    } else {
      icons
    }

    return if (sceneEntity.altIcon >= icons.size) {
      ImageId(icons[0])
    } else {
      ImageId(icons[sceneEntity.altIcon])
    }
  }
}
