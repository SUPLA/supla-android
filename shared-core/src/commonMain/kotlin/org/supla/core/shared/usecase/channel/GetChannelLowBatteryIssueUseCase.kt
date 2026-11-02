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

import org.supla.core.shared.data.model.battery.BatteryState
import org.supla.core.shared.data.model.channel.ChannelChild
import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.general.Channel
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.extensions.forTrue
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
    // childrenIds is used to eliminate cycles in children
    val childrenIds: MutableList<Int> = mutableListOf(channelWithChildren.channel.remoteId)
    val childrenIssues = getChildrenIssues(childrenIds, channelWithChildren.children)

    val mainBatteryIssues = getMainChannelBatteryMessage(channelWithChildren.channel)
    if (mainBatteryIssues.isEmpty() && childrenIssues.isEmpty()) {
      return null
    }

    val childrenHaveIssues = childrenIssues.isNotEmpty()
    val messages = mutableListOf<LocalizedString>()
    mainBatteryIssues.forEach {
      messages.add(it.toString(childrenHaveIssues))
    }

    // collectedChannelIds is used to eliminate duplications
    childrenIssues.forEach { issue ->
      messages.add(issue.toString(childrenHaveIssues))
    }

    return ChannelIssueItem.LowBattery(messages)
  }

  private fun getMainChannelBatteryMessage(channel: Channel): List<BatteryIssue> {
    val id = channel.remoteId
    val result = mutableListOf<BatteryIssue>()

    channel.batteryInfo?.level?.let {
      if (it < applicationPreferences.batteryWarningLevel) {
        result.add(BatteryIssue.Level(getCaptionUseCase(channel), id, it))
      }
    }

    channel.batteryInfo?.state?.let {
      if (it == BatteryState.LOW) {
        result.add(BatteryIssue.Low(getCaptionUseCase(channel), id))
      }
    }

    return result
  }

  private fun BatteryIssue.toString(childrenHaveIssues: Boolean): LocalizedString =
    if (childrenHaveIssues) {
      when (this) {
        is BatteryIssue.Level -> localizedString(LocalizedStringId.CHANNEL_BATTERY_LEVEL_WITH_INFO, id, name, level)
        is BatteryIssue.Low -> localizedString(LocalizedStringId.CHANNEL_BATTERY_LOW_WITH_INFO, id, name)
      }
    } else {
      when (this) {
        is BatteryIssue.Level -> localizedString(LocalizedStringId.CHANNEL_BATTERY_LEVEL, level)
        is BatteryIssue.Low -> localizedString(LocalizedStringId.CHANNEL_BATTERY_LOW)
      }
    }

  private fun getChildrenIssues(childrenIds: MutableList<Int>, children: List<ChannelChild>): List<BatteryIssue> {
    val list = mutableListOf<BatteryIssue>()
    children.forEach { child ->
      if (childrenIds.contains(child.channel.remoteId)) {
        // cycle found
        return@forEach
      }
      // May happen that same channel is linked twice via relation.
      // In such a situation would be listed twice, `duplicate` is to avoid that.
      val duplicate = list.map { it.id }.firstOrNull { it == child.channel.remoteId } != null
      if (!duplicate) {
        val batteryInfo = child.channel.batteryInfo
        batteryInfo?.level?.let {
          if (it < applicationPreferences.batteryWarningLevel) {
            list.add(
              BatteryIssue.Level(
                name = getCaptionUseCase(child.channel),
                id = child.channel.remoteId,
                level = it
              )
            )
          }
        }

        batteryInfo?.state?.let {
          if (it == BatteryState.LOW) {
            list.add(
              BatteryIssue.Low(
                name = getCaptionUseCase(child.channel),
                id = child.channel.remoteId
              )
            )
          }
        }
      }

      child.children.isNotEmpty().forTrue {
        childrenIds.add(child.channel.remoteId)
        list.addAll(getChildrenIssues(childrenIds, child.children))
        childrenIds.remove(child.channel.remoteId)
      }
    }

    return list
  }

  private sealed interface BatteryIssue {
    val name: LocalizedString
    val id: Int

    data class Level(
      override val name: LocalizedString,
      override val id: Int,
      val level: Int
    ) : BatteryIssue

    data class Low(
      override val name: LocalizedString,
      override val id: Int
    ) : BatteryIssue
  }
}
