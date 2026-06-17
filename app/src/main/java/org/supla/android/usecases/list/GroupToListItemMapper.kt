package org.supla.android.usecases.list
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

import org.supla.android.core.shared.shareable
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.views.list.ListItemStatus
import org.supla.android.usecases.group.GetGroupActivePercentageUseCase
import org.supla.android.usecases.group.totalvalue.HeatpolThermostatGroupValue
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import org.supla.core.shared.usecase.channel.valueformatter.types.ValuePrecision

interface GroupToListItemMapper {
  val getGroupActivePercentageUseCase: GetGroupActivePercentageUseCase
  val getCaptionUseCase: GetCaptionUseCase
  val getChannelIconUseCase: GetChannelIconUseCase
  val thermometerValueFormatter: ValueFormatter

  fun toIconValueItem(group: ChannelGroupDataEntity): ListItem.IconValueItem =
    ListItem.IconValueItem(
      dataBase = group,
      locationCaption = group.locationEntity.caption,
      status = ListItemStatus.Group(
        onlinePercentage = group.channelGroupEntity.onlinePercentage,
        activePercentage = getGroupActivePercentageUseCase(group.channelGroupEntity).coerceIn(0, 100).div(100f)
      ),
      captionProvider = getCaptionUseCase(group.shareable),
      icon = getChannelIconUseCase(group),
      value = null,
      issues = ListItemIssues.empty,
      processing = false
    )

  fun toIconWithButtonsItem(group: ChannelGroupDataEntity): ListItem.IconWithButtonsItem =
    ListItem.IconWithButtonsItem(
      dataBase = group,
      locationCaption = group.locationEntity.caption,
      status = ListItemStatus.Group(
        onlinePercentage = group.channelGroupEntity.onlinePercentage,
        activePercentage = getGroupActivePercentageUseCase(group.channelGroupEntity).coerceIn(0, 100).div(100f)
      ),
      captionProvider = getCaptionUseCase(group.shareable),
      icon = getChannelIconUseCase(group),
      value = null,
      estimatedTimerEndDate = null,
      issues = ListItemIssues.empty,
    )

  fun toIconWithRightButtonItem(group: ChannelGroupDataEntity): ListItem.IconWithRightButtonItem =
    ListItem.IconWithRightButtonItem(
      dataBase = group,
      locationCaption = group.locationEntity.caption,
      status = ListItemStatus.Group(
        onlinePercentage = group.channelGroupEntity.onlinePercentage,
        activePercentage = getGroupActivePercentageUseCase(group.channelGroupEntity).coerceIn(0, 100).div(100f)
      ),
      captionProvider = getCaptionUseCase(group.shareable),
      icon = getChannelIconUseCase(group),
      value = null,
      estimatedTimerEndDate = null,
      issues = ListItemIssues.empty
    )

  fun toHeatpolThermostatItem(group: ChannelGroupDataEntity): ListItem.HeatpolThermostatItem =
    ListItem.HeatpolThermostatItem(
      dataBase = group,
      locationCaption = group.locationEntity.caption,
      status = ListItemStatus.Group(
        onlinePercentage = group.channelGroupEntity.onlinePercentage,
        activePercentage = getGroupActivePercentageUseCase(group.channelGroupEntity).coerceIn(0, 100).div(100f)
      ),
      captionProvider = getCaptionUseCase(group.shareable),
      icon = getChannelIconUseCase(group),
      value = getThermostatValue(group),
      issues = ListItemIssues.empty,
      subValue = getThermostatSubValue(group),
    )

  private fun getThermostatValue(group: ChannelGroupDataEntity) : String {
    val min = group.channelGroupEntity.groupTotalValues
      .mapNotNull { (it as? HeatpolThermostatGroupValue)?.measuredTemperature }
      .min()

    val max = group.channelGroupEntity.groupTotalValues
      .mapNotNull { (it as? HeatpolThermostatGroupValue)?.measuredTemperature }
      .max()

    val format = ValueFormat.TemperatureWithDegree.copy(precision = ValueFormat.Precision.Custom(ValuePrecision.exact(1)))
    val minString = thermometerValueFormatter.format(min, format)
    val maxString = thermometerValueFormatter.format(max, format)

    return "$minString - $maxString"
  }

  private fun getThermostatSubValue(group: ChannelGroupDataEntity) : String {
    val min = group.channelGroupEntity.groupTotalValues
      .mapNotNull { (it as? HeatpolThermostatGroupValue)?.presetTemperature }
      .min()

    val max = group.channelGroupEntity.groupTotalValues
      .mapNotNull { (it as? HeatpolThermostatGroupValue)?.presetTemperature }
      .max()

    val format = ValueFormat.TemperatureWithDegree.copy(precision = ValueFormat.Precision.Custom(ValuePrecision.exact(1)))
    val minString = thermometerValueFormatter.format(min, format)
    val maxString = thermometerValueFormatter.format(max, format)

    return "$minString - $maxString"
  }
}