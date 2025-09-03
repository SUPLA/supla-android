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
import org.supla.android.data.source.local.dao.ChannelValueDao
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.usecases.channel.RemoveHiddenChannelsUseCase
import org.supla.android.usecases.developerinfo.CountProvider
import org.supla.android.usecases.profile.DeleteProfileUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelValueRepository @Inject constructor(
  private val channelValueDao: ChannelValueDao
) : CountProvider, RemoveHiddenChannelsUseCase.ChannelsDeletable, DeleteProfileUseCase.ProfileRemover {

  fun findByRemoteId(remoteId: Int) = channelValueDao.findByRemoteId(remoteId)
  fun update(entity: ChannelValueEntity) = channelValueDao.update(entity)
  fun insert(entity: ChannelValueEntity) = channelValueDao.insert(entity)
  override fun count(): Observable<Int> = channelValueDao.count()
  override suspend fun deleteChannelRelated(remoteId: Int, profileId: Long) = channelValueDao.deleteKtx(remoteId, profileId)
  override fun deleteByProfile(profileId: Long): Completable = channelValueDao.deleteByProfile(profileId)
}
