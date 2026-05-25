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
import org.supla.android.data.source.local.dao.ChannelGroupRelationDao
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity
import org.supla.android.usecases.channel.VisibilityChange
import org.supla.android.usecases.developerinfo.CountProvider
import org.supla.android.usecases.profile.DeleteProfileUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelGroupRelationRepository @Inject constructor(
  private val channelGroupRelationDao: ChannelGroupRelationDao
) : CountProvider, DeleteProfileUseCase.ProfileRemover {
  fun findAllVisibleRelations() = channelGroupRelationDao.allVisibleRelations()

  fun findGroupRelationsData(remoteId: Int) = channelGroupRelationDao.findGroupRelationsData(remoteId)

  fun findGroupRelations(remoteId: Int) = channelGroupRelationDao.findGroupRelations(remoteId)

  suspend fun findByGroupAndChannel(groupId: Int, channelId: Int): ChannelGroupRelationEntity? =
    channelGroupRelationDao.findByGroupAndChannel(groupId, channelId)

  suspend fun insert(entity: ChannelGroupRelationEntity) = channelGroupRelationDao.insert(entity)

  suspend fun updateEntity(entity: ChannelGroupRelationEntity) =
    channelGroupRelationDao.update(entity)

  suspend fun setChannelGroupRelationsVisible(change: VisibilityChange): Boolean =
    channelGroupRelationDao.setChannelGroupRelationsVisible(change.newVisibility, change.applyForVisibility) > 0

  override fun count(): Observable<Int> = channelGroupRelationDao.count()

  override fun deleteByProfile(profileId: Long): Completable = channelGroupRelationDao.deleteByProfile(profileId)
}
