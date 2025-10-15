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
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.LocalizedStringId

class LifespanIssuesProvider : ChannelIssuesProvider {
  override fun provide(channelWithChildren: ChannelWithChildren): List<ChannelIssueItem> {
    if (channelWithChildren.channel.function != SuplaFunction.LIGHTSWITCH) {
      return emptyList()
    }

    val channelState = channelWithChildren.channel.channelState
    if (channelState == null) {
      return emptyList()
    }

    if (channelState.lightSourceLifespan == null || channelState.lightSourceLifespan <= 0) {
      return emptyList()
    }

    val lifespanLeft = channelState.lightSourceLifespanLeft ?: channelState.lightSourceOperatingTimePercentLeft
    if (lifespanLeft == null || lifespanLeft > 20) {
      return emptyList()
    }

    val issue =
      if (channelWithChildren.channel.altIcon == 2) {
        if (lifespanLeft <= 5) {
          ChannelIssueItem.Error(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING_REPLACE, listOf(lifespanLeft)))
        } else {
          ChannelIssueItem.Warning(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING_SCHEDULE, listOf(lifespanLeft)))
        }
      } else {
        if (lifespanLeft <= 5) {
          ChannelIssueItem.Error(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING, listOf(lifespanLeft)))
        } else {
          ChannelIssueItem.Warning(LocalizedString.WithId(LocalizedStringId.LIFESPAN_WARNING, listOf(lifespanLeft)))
        }
      }

    return listOf(issue)
  }
}
