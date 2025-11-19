package org.supla.android.data.source
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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.reactivex.rxjava3.core.Completable
import org.supla.android.data.source.local.dao.ColorListDao
import org.supla.android.data.source.local.entity.ColorEntity
import org.supla.android.usecases.channel.RemoveHiddenChannelsUseCase
import org.supla.android.usecases.profile.DeleteProfileUseCase
import org.supla.core.shared.extensions.ifTrue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ColorListRepository @Inject constructor(
  private val colorListDao: ColorListDao
) : RemoveHiddenChannelsUseCase.ChannelsDeletable, DeleteProfileUseCase.ProfileRemover {

  fun findAllChannelColors(remoteId: Int) = colorListDao.findAllColors(remoteId, isGroup = 0)

  fun findAllGroupColors(remoteId: Int) = colorListDao.findAllColors(remoteId, isGroup = 1)

  suspend fun save(remoteId: Int, isGroup: Boolean, color: Color, brightness: Int, profileId: Long) {
    val entity = ColorEntity(
      id = null,
      remoteId = remoteId,
      isGroup = isGroup,
      idx = 0,
      color = color.toArgb(),
      brightness = brightness.toShort(),
      profileId = profileId
    )

    colorListDao.save(entity)
    colorListDao.updatePositions(remoteId, isGroup)
  }

  suspend fun delete(colorId: Long, remoteId: Int, isGroup: Boolean) {
    colorListDao.delete(
      id = colorId,
      isGroup = isGroup.ifTrue { 1 } ?: 0
    )
    colorListDao.updatePositions(remoteId, isGroup)
  }

  suspend fun swapPositions(remoteId: Int, from: Int, to: Int, isGroup: Boolean) = colorListDao.swapPositions(remoteId, from, to, isGroup)

  override suspend fun deleteChannelRelated(remoteId: Int, profileId: Long) = colorListDao.deleteKtx(remoteId, profileId)
  override fun deleteByProfile(profileId: Long): Completable = colorListDao.deleteByProfile(profileId)
}
