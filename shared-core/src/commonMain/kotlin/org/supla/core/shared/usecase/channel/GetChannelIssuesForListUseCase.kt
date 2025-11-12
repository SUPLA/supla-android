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

import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.LocalizedStringId
import org.supla.core.shared.usecase.GetCaptionUseCase

class GetChannelIssuesForListUseCase(
  private val getChannelLowBatteryIssueUseCase: GetChannelLowBatteryIssueUseCase,
  private val getChannelSpecificIssuesUseCase: GetChannelSpecificIssuesUseCase,
  private val getChannelBatteryIconUseCase: GetChannelBatteryIconUseCase,
  private val getCaptionUseCase: GetCaptionUseCase
) {

  operator fun invoke(channelWithChildren: ChannelWithChildren): ListItemIssues {
    return when (channelWithChildren.channel.status) {
      SuplaChannelAvailabilityStatus.ONLINE_BUT_NOT_AVAILABLE -> handleNotAvailable()
      SuplaChannelAvailabilityStatus.FIRMWARE_UPDATE_ONGOING -> handleUpdate()
      else -> provideIssues(channelWithChildren)
    }
  }

  private fun handleNotAvailable(): ListItemIssues =
    ListItemIssues(
      icons = listOf(IssueIcon.Error),
      issuesStrings = listOf(LocalizedString.WithId(LocalizedStringId.CHANNEL_STATUS_NOT_AVAILABLE))
    )

  private fun handleUpdate(): ListItemIssues =
    ListItemIssues(
      icons = listOf(IssueIcon.Update),
      issuesStrings = listOf(LocalizedString.WithId(LocalizedStringId.CHANNEL_STATUS_UPDATING))
    )

  private fun provideIssues(channelWithChildren: ChannelWithChildren): ListItemIssues {
    val batteryIssue = getChannelLowBatteryIssueUseCase(channelWithChildren)
    val otherIssues = mutableListOf<ChannelIssueItem>()
    otherIssues.addAll(provideOtherIssues(channelWithChildren))
    getAvailabilityIssues(channelWithChildren)?.let { otherIssues.add(it) }

    val icons = mutableListOf<IssueIcon>()
    val messages = mutableListOf<LocalizedString>()
    otherIssues.minByOrNull { it.priority }?.let { icons.add(it.icon) }
    if (!icons.contains(IssueIcon.Sound)) {
      otherIssues.firstOrNull { it is ChannelIssueItem.SoundAlarm }?.let { icons.add(it.icon) }
    }
    otherIssues.forEach { messages.addAll(it.messages) }

    batteryIssue?.let { issue ->
      if (!icons.contains(IssueIcon.Error)) {
        icons.clear()
        icons.add(IssueIcon.Error)
        otherIssues.firstOrNull { it is ChannelIssueItem.SoundAlarm }?.let { icons.add(it.icon) }
      }
      messages.addAll(issue.messages)
    }

    // Add battery icon only if there is place for that
    if (otherIssues.size < 2) {
      getChannelBatteryIconUseCase(channelWithChildren)?.let { icons.add(it) }
    }

    return ListItemIssues(icons, messages)
  }

  private fun getAvailabilityIssues(channelWithChildren: ChannelWithChildren): ChannelIssueItem? {
    if (channelWithChildren.channel.status == SuplaChannelAvailabilityStatus.OFFLINE_REMOTE_WAKEUP_NOT_SUPPORTED) {
      return ChannelIssueItem.Warning(LocalizedStringId.CHANNEL_STATUS_AWAITING)
    }

    return null
  }

  private fun provideOtherIssues(channelWithChildren: ChannelWithChildren): List<ChannelIssueItem> {
    val childrenIssues = channelWithChildren.allChildrenFlat
      .flatMap { channelWithChildren ->
        getChannelSpecificIssuesUseCase(channelWithChildren)
          .map { it.extendOn(channelWithChildren.channel.remoteId, getCaptionUseCase(channelWithChildren.channel)) }
      }

    val channelIssues = getChannelSpecificIssuesUseCase(channelWithChildren)

    return if (childrenIssues.isEmpty()) {
      channelIssues
    } else {
      channelIssues.map {
        it.extendOn(channelWithChildren.channel.remoteId, getCaptionUseCase(channelWithChildren.channel))
      } + childrenIssues
    }
  }
}

fun Any?.isNull() = this == null
