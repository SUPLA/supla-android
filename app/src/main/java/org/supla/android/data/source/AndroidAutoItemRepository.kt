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

import org.supla.android.data.source.local.dao.AndroidAutoItemDao
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity
import org.supla.android.usecases.channel.RemoveHiddenChannelsUseCase
import org.supla.android.usecases.scene.RemoveHiddenScenesUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAutoItemRepository @Inject constructor(
  private val androidAutoItemDao: AndroidAutoItemDao
) : RemoveHiddenChannelsUseCase.ChannelsDeletable, RemoveHiddenScenesUseCase.ScenesDeletable {

  fun findAll() = androidAutoItemDao.findAll()

  fun lastOrderNo() = androidAutoItemDao.lastOrderNo().onErrorReturnItem(0)

  fun findById(id: Long) = androidAutoItemDao.findById(id)

  fun delete(id: Long) = androidAutoItemDao.delete(id)

  fun insert(item: AndroidAutoItemEntity) = androidAutoItemDao.insert(item)

  fun setItemsOrder(orderedIds: List<Long>) = androidAutoItemDao.setItemsOrder(orderedIds)

  override suspend fun deleteChannelRelated(remoteId: Int, profileId: Long) =
    androidAutoItemDao.deleteChannelRelated(remoteId, profileId)

  override suspend fun deleteScenesRelated(remoteId: Int, profileId: Long) =
    androidAutoItemDao.deleteSceneRelated(remoteId, profileId)
}
