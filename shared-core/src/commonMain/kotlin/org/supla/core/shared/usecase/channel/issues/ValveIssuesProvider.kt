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

import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.channel.valveValue
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.valve.SuplaValveFlag
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.infrastructure.LocalizedStringId

class ValveIssuesProvider : ChannelIssuesProvider {
  override fun provide(channelWithChildren: ChannelWithChildren): List<ChannelIssueItem> {
    if (channelWithChildren.channel.function != SuplaFunction.VALVE_OPEN_CLOSE) {
      return emptyList()
    }

    if (!channelWithChildren.channel.online) {
      return emptyList()
    }

    val children = channelWithChildren.children.filter { it.relation.relationType == ChannelRelationType.DEFAULT }
    val issues = mutableListOf<ChannelIssueItem>()

    val anyActive = children.firstOrNull { it.channel.value?.get(0)?.toInt() == 1 } != null
    if (anyActive) {
      issues.add(ChannelIssueItem.Error(LocalizedStringId.FLOOD_SENSOR_ACTIVE))
    }

    val value = channelWithChildren.channel.valveValue
    value?.flags?.contains(SuplaValveFlag.FLOODING)?.ifTrue {
      issues.add(ChannelIssueItem.Error(LocalizedStringId.VALVE_FLOODING))
    }
    value?.flags?.contains(SuplaValveFlag.MANUALLY_CLOSED)?.ifTrue {
      issues.add(ChannelIssueItem.Error(LocalizedStringId.VALVE_MANUALLY_CLOSED))
    }

    return issues
  }
}
