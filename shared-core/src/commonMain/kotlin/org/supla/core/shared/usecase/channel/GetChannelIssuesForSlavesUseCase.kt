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
import org.supla.core.shared.data.model.general.Channel
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.channel.issues.ChannelIssuesProvider
import org.supla.core.shared.usecase.channel.issues.ShadingSystemIssuesProvider
import org.supla.core.shared.usecase.channel.issues.ThermostatIssuesProvider

class GetChannelIssuesForSlavesUseCase(
  private val getChannelLowBatteryIssueUseCase: GetChannelLowBatteryIssueUseCase,
  private val getChannelBatteryIconUseCase: GetChannelBatteryIconUseCase
) {

  private val otherIssuesProviders: List<ChannelIssuesProvider> = listOf(
    ThermostatIssuesProvider(),
    ShadingSystemIssuesProvider()
  )

  operator fun invoke(channel: Channel): ListItemIssues =
    invoke(ChannelWithChildren(channel, emptyList()))

  operator fun invoke(channelWithChildren: ChannelWithChildren): ListItemIssues {
    val batteryIssue = getChannelLowBatteryIssueUseCase(channelWithChildren)
    val otherIssues = mutableListOf<ChannelIssueItem>()
    otherIssuesProviders.forEach { otherIssues.addAll(it.provide(channelWithChildren)) }

    val messages = mutableListOf<LocalizedString>()
    otherIssues.forEach { messages.addAll(it.messages) }
    var icon = otherIssues.minByOrNull { it.priority }?.icon
    batteryIssue?.let {
      icon = IssueIcon.Error
      messages.addAll(it.messages)
    }

    icon?.let {
      return ListItemIssues(listOf(it), messages)
    }

    getChannelBatteryIconUseCase(channelWithChildren)?.let {
      return ListItemIssues(listOf(it))
    }

    return ListItemIssues()
  }
}
