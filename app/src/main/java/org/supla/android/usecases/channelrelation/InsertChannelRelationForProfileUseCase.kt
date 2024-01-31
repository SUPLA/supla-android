package org.supla.android.usecases.channelrelation
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
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.lib.SuplaChannelRelation
import org.supla.android.profile.ProfileManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsertChannelRelationForProfileUseCase @Inject constructor(
  private val profileManager: ProfileManager,
  private val channelRelationRepository: ChannelRelationRepository
) {
  operator fun invoke(suplaRelation: SuplaChannelRelation): Completable =
    profileManager.getCurrentProfile()
      .flatMapCompletable { channelRelationRepository.insertOrUpdate(ChannelRelationEntity.from(suplaRelation, it.id)) }
}

fun ChannelRelationEntity.Companion.from(suplaRelation: SuplaChannelRelation, profileId: Long): ChannelRelationEntity =
  ChannelRelationEntity(
    channelId = suplaRelation.channelId,
    parentId = suplaRelation.parentId,
    relationType = ChannelRelationType.from(suplaRelation.relationType),
    profileId = profileId,
    deleteFlag = false
  )
