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
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.isThermostat
import org.supla.android.data.source.remote.thermostat.SuplaThermostatFlag
import org.supla.android.ui.lists.data.ChannelIssueItem

class ThermostatIssuesProvider : ChannelIssuesProvider {
  override fun provide(channelWithChildren: ChannelWithChildren): List<ChannelIssueItem> {
    if (!channelWithChildren.channel.isThermostat()) {
      return emptyList()
    }

    if (!channelWithChildren.channel.isOnline()) {
      return emptyList()
    }

    val thermostatValue = channelWithChildren.channel.channelValueEntity.asThermostatValue()
    val issues = mutableListOf<ChannelIssueItem>()

    if (thermostatValue.flags.contains(SuplaThermostatFlag.THERMOMETER_ERROR)) {
      issues.add(ChannelIssueItem.Error(R.string.thermostat_thermometer_error))
    }
    if (thermostatValue.flags.contains(SuplaThermostatFlag.BATTERY_COVER_OPEN)) {
      issues.add(ChannelIssueItem.Error(R.string.thermostat_battery_cover_open))
    }
    if (thermostatValue.flags.contains(SuplaThermostatFlag.CLOCK_ERROR)) {
      issues.add(ChannelIssueItem.Warning(R.string.thermostat_clock_error))
    }

    return issues
  }
}
