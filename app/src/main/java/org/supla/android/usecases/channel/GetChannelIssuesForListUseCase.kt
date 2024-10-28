package org.supla.android.usecases.channel
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

import org.supla.android.core.ui.LocalizedString
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.ui.lists.ListItemIssues
import org.supla.android.ui.lists.data.ChannelIssueItem
import org.supla.android.ui.lists.data.IssueIcon
import org.supla.android.usecases.channel.issues.ShadingSystemIssuesProvider
import org.supla.android.usecases.channel.issues.ThermostatIssuesProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChannelIssuesForListUseCase @Inject constructor(
  private val getChannelLowBatteryIssueUseCase: GetChannelLowBatteryIssueUseCase,
  private val getChannelBatteryIconUseCase: GetChannelBatteryIconUseCase
) {

  private val otherIssuesProviders = listOf(
    ThermostatIssuesProvider(),
    ShadingSystemIssuesProvider()
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
