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

import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.usecase.channel.issues.ChannelIssuesProvider
import org.supla.core.shared.usecase.channel.issues.ContainerIssuesProvider
import org.supla.core.shared.usecase.channel.issues.DigiglassIssuesProvider
import org.supla.core.shared.usecase.channel.issues.LifespanIssuesProvider
import org.supla.core.shared.usecase.channel.issues.RelayIssuesProvider
import org.supla.core.shared.usecase.channel.issues.ShadingSystemIssuesProvider
import org.supla.core.shared.usecase.channel.issues.ThermostatIssuesProvider
import org.supla.core.shared.usecase.channel.issues.ValveIssuesProvider

class GetChannelSpecificIssuesUseCase {

  private val otherIssuesProviders: List<ChannelIssuesProvider> = listOf(
    ThermostatIssuesProvider(),
    ShadingSystemIssuesProvider(),
    ContainerIssuesProvider(),
    ValveIssuesProvider(),
    RelayIssuesProvider(),
    LifespanIssuesProvider(),
    DigiglassIssuesProvider()
  )

  operator fun invoke(channelWithChildren: ChannelWithChildren): List<ChannelIssueItem> {
    return otherIssuesProviders.flatMap { it.provide(channelWithChildren) }
  }
}
