package org.supla.android.usecases.list.eventmappers
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

import com.google.gson.Gson
import org.supla.android.core.shared.shareable
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.entity.complex.indicatorIcon
import org.supla.android.data.source.local.entity.complex.isHvacThermostat
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.remote.hvac.filterRelationType
import org.supla.android.data.source.remote.thermostat.getIndicatorIcon
import org.supla.android.data.source.remote.thermostat.getSetpointText
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.extensions.guardLet
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.list.CreateListItemUpdateEventDataUseCase
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetChannelIssuesForListUseCase
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ChannelWithChildrenToThermostatUpdateEventMapper @Inject constructor(
  private val getCaptionUseCase: GetCaptionUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val getChannelIssuesForListUseCase: GetChannelIssuesForListUseCase,
  private val valuesFormatter: ValuesFormatter,
  @Named(GSON_FOR_REPO) private val gson: Gson
) :
  CreateListItemUpdateEventDataUseCase.Mapper {
  override fun handle(item: Any): Boolean {
    return (item as? ChannelWithChildren)?.channel?.isHvacThermostat() == true
  }

  override fun map(item: Any): SlideableListItemData {
    val (channel) = guardLet(item as? ChannelWithChildren) {
      throw IllegalArgumentException("Expected Channel but got $item")
    }

    return toSlideableListItemData(channel, valuesFormatter)
  }

  private fun toSlideableListItemData(
    channelWithChildren: ChannelWithChildren,
    valuesFormatter: ValuesFormatter
  ): SlideableListItemData.Thermostat {
    val channelData = channelWithChildren.channel
    val children = channelWithChildren.children
    val thermostatValue = channelData.channelValueEntity.asThermostatValue()
    val temperatureControlType = channelWithChildren.temperatureControlType(gson)
    val thermometerChild = children.firstOrNull { temperatureControlType.filterRelationType(it.relationType) }?.withChildren
    val indicatorIcon = thermostatValue.getIndicatorIcon() mergeWith children.indicatorIcon

    return SlideableListItemData.Thermostat(
      onlineState = channelWithChildren.onlineState,
      title = getCaptionUseCase(channelData.shareable),
      icon = getChannelIconUseCase.invoke(channelData),
      value = thermometerChild?.let { getChannelValueStringUseCase(it) } ?: ValuesFormatter.NO_VALUE_TEXT,
      subValue = thermostatValue.getSetpointText(valuesFormatter),
      indicatorIcon = indicatorIcon.resource,
      issues = getChannelIssuesForListUseCase(channelWithChildren.shareable),
      estimatedTimerEndDate = channelData.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue?.countdownEndsAt,
      infoSupported = SuplaChannelFlag.CHANNEL_STATE.inside(channelData.flags)
    )
  }
}
