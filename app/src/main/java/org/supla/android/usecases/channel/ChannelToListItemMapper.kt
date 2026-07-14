package org.supla.android.usecases.channel
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
import org.supla.android.data.model.general.IconType
import org.supla.android.data.source.local.entity.complex.indicatorIcon
import org.supla.android.data.source.local.entity.complex.onlineState
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.hvac.SuplaChannelHvacConfig
import org.supla.android.data.source.remote.hvac.filterRelationType
import org.supla.android.data.source.remote.thermostat.getIndicatorIcon
import org.supla.android.data.source.remote.thermostat.getSetpointText
import org.supla.android.di.FORMATTER_THERMOMETER
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.events.DownloadEventsManager
import org.supla.android.events.inProgress
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.lists.onlineState
import org.supla.android.ui.views.list.ListItemStatus
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.GetChannelActionStringUseCase
import org.supla.core.shared.usecase.channel.GetChannelIssuesForListUseCase
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ChannelToListItemMapper @Inject constructor(
  private val getChannelIssuesForListUseCase: GetChannelIssuesForListUseCase,
  private val getChannelActionStringUseCase: GetChannelActionStringUseCase,
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val downloadEventsManager: DownloadEventsManager,
  private val getCaptionUseCase: GetCaptionUseCase,
  @param:Named(FORMATTER_THERMOMETER) private val thermometerValueFormatter: ValueFormatter,
  @param:Named(GSON_FOR_REPO) private val gson: Gson
) {

  operator fun invoke(channelWithChildren: ChannelWithChildren): ListItem =
    when (channelWithChildren.function) {
      SuplaFunction.HUMIDITY_AND_TEMPERATURE -> toDoubleValueItem(channelWithChildren)
      SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS -> toHeatpolThermostatItem(channelWithChildren)
      SuplaFunction.HVAC_THERMOSTAT,
      SuplaFunction.HVAC_DOMESTIC_HOT_WATER,
      SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL -> toThermostatItem(channelWithChildren)
      SuplaFunction.UNKNOWN,
      SuplaFunction.NONE,
      SuplaFunction.ALARM_ARMAMENT_SENSOR,
      SuplaFunction.HOTEL_CARD_SENSOR,
      SuplaFunction.THERMOMETER,
      SuplaFunction.DEPTH_SENSOR,
      SuplaFunction.DISTANCE_SENSOR,
      SuplaFunction.ELECTRICITY_METER,
      SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH,
      SuplaFunction.PUMP_SWITCH,
      SuplaFunction.NO_LIQUID_SENSOR,
      SuplaFunction.RAIN_SENSOR,
      SuplaFunction.MAIL_SENSOR,
      SuplaFunction.OPENING_SENSOR_WINDOW,
      SuplaFunction.OPEN_SENSOR_DOOR,
      SuplaFunction.OPEN_SENSOR_GATE,
      SuplaFunction.OPEN_SENSOR_GATEWAY,
      SuplaFunction.OPEN_SENSOR_GARAGE_DOOR,
      SuplaFunction.OPEN_SENSOR_ROOF_WINDOW,
      SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER,
      SuplaFunction.PRESSURE_SENSOR,
      SuplaFunction.WEIGHT_SENSOR,
      SuplaFunction.HUMIDITY,
      SuplaFunction.CONTAINER,
      SuplaFunction.WATER_TANK,
      SuplaFunction.SEPTIC_TANK,
      SuplaFunction.IC_WATER_METER,
      SuplaFunction.IC_GAS_METER,
      SuplaFunction.IC_HEAT_METER,
      SuplaFunction.IC_ELECTRICITY_METER,
      SuplaFunction.FLOOD_SENSOR,
      SuplaFunction.CONTAINER_LEVEL_SENSOR,
      SuplaFunction.WIND_SENSOR,
      SuplaFunction.DIGIGLASS_VERTICAL,
      SuplaFunction.DIGIGLASS_HORIZONTAL,
      SuplaFunction.GENERAL_PURPOSE_MEASUREMENT,
      SuplaFunction.GENERAL_PURPOSE_METER,
      SuplaFunction.RING,
      SuplaFunction.ALARM,
      SuplaFunction.NOTIFICATION,
      SuplaFunction.WEATHER_STATION,
      SuplaFunction.MOTION_SENSOR,
      SuplaFunction.BINARY_SENSOR,
      SuplaFunction.CONTROLLING_THE_GATE,
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
      SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
      SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
      SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
      SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
      SuplaFunction.TERRACE_AWNING,
      SuplaFunction.CURTAIN,
      SuplaFunction.VERTICAL_BLIND,
      SuplaFunction.PROJECTOR_SCREEN,
      SuplaFunction.ROLLER_GARAGE_DOOR,
      SuplaFunction.LIGHTSWITCH,
      SuplaFunction.POWER_SWITCH,
      SuplaFunction.STAIRCASE_TIMER,
      SuplaFunction.RGB_LIGHTING,
      SuplaFunction.DIMMER_AND_RGB_LIGHTING,
      SuplaFunction.DIMMER_CCT_AND_RGB,
      SuplaFunction.DIMMER,
      SuplaFunction.DIMMER_CCT,
      SuplaFunction.VALVE_OPEN_CLOSE,
      SuplaFunction.VALVE_PERCENTAGE,
      SuplaFunction.HVAC_HRV -> toChannelItem(channelWithChildren)
    }

  private fun toDoubleValueItem(
    channelWithChildren: ChannelWithChildren
  ): ListItem.DoubleValueItem =
    ListItem.DoubleValueItem(
      remoteId = channelWithChildren.remoteId,
      profileId = channelWithChildren.profileId,
      function = channelWithChildren.function,
      locationCaption = channelWithChildren.channel.locationEntity.caption,
      locationId = channelWithChildren.channel.locationEntity.remoteId,
      status = ListItemStatus.Channel(channelWithChildren.onlineState),
      captionProvider = getCaptionUseCase(channelWithChildren.channel.shareable),
      userCaption = channelWithChildren.caption,
      icon = getChannelIconUseCase(channelWithChildren),
      value = getChannelValueStringUseCase.valueOrNull(channel = channelWithChildren, valueType = ListFirstValue),
      issues = getChannelIssuesForListUseCase(channelWithChildren.shareable),
      processing = false,
      estimatedTimerEndDate = channelWithChildren.channel.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue?.countdownEndsAt,
      infoSupported = channelWithChildren.showInfo,
      leftButtonString = getChannelActionStringUseCase.leftButton(channelWithChildren.function)?.let { localizedString(it) },
      rightButtonString = getChannelActionStringUseCase.rightButton(channelWithChildren.function)?.let { localizedString(it) },
      secondIcon = getChannelIconUseCase(channelWithChildren, IconType.SECOND),
      secondValue = getChannelValueStringUseCase.valueOrNull(channelWithChildren, ListSecondValue, withUnit = false)
    )

  private fun toHeatpolThermostatItem(channelWithChildren: ChannelWithChildren): ListItem.HeatpolThermostatItem {
    val channel = channelWithChildren.channel
    val value = channel.channelValueEntity.asHeatpolThermostatValue()

    return ListItem.HeatpolThermostatItem(
      remoteId = channel.remoteId,
      profileId = channel.profileId,
      locationCaption = channel.locationEntity.caption,
      locationId = channelWithChildren.channel.locationEntity.remoteId,
      status = ListItemStatus.Channel(channel.channelValueEntity.status.onlineState),
      captionProvider = getCaptionUseCase(channel.shareable),
      userCaption = channel.caption,
      icon = getChannelIconUseCase(channel),
      value = getChannelValueStringUseCase(channelWithChildren, ListFirstValue),
      issues = getChannelIssuesForListUseCase(channelWithChildren.shareable),
      processing = false,
      estimatedTimerEndDate = channel.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue?.countdownEndsAt,
      infoSupported = channel.showInfo,
      subValue = thermometerValueFormatter.format(value.presetTemperature, ValueFormat.WithUnit),
    )
  }

  private fun toThermostatItem(channelWithChildren: ChannelWithChildren): ListItem.HvacThermostatItem {
    val channel = channelWithChildren.channel
    val thermostatValue = channel.channelValueEntity.asThermostatValue()
    val children = channelWithChildren.children
    val temperatureControlType = (channel.configEntity?.toSuplaConfig(gson) as? SuplaChannelHvacConfig)?.temperatureControlType
    val thermometerChild = children.firstOrNull { temperatureControlType.filterRelationType(it.relationType) }
    val indicatorIcon = thermostatValue.getIndicatorIcon() mergeWith children.indicatorIcon
    val onlineState = channel.channelValueEntity.status.onlineState mergeWith children.onlineState

    return ListItem.HvacThermostatItem(
      remoteId = channel.remoteId,
      profileId = channel.profileId,
      function = channel.function,
      locationCaption = channel.locationEntity.caption,
      locationId = channelWithChildren.channel.locationEntity.remoteId,
      status = ListItemStatus.Channel(onlineState),
      captionProvider = getCaptionUseCase(channel.shareable),
      userCaption = channel.caption,
      icon = getChannelIconUseCase(channel),
      value = thermometerChild?.let { getChannelValueStringUseCase(it.withChildren, ListFirstValue) } ?: NO_VALUE_TEXT,
      issues = getChannelIssuesForListUseCase(channelWithChildren.shareable),
      processing = false,
      estimatedTimerEndDate = channel.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue?.countdownEndsAt,
      infoSupported = channel.showInfo,
      subValue = thermostatValue.getSetpointText(thermometerValueFormatter),
      indicatorIcon = indicatorIcon.resource,
    )
  }

  private fun toChannelItem(channelWithChildren: ChannelWithChildren): ListItem.DefaultItem =
    ListItem.DefaultItem(
      remoteId = channelWithChildren.remoteId,
      profileId = channelWithChildren.profileId,
      function = channelWithChildren.function,
      locationCaption = channelWithChildren.channel.locationEntity.caption,
      locationId = channelWithChildren.channel.locationEntity.remoteId,
      status = ListItemStatus.Channel(channelWithChildren.onlineState),
      captionProvider = getCaptionUseCase(channelWithChildren.channel.shareable),
      userCaption = channelWithChildren.caption,
      icon = getChannelIconUseCase(channelWithChildren),
      value = getChannelValueStringUseCase.valueOrNull(channelWithChildren, ListFirstValue),
      issues = getChannelIssuesForListUseCase(channelWithChildren.shareable),
      processing = downloadEventsManager.getLastChannelDownloadState(channelWithChildren.remoteId).inProgress,
      estimatedTimerEndDate = channelWithChildren.channel.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue?.countdownEndsAt,
      infoSupported = channelWithChildren.showInfo,
      leftButtonString = getChannelActionStringUseCase.leftButton(channelWithChildren.function)?.let { localizedString(it) },
      rightButtonString = getChannelActionStringUseCase.rightButton(channelWithChildren.function)?.let { localizedString(it) }
    )
}
