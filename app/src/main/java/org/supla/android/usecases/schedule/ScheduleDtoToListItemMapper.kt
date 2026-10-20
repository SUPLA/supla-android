package org.supla.android.usecases.schedule
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

import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitSingleOrNull
import org.supla.android.R
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.SceneRepository
import org.supla.android.data.source.remote.rest.schedule.ScheduleDto
import org.supla.android.data.source.remote.rest.schedule.ScheduleSubjectDto
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.views.list.ListItemStatus
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleDtoToListItemMapper @Inject constructor(
  private val channelRepository: ChannelRepository,
  private val channelGroupRepository: ChannelGroupRepository,
  private val sceneRepository: SceneRepository,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getSceneIconUseCase: GetSceneIconUseCase
) {

  suspend operator fun invoke(schedule: ScheduleDto, profileId: Long): ListItem.Schedule? {
    val remoteId = schedule.id ?: return null

    return ListItem.Schedule(
      remoteId = remoteId,
      profileId = profileId,
      userCaption = schedule.caption.orEmpty(),
      status = ListItemStatus.Schedule(enabled = schedule.enabled == true),
      icon = resolveIcon(schedule, profileId)
    )
  }

  private suspend fun resolveIcon(schedule: ScheduleDto, profileId: Long): ImageId {
    val subjectType = schedule.subjectType ?: return unknownIcon()
    val subjectIcon = schedule.subject?.let { resolveIconFromDto(subjectType, it, profileId) }
    if (subjectIcon != null) return subjectIcon

    return resolveIconFromLocalData(subjectType, schedule.subjectId, profileId) ?: unknownIcon()
  }

  private fun resolveIconFromDto(
    subjectType: SubjectType,
    subject: ScheduleSubjectDto,
    profileId: Long
  ): ImageId? {
    return when (subjectType) {
      SubjectType.SCENE -> {
        if (subject.altIcon == null && subject.userIconId == null) {
          null
        } else {
          getSceneIconUseCase(subject.altIcon, subject.userIconId, profileId)
        }
      }
      SubjectType.GROUP, SubjectType.CHANNEL -> {
        val functionId = subject.function?.id ?: subject.functionId ?: return null
        val function = SuplaFunction.from(functionId)
        if (function == SuplaFunction.UNKNOWN) {
          null
        } else {
          val icon = getChannelIconUseCase.forCloudSubject(function, subject.altIcon, subject.userIconId, profileId)
          icon.takeUnless { it.id == R.drawable.ic_unknown_channel }
        }
      }
    }
  }

  private suspend fun resolveIconFromLocalData(
    subjectType: SubjectType,
    subjectId: Int?,
    profileId: Long
  ): ImageId? {
    subjectId ?: return null

    return when (subjectType) {
      SubjectType.SCENE -> {
        val scene = sceneRepository.findProfileScenes(profileId).await()
          .firstOrNull { it.remoteId == subjectId }
          ?.sceneEntity
        scene?.let(getSceneIconUseCase::invoke)
      }
      SubjectType.GROUP -> {
        val group = channelGroupRepository.findProfileGroups(profileId).await()
          .firstOrNull { it.remoteId == subjectId }
          ?.channelGroupEntity
        group?.let { getChannelIconUseCase.forState(it, ChannelState.active(it.function)) }
      }
      SubjectType.CHANNEL -> {
        val channel = channelRepository.findByRemoteId(profileId, subjectId).awaitSingleOrNull()
        channel?.let { getChannelIconUseCase.forState(it, ChannelState.active(it.function)) }
      }
    }
  }

  private fun unknownIcon() = ImageId(R.drawable.ic_unknown_channel)
}
