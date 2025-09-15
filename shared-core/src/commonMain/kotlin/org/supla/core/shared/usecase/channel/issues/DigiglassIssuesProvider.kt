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
import org.supla.core.shared.data.model.channel.digiglassValue
import org.supla.core.shared.data.model.function.digiglass.SuplaDigiglassFlag
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.infrastructure.LocalizedStringId

class DigiglassIssuesProvider : ChannelIssuesProvider {
  override fun provide(channelWithChildren: ChannelWithChildren): List<ChannelIssueItem> {
    if (!channelWithChildren.isDigiglass) {
      return emptyList()
    }

    val (value) = guardLet(channelWithChildren.channel.digiglassValue) { return emptyList() }

    val issues = mutableListOf<ChannelIssueItem>()

    if (value.flags.contains(SuplaDigiglassFlag.PLANNED_REGENERATION_IN_PROGRESS)) {
      issues.add(ChannelIssueItem.Warning(LocalizedStringId.DIGIGLASS_PLANNED_REGENERATION))
    }
    if (value.flags.contains(SuplaDigiglassFlag.REGENERATION_AFTER_20H_IN_PROGRESS)) {
      issues.add(ChannelIssueItem.Warning(LocalizedStringId.DIGIGLASS_REGENERATION_AFTER_20H))
    }
    if (value.flags.contains(SuplaDigiglassFlag.TOO_LONG_OPERATION)) {
      issues.add(ChannelIssueItem.Error(LocalizedStringId.DIGIGLASS_TO_LONG_OPERATION))
    }

    return issues
  }
}

private val ChannelWithChildren.isDigiglass: Boolean
  get() = channel.function == SuplaFunction.DIGIGLASS_HORIZONTAL ||
    channel.function == SuplaFunction.DIGIGLASS_VERTICAL
