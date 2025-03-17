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
import org.supla.core.shared.data.model.channel.isHvacThermostat
import org.supla.core.shared.data.model.channel.thermostatValue
import org.supla.core.shared.data.model.function.thermostat.SuplaThermostatFlag
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.infrastructure.LocalizedStringId

class ThermostatIssuesProvider : ChannelIssuesProvider {
  override fun provide(channelWithChildren: ChannelWithChildren): List<ChannelIssueItem> {
    if (!channelWithChildren.channel.isHvacThermostat) {
      return emptyList()
    }

    if (channelWithChildren.channel.status.offline) {
      return emptyList()
    }

    val flags = channelWithChildren.channel.thermostatValue?.flags ?: emptyList()
    val issues = mutableListOf<ChannelIssueItem>()

    if (flags.contains(SuplaThermostatFlag.THERMOMETER_ERROR)) {
      issues.add(ChannelIssueItem.Error(LocalizedStringId.THERMOSTAT_THERMOMETER_ERROR))
    }
    if (flags.contains(SuplaThermostatFlag.BATTERY_COVER_OPEN)) {
      issues.add(ChannelIssueItem.Error(LocalizedStringId.THERMOSTAT_BATTER_COVER_OPEN))
    }
    if (flags.contains(SuplaThermostatFlag.CLOCK_ERROR)) {
      issues.add(ChannelIssueItem.Warning(LocalizedStringId.THERMOSTAT_CLOCK_ERROR))
    }
    if (flags.contains(SuplaThermostatFlag.CALIBRATION_ERROR)) {
      issues.add(ChannelIssueItem.Error(LocalizedStringId.THERMOSTAT_CALIBRATION_ERROR))
    }

    return issues
  }
}
