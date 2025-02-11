package org.supla.core.shared.usecase.channel
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
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.channel.issues.ChannelIssuesProvider
import org.supla.core.shared.usecase.channel.issues.ContainerIssuesProvider
import org.supla.core.shared.usecase.channel.issues.ShadingSystemIssuesProvider
import org.supla.core.shared.usecase.channel.issues.ThermostatIssuesProvider
import org.supla.core.shared.usecase.channel.issues.ValveIssuesProvider

class GetChannelIssuesForListUseCase(
  private val getChannelLowBatteryIssueUseCase: GetChannelLowBatteryIssueUseCase,
  private val getChannelBatteryIconUseCase: GetChannelBatteryIconUseCase
) {

  private val otherIssuesProviders: List<ChannelIssuesProvider> = listOf(
    ThermostatIssuesProvider(),
    ShadingSystemIssuesProvider(),
    ContainerIssuesProvider(),
    ValveIssuesProvider()
  )

  operator fun invoke(channelWithChildren: ChannelWithChildren): ListItemIssues {
    val batteryIssue = getChannelLowBatteryIssueUseCase(channelWithChildren)
    val otherIssues = mutableListOf<ChannelIssueItem>()
    otherIssuesProviders.forEach { otherIssues.addAll(it.provide(channelWithChildren)) }

    val icons = mutableListOf<IssueIcon>()
    val messages = mutableListOf<LocalizedString>()
    otherIssues.minByOrNull { it.priority }?.let { icons.add(it.icon) }
    otherIssues.forEach { messages.addAll(it.messages) }
    batteryIssue?.let {
      if (!icons.contains(IssueIcon.Error)) {
        icons.clear()
        icons.add(IssueIcon.Error)
      }
      messages.addAll(it.messages)
    }

    getChannelBatteryIconUseCase(channelWithChildren)?.let { icons.add(it) }

    return ListItemIssues(icons, messages)
  }
}

fun Any?.isNull() = this == null
