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
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.local.dao.ChannelGroupDao
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.usecases.captionchange.CaptionChangeUseCase
import org.supla.android.usecases.developerinfo.CountProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelGroupRepository @Inject constructor(
  private val channelGroupDao: ChannelGroupDao
) : CountProvider, CaptionChangeUseCase.Updater {

  fun findByRemoteId(remoteId: Int) = channelGroupDao.findByRemoteId(remoteId)

  fun findList() = channelGroupDao.findList()

  fun findGroupDataEntity(remoteId: Int) = channelGroupDao.findGroupDataEntity(remoteId)

  fun findGroupOnlineCount(groupId: Long) = channelGroupDao.findGroupOnlineCount(groupId)

  fun findProfileGroups(profileId: Long): Single<List<ChannelGroupDataEntity>> = channelGroupDao.findProfileGroups(profileId)

  fun update(groups: List<ChannelGroupEntity>) = channelGroupDao.update(groups)

  override fun count(): Observable<Int> = channelGroupDao.count()

  override fun updateCaption(caption: String, remoteId: Int, profileId: Long): Completable =
    channelGroupDao.updateCaption(caption, remoteId, profileId)
}
