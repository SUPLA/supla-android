package org.supla.core.shared.usecase.channel.issues
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

import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.channel.facadeBlindValue
import org.supla.core.shared.data.model.channel.isFacadeBlind
import org.supla.core.shared.data.model.channel.isGarageDoorRoller
import org.supla.core.shared.data.model.channel.isProjectorScreen
import org.supla.core.shared.data.model.channel.isShadingSystem
import org.supla.core.shared.data.model.channel.isVerticalBlind
import org.supla.core.shared.data.model.channel.rollerShutterValue
import org.supla.core.shared.data.model.general.Channel
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.shadingsystem.SuplaShadingSystemFlag
import org.supla.core.shared.infrastructure.LocalizedStringId

class ShadingSystemIssuesProvider : ChannelIssuesProvider {
  override fun provide(channelWithChildren: ChannelWithChildren): List<ChannelIssueItem> {
    if (!handle(channelWithChildren.channel)) {
      return emptyList()
    }

    if (!channelWithChildren.channel.online) {
      return emptyList()
    }

    val flags = if (channelWithChildren.facadeBlindValue()) {
      channelWithChildren.channel.facadeBlindValue?.flags ?: emptyList()
    } else {
      channelWithChildren.channel.rollerShutterValue?.flags ?: emptyList()
    }
    val issues = mutableListOf<ChannelIssueItem>()

    if (flags.contains(SuplaShadingSystemFlag.MOTOR_PROBLEM)) {
      issues.add(ChannelIssueItem.Error(LocalizedStringId.MOTOR_PROBLEM))
    }
    if (flags.contains(SuplaShadingSystemFlag.CALIBRATION_LOST)) {
      issues.add(ChannelIssueItem.Warning(LocalizedStringId.CALIBRATION_LOST))
    }
    if (flags.contains(SuplaShadingSystemFlag.CALIBRATION_FAILED)) {
      issues.add(ChannelIssueItem.Warning(LocalizedStringId.CALIBRATION_FAILED))
    }

    return issues
  }

  private fun handle(channel: Channel) =
    channel.isShadingSystem || channel.isProjectorScreen || channel.isGarageDoorRoller

  private fun ChannelWithChildren.facadeBlindValue() =
    channel.isFacadeBlind || channel.isVerticalBlind
}
