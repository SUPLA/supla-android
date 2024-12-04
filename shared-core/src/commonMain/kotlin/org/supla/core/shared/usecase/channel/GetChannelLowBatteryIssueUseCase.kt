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

import org.supla.core.shared.data.model.battery.BatteryInfo
import org.supla.core.shared.data.model.channel.ChannelChild
import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.LocalizedStringId
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.infrastructure.storage.ApplicationPreferences
import org.supla.core.shared.usecase.GetCaptionUseCase

class GetChannelLowBatteryIssueUseCase(
  private val getCaptionUseCase: GetCaptionUseCase,
  private val applicationPreferences: ApplicationPreferences
) {
  operator fun invoke(channelWithChildren: ChannelWithChildren): ChannelIssueItem? {
    val mainBatteryInfo = channelWithChildren.channel.batteryInfo
    val childrenIds: MutableList<Int> = mutableListOf(channelWithChildren.channel.remoteId)
    val childrenIssues = getChildrenIssues(childrenIds, channelWithChildren.children)

    val mainBatteryIssue = mainBatteryInfo?.level?.let { it < applicationPreferences.batteryWarningLevel } ?: false
    if (!mainBatteryIssue && childrenIssues.isEmpty()) {
      return null
    }

    val messages = mutableListOf<LocalizedString>()
    mainBatteryIssue.ifTrue {
      val id = channelWithChildren.channel.remoteId
      val name = getCaptionUseCase(channelWithChildren.channel)
      val level = mainBatteryInfo?.level ?: 0

      messages.add(localizedString(LocalizedStringId.CHANNEL_BATTERY_LEVEL, id, name, level))
    }

    childrenIssues.forEach { issue ->
      issue.batteryInfo.level?.let { level ->
        messages.add(localizedString(LocalizedStringId.CHANNEL_BATTERY_LEVEL, issue.id, issue.name, level))
      }
    }

    return ChannelIssueItem.LowBattery(messages)
  }

  private fun getChildrenIssues(childrenIds: MutableList<Int>, children: List<ChannelChild>): List<BatteryIssue> {
    val list = mutableListOf<BatteryIssue>()
    children.forEach { child ->
      if (childrenIds.contains(child.channel.remoteId)) {
        // cycle found
        return@forEach
      }

      val batteryInfo = child.channel.batteryInfo
      batteryInfo?.level?.let { level ->
        // May happen that same channel is linked twice via relation.
        // In such a situation would be listed twice, `duplicate` is to avoid that.
        val duplicate = list.map { it.id }.firstOrNull { it == child.channel.remoteId } != null

        if (level < applicationPreferences.batteryWarningLevel && !duplicate) {
          list.add(
            BatteryIssue(
              name = getCaptionUseCase(child.channel),
              id = child.channel.remoteId,
              batteryInfo = batteryInfo
            )
          )
        }
      }
      child.children.isNotEmpty().ifTrue {
        childrenIds.add(child.channel.remoteId)
        list.addAll(getChildrenIssues(childrenIds, child.children))
        childrenIds.remove(child.channel.remoteId)
      }
    }

    return list
  }

  private data class BatteryIssue(
    val name: LocalizedString,
    val id: Int,
    val batteryInfo: BatteryInfo
  )
}
