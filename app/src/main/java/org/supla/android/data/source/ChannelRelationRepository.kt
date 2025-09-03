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

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.local.dao.ChannelRelationDao
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.usecases.channel.RemoveHiddenChannelsUseCase
import org.supla.android.usecases.developerinfo.CountProvider
import org.supla.android.usecases.profile.DeleteProfileUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRelationRepository @Inject constructor(
  private val channelRelationDao: ChannelRelationDao
) : CountProvider, RemoveHiddenChannelsUseCase.ChannelsDeletable, DeleteProfileUseCase.ProfileRemover {

  fun insertOrUpdate(channelRelation: ChannelRelationEntity) = channelRelationDao.insertOrUpdate(channelRelation)

  fun markAsRemovable(profileId: Long) = channelRelationDao.markAsRemovable(profileId)

  fun cleanUnused() = channelRelationDao.cleanUnused()

  fun findChildrenForParent(parentId: Int) = channelRelationDao.findChildrenFor(parentId)

  fun findChildrenToParentsRelations(): Observable<Map<Int, List<ChannelRelationEntity>>> =
    channelRelationDao.getForActiveProfile().map { entities ->
      mutableMapOf<Int, MutableList<ChannelRelationEntity>>().also { map ->
        entities.forEach { channelRelation ->
          if (map.contains(channelRelation.parentId)) {
            map[channelRelation.parentId]?.add(channelRelation)
          } else {
            map[channelRelation.parentId] = mutableListOf(channelRelation)
          }
        }
      }
    }

  override fun count(): Observable<Int> = channelRelationDao.count()

  override suspend fun deleteChannelRelated(remoteId: Int, profileId: Long) = channelRelationDao.deleteKtx(remoteId, profileId)
  override fun deleteByProfile(profileId: Long): Completable = channelRelationDao.deleteByProfile(profileId)
}
