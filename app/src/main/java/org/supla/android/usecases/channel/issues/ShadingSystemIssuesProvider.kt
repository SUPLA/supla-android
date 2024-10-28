package org.supla.android.usecases.channel.issues
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

import org.supla.android.R
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.isFacadeBlind
import org.supla.android.data.source.local.entity.complex.isShadingSystem
import org.supla.android.data.source.local.entity.complex.isVerticalBlind
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.isGarageDoorRoller
import org.supla.android.data.source.local.entity.isProjectorScreen
import org.supla.android.data.source.remote.shadingsystem.SuplaShadingSystemFlag
import org.supla.android.ui.lists.data.ChannelIssueItem

class ShadingSystemIssuesProvider : ChannelIssuesProvider {
  override fun provide(channelWithChildren: ChannelWithChildren): List<ChannelIssueItem> {
    if (!handle(channelWithChildren.channel)) {
      return emptyList()
    }

    if (!channelWithChildren.channel.isOnline()) {
      return emptyList()
    }

    val flags = if (channelWithChildren.facadeBlindValue()) {
      channelWithChildren.channel.channelValueEntity.asFacadeBlindValue().flags
    } else {
      channelWithChildren.channel.channelValueEntity.asRollerShutterValue().flags
    }
    val issues = mutableListOf<ChannelIssueItem>()

    if (flags.contains(SuplaShadingSystemFlag.MOTOR_PROBLEM)) {
      issues.add(ChannelIssueItem.Error(R.string.motor_problem))
    }
    if (flags.contains(SuplaShadingSystemFlag.CALIBRATION_LOST)) {
      issues.add(ChannelIssueItem.Warning(R.string.calibration_lost))
    }
    if (flags.contains(SuplaShadingSystemFlag.CALIBRATION_FAILED)) {
      issues.add(ChannelIssueItem.Warning(R.string.calibration_failed))
    }

    return issues
  }

  private fun handle(channel: ChannelDataEntity) =
    channel.isShadingSystem() || channel.isProjectorScreen() || channel.isGarageDoorRoller()

  private fun ChannelWithChildren.facadeBlindValue() =
    channel.isFacadeBlind() || channel.isVerticalBlind()
}
