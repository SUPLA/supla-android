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
import org.supla.core.shared.data.model.channel.actionTriggerValue
import org.supla.core.shared.data.model.function.actiontrigger.SuplaActionTriggerFlag
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem

class ActionTriggerScheduleInfoProvider : ChannelIssuesProvider {
  override fun provide(channelWithChildren: ChannelWithChildren): List<ChannelIssueItem> {
    if (!channelWithChildren.isActionTrigger) {
      return emptyList()
    }
    if (channelWithChildren.channel.status.offline) {
      return emptyList()
    }

    val flags = channelWithChildren.channel.actionTriggerValue?.flags ?: emptyList()
    val issues = mutableListOf<ChannelIssueItem>()

    if (flags.contains(SuplaActionTriggerFlag.WEEKLY_SCHEDULE_ENABLED)) {
      issues.add(ChannelIssueItem.Schedule)
    }

    return issues
  }

  private val ChannelWithChildren.isActionTrigger: Boolean
    get() = channel.function == SuplaFunction.ACTION_TRIGGER
}
