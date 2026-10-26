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
import org.supla.android.data.source.remote.rest.schedule.ScheduleClosestExecutionDto
import org.supla.android.data.source.remote.rest.schedule.ScheduleDto
import org.supla.android.data.source.remote.rest.schedule.ScheduleSubjectDto
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

data class ScheduleVo(
  val remoteId: Int,
  val profileId: Long,
  val userCaption: String,
  val caption: LocalizedString,
  val enabled: Boolean,
  val lastExecution: ScheduleExecutionVo?,
  val nextExecution: ScheduleExecutionVo?
)

data class ScheduleExecutionVo(
  val plannedTimestamp: LocalDateTime?,
  val failed: Boolean?
)

@Singleton
class ScheduleDtoToListItemMapper @Inject constructor() {

  operator fun invoke(schedule: ScheduleDto, profileId: Long): ScheduleVo {
    val remoteId = schedule.id ?: 0

    return ScheduleVo(
      remoteId = remoteId,
      profileId = profileId,
      userCaption = schedule.caption ?: "",
      caption = schedule.captionString,
      enabled = schedule.enabled == true,
      lastExecution = schedule.closestExecutions?.past?.lastOrNull()?.toVo(),
      nextExecution = schedule.closestExecutions?.future?.firstOrNull()?.toVo()
    )
  }

  private fun ScheduleClosestExecutionDto.toVo() =
    ScheduleExecutionVo(
      plannedTimestamp = plannedTimestamp,
      failed = failed
    )
}
