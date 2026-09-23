package org.supla.android.usecases.group
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
import org.supla.android.di.FORMATTER_THERMOMETER
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.views.list.ListItemStatus
import org.supla.android.usecases.group.totalvalue.HeatpolThermostatGroupValue
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.GetChannelActionStringUseCase
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import org.supla.core.shared.usecase.channel.valueformatter.types.ValuePrecision
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class GroupToListItemMapper @Inject constructor(
  private val getGroupActivePercentageUseCase: GetGroupActivePercentageUseCase,
  private val getChannelActionStringUseCase: GetChannelActionStringUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getCaptionUseCase: GetCaptionUseCase,
  @param:Named(FORMATTER_THERMOMETER) private val thermometerValueFormatter: ValueFormatter
) {

  operator fun invoke(group: ChannelGroupDataEntity): ListItem =
    when (group.function) {
      SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS -> toHeatpolThermostatItem(group)
      SuplaFunction.UNKNOWN,
      SuplaFunction.NONE,
      SuplaFunction.THERMOMETER,
      SuplaFunction.HUMIDITY,
      SuplaFunction.HUMIDITY_AND_TEMPERATURE,
      SuplaFunction.OPEN_SENSOR_GATEWAY,
      SuplaFunction.OPEN_SENSOR_GATE,
      SuplaFunction.OPEN_SENSOR_GARAGE_DOOR,
      SuplaFunction.NO_LIQUID_SENSOR,
      SuplaFunction.OPEN_SENSOR_DOOR,
      SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
      SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER,
      SuplaFunction.OPEN_SENSOR_ROOF_WINDOW,
      SuplaFunction.RING,
      SuplaFunction.ALARM,
      SuplaFunction.NOTIFICATION,
      SuplaFunction.DEPTH_SENSOR,
      SuplaFunction.DISTANCE_SENSOR,
      SuplaFunction.OPENING_SENSOR_WINDOW,
      SuplaFunction.HOTEL_CARD_SENSOR,
      SuplaFunction.ALARM_ARMAMENT_SENSOR,
      SuplaFunction.MAIL_SENSOR,
      SuplaFunction.WIND_SENSOR,
      SuplaFunction.PRESSURE_SENSOR,
      SuplaFunction.RAIN_SENSOR,
      SuplaFunction.WEIGHT_SENSOR,
      SuplaFunction.WEATHER_STATION,
      SuplaFunction.ELECTRICITY_METER,
      SuplaFunction.IC_ELECTRICITY_METER,
      SuplaFunction.IC_GAS_METER,
      SuplaFunction.IC_WATER_METER,
      SuplaFunction.IC_HEAT_METER,
      SuplaFunction.GENERAL_PURPOSE_MEASUREMENT,
      SuplaFunction.GENERAL_PURPOSE_METER,
      SuplaFunction.DIGIGLASS_HORIZONTAL,
      SuplaFunction.DIGIGLASS_VERTICAL,
      SuplaFunction.PUMP_SWITCH,
      SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH,
      SuplaFunction.CONTAINER,
      SuplaFunction.SEPTIC_TANK,
      SuplaFunction.WATER_TANK,
      SuplaFunction.CONTAINER_LEVEL_SENSOR,
      SuplaFunction.FLOOD_SENSOR,
      SuplaFunction.MOTION_SENSOR,
      SuplaFunction.BINARY_SENSOR,
      SuplaFunction.SMOKE_SENSOR,
      SuplaFunction.CARBON_MONOXIDE_SENSOR,
      SuplaFunction.GAS_SENSOR,
      SuplaFunction.CONTROLLING_THE_GATE,
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
      SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
      SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
      SuplaFunction.POWER_SWITCH,
      SuplaFunction.LIGHTSWITCH,
      SuplaFunction.DIMMER,
      SuplaFunction.DIMMER_CCT,
      SuplaFunction.RGB_LIGHTING,
      SuplaFunction.DIMMER_AND_RGB_LIGHTING,
      SuplaFunction.DIMMER_CCT_AND_RGB,
      SuplaFunction.STAIRCASE_TIMER,
      SuplaFunction.HVAC_THERMOSTAT,
      SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
      SuplaFunction.HVAC_DOMESTIC_HOT_WATER,
      SuplaFunction.HVAC_HRV,
      SuplaFunction.VALVE_OPEN_CLOSE,
      SuplaFunction.VALVE_PERCENTAGE,
      SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
      SuplaFunction.TERRACE_AWNING,
      SuplaFunction.PROJECTOR_SCREEN,
      SuplaFunction.CURTAIN,
      SuplaFunction.VERTICAL_BLIND,
      SuplaFunction.ROLLER_GARAGE_DOOR -> toIconValueItem(group)
    }

  fun toIconValueItem(group: ChannelGroupDataEntity): ListItem.GroupItem =
    ListItem.GroupItem(
      remoteId = group.remoteId,
      profileId = group.profileId,
      function = group.function,
      locationCaption = group.locationEntity.caption,
      locationId = group.locationEntity.remoteId,
      status = ListItemStatus.Group(
        onlinePercentage = group.channelGroupEntity.onlinePercentage,
        activePercentage = getGroupActivePercentageUseCase(group.channelGroupEntity).coerceIn(0, 100).div(100f)
      ),
      captionProvider = getCaptionUseCase(group.shareable),
      userCaption = group.caption,
      icon = getChannelIconUseCase(group),
      leftButtonString = localizedString(getChannelActionStringUseCase.leftButton(group.function)),
      rightButtonString = localizedString(getChannelActionStringUseCase.rightButton(group.function))
    )

  fun toHeatpolThermostatItem(group: ChannelGroupDataEntity): ListItem.HeatpolThermostatItem =
    ListItem.HeatpolThermostatItem(
      remoteId = group.remoteId,
      profileId = group.profileId,
      locationCaption = group.locationEntity.caption,
      locationId = group.locationEntity.remoteId,
      status = ListItemStatus.Group(
        onlinePercentage = group.channelGroupEntity.onlinePercentage,
        activePercentage = getGroupActivePercentageUseCase(group.channelGroupEntity).coerceIn(0, 100).div(100f)
      ),
      captionProvider = getCaptionUseCase(group.shareable),
      userCaption = group.caption,
      icon = getChannelIconUseCase(group),
      value = getThermostatValue(group),
      issues = ListItemIssues.empty,
      subValue = getThermostatSubValue(group),
    )

  private fun getThermostatValue(group: ChannelGroupDataEntity): String {
    val min = group.channelGroupEntity.groupTotalValues
      .mapNotNull { (it as? HeatpolThermostatGroupValue)?.measuredTemperature }
      .minOrNull()

    val max = group.channelGroupEntity.groupTotalValues
      .mapNotNull { (it as? HeatpolThermostatGroupValue)?.measuredTemperature }
      .maxOrNull()

    if (min == null || max == null) {
      return NO_VALUE_TEXT
    }

    val format = ValueFormat.TemperatureWithDegree.copy(precision = ValueFormat.Precision.Custom(ValuePrecision.exact(1)))
    val minString = thermometerValueFormatter.format(min, format)
    val maxString = thermometerValueFormatter.format(max, format)

    return "$minString - $maxString"
  }

  private fun getThermostatSubValue(group: ChannelGroupDataEntity): String {
    val min = group.channelGroupEntity.groupTotalValues
      .mapNotNull { (it as? HeatpolThermostatGroupValue)?.presetTemperature }
      .minOrNull()

    val max = group.channelGroupEntity.groupTotalValues
      .mapNotNull { (it as? HeatpolThermostatGroupValue)?.presetTemperature }
      .maxOrNull()

    if (min == null || max == null) {
      return NO_VALUE_TEXT
    }

    val format = ValueFormat.TemperatureWithDegree.copy(precision = ValueFormat.Precision.Custom(ValuePrecision.exact(1)))
    val minString = thermometerValueFormatter.format(min, format)
    val maxString = thermometerValueFormatter.format(max, format)

    return "$minString - $maxString"
  }
}
