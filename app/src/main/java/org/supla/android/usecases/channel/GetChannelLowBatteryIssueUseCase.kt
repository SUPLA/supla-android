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

import org.supla.android.R
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.ui.LocalizedString
import org.supla.android.core.ui.localizedString
import org.supla.android.data.model.battery.BatteryInfo
import org.supla.android.data.model.battery.batterInfo
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.extensions.ifTrue
import org.supla.android.ui.lists.data.ChannelIssueItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChannelLowBatteryIssueUseCase @Inject constructor(
  private val getChannelCaptionUseCase: GetChannelCaptionUseCase,
  private val applicationPreferences: ApplicationPreferences
) {

  operator fun invoke(channelWithChildren: ChannelWithChildren): ChannelIssueItem? {
    val mainBatteryInfo = channelWithChildren.channel.batterInfo
    val childrenIssues = getChildrenIssues(channelWithChildren.children)

    val mainBatteryIssue = mainBatteryInfo?.level?.let { it < applicationPreferences.batteryWarningLevel } ?: false
    if (!mainBatteryIssue && childrenIssues.isEmpty()) {
      return null
    }

    val messages = mutableListOf<LocalizedString>()
    mainBatteryIssue.ifTrue {
      val id = channelWithChildren.channel.remoteId
      val name = getChannelCaptionUseCase(channelWithChildren.channel)
      val level = mainBatteryInfo?.level ?: 0

      messages.add(localizedString(R.string.channel_battery_level, id, name, level))
    }

    childrenIssues.forEach { issue ->
      issue.batteryInfo.level?.let { level ->
        messages.add(localizedString(R.string.channel_battery_level, issue.id, issue.name, level))
      }
    }

    return ChannelIssueItem.LowBattery(messages)
  }

  private fun getChildrenIssues(children: List<ChannelChildEntity>): List<BatteryIssue> {
    val list = mutableListOf<BatteryIssue>()
    children.forEach { child ->
      val batteryInfo = child.channelDataEntity.batterInfo

      batteryInfo?.level?.let {
        if (it < applicationPreferences.batteryWarningLevel) {
          list.add(
            BatteryIssue(
              name = getChannelCaptionUseCase(child.channel),
              id = child.channel.remoteId,
              batteryInfo = batteryInfo
            )
          )
        }
      }
      child.children.isNotEmpty().ifTrue {
        list.addAll(getChildrenIssues(child.children))
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
