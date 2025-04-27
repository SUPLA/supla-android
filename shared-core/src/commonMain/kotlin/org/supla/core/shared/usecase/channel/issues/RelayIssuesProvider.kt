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
import org.supla.core.shared.data.model.channel.relayValue
import org.supla.core.shared.data.model.function.relay.SuplaRelayFlag
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.infrastructure.LocalizedStringId

class RelayIssuesProvider : ChannelIssuesProvider {
  override fun provide(channelWithChildren: ChannelWithChildren): List<ChannelIssueItem> {
    if (!channelWithChildren.isRelay) {
      return emptyList()
    }
    if (channelWithChildren.channel.status.offline) {
      return emptyList()
    }

    val flags = channelWithChildren.channel.relayValue?.flags ?: emptyList()
    val issues = mutableListOf<ChannelIssueItem>()

    if (flags.contains(SuplaRelayFlag.OVERCURRENT_RELAY_OFF)) {
      issues.add(ChannelIssueItem.Error(LocalizedStringId.OVERCURRENT_WARNING))
    }

    return issues
  }

  private val ChannelWithChildren.isRelay: Boolean
    get() = when (channel.function) {
      SuplaFunction.POWER_SWITCH,
      SuplaFunction.LIGHTSWITCH,
      SuplaFunction.STAIRCASE_TIMER -> true

      else -> false
    }
}
