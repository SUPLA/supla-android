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
import org.supla.android.data.source.local.dao.ChannelDao
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.usecases.captionchange.CaptionChangeUseCase
import org.supla.android.usecases.channel.RemoveHiddenChannelsUseCase
import org.supla.android.usecases.developerinfo.CountProvider
import org.supla.android.usecases.profile.DeleteProfileUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomChannelRepository @Inject constructor(
  private val channelDao: ChannelDao
) : CountProvider, CaptionChangeUseCase.Updater, RemoveHiddenChannelsUseCase.ChannelsDeletable, DeleteProfileUseCase.ProfileRemover {

  fun findByRemoteId(remoteId: Int) = channelDao.findByRemoteId(remoteId)

  fun findByRemoteId(profileId: Long, remoteId: Int) = channelDao.findByRemoteId(profileId, remoteId)

  fun findList() = channelDao.findList().firstOrError()

  fun findObservableList() = channelDao.findList()

  fun findChannelDataEntity(remoteId: Int) = channelDao.findChannelDataEntity(remoteId)

  fun update(entity: ChannelEntity) = channelDao.update(entity)

  fun insert(entity: ChannelEntity) = channelDao.insert(entity)

  fun findChannelCountInLocation(locationRemoteId: Int) = channelDao.findChannelCountInLocation(locationRemoteId)

  fun findChannelsCount(profileId: Long) = channelDao.findChannelsCount(profileId)

  fun findProfileChannels(profileId: Long) = channelDao.findProfileChannels(profileId)

  suspend fun findHiddenChannels() = channelDao.findHiddenChannels()

  override fun count(): Observable<Int> = channelDao.count()

  override fun updateCaption(caption: String, remoteId: Int, profileId: Long): Completable =
    channelDao.updateCaption(caption, remoteId, profileId)

  override suspend fun deleteChannelRelated(remoteId: Int, profileId: Long) {
    channelDao.deleteKtx(remoteId, profileId)
  }

  override fun deleteByProfile(profileId: Long): Completable = channelDao.deleteByProfile(profileId)
}
