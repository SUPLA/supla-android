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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.supla.android.core.shared.shareable
import org.supla.android.data.model.general.IconType
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.indicatorIcon
import org.supla.android.data.source.local.entity.complex.onlineState
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.hvac.SuplaChannelHvacConfig
import org.supla.android.data.source.remote.hvac.filterRelationType
import org.supla.android.data.source.remote.thermostat.getIndicatorIcon
import org.supla.android.data.source.remote.thermostat.getSetpointText
import org.supla.android.di.FORMATTER_THERMOMETER
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.lists.onlineState
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetChannelIssuesForListUseCase
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CreateProfileChannelsListUseCase @Inject constructor(
  private val getChannelIssuesForListUseCase: GetChannelIssuesForListUseCase,
  private val getChannelChildrenTreeUseCase: GetChannelChildrenTreeUseCase,
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val channelRelationRepository: ChannelRelationRepository,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val channelRepository: RoomChannelRepository,
  private val getCaptionUseCase: GetCaptionUseCase,
  @param:Named(FORMATTER_THERMOMETER) private val thermometerValueFormatter: ValueFormatter,
  @param:Named(GSON_FOR_REPO) private val gson: Gson,
) {

  operator fun invoke(): Observable<List<ListItem>> =
    Single.zip(
      channelRelationRepository.findChildrenToParentsRelations().firstOrError(),
      channelRepository.findList()
    ) { relationMap, entities -> Pair(relationMap, entities) }
      .map { (relationMap, entities) ->
        val channels = mutableListOf<ListItem>()

        val channelsMap = mutableMapOf<Int, ChannelDataEntity>().also { map -> entities.forEach { map[it.remoteId] = it } }
        val allChildrenIds = relationMap.flatMap { it.value }.map { it.channelId }
        val childrenMap = mutableMapOf<Int, List<ChannelChildEntity?>>().also { map ->
          relationMap.forEach { relation ->
            val childrenList = LinkedList<Int>()
            map[relation.key] = getChannelChildrenTreeUseCase.invoke(relation.key, relationMap, channelsMap, childrenList)
          }
        }

        var location: LocationEntity? = null
        entities.forEach {
          if (allChildrenIds.contains(it.remoteId)) {
            // Skip channels which have parent ID.
            return@forEach
          }

          val currentLocation = location
          if (currentLocation == null || currentLocation.remoteId != it.locationEntity.remoteId) {
            val newLocation = it.locationEntity

            if (currentLocation == null || newLocation.caption != currentLocation.caption) {
              location = newLocation
              channels.add(ListItem.LocationItem(newLocation))
            }
          }

          location.let { locationEntity ->
            if (!locationEntity.isCollapsed(CollapsedFlag.CHANNEL)) {
              channels.add(createChannelListItem(it, childrenMap))
            }
          }
        }

        channels.toList()
      }.toObservable()

  private fun createChannelListItem(channelData: ChannelDataEntity, childrenMap: MutableMap<Int, List<ChannelChildEntity?>>) =
    when (channelData.function) {
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
      SuplaFunction.VALVE_PERCENTAGE -> toIconWithButtonsItem(channelData, childrenMap)

      SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS -> toHeatpolThermostatItem(channelData, childrenMap)

      SuplaFunction.CONTROLLING_THE_GATE,
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
      SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
      SuplaFunction.CONTROLLING_THE_DOOR_LOCK -> toIconWithRightButtonItem(channelData, childrenMap)

      SuplaFunction.HVAC_THERMOSTAT,
      SuplaFunction.HVAC_DOMESTIC_HOT_WATER,
      SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL -> toThermostatItem(channelData, childrenMap)
      SuplaFunction.HUMIDITY_AND_TEMPERATURE -> toDoubleValueItem(channelData, childrenMap)

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
      SuplaFunction.BINARY_SENSOR -> toIconValueItem(channelData, childrenMap)
    }

  private fun toIconValueItem(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ): ListItem.IconValueItem =
    channelWithChildren(channelData, childrenMap).let {
      ListItem.IconValueItem(
        channelData,
        channelData.locationEntity.caption,
        it.onlineState,
        getCaptionUseCase(channelData.shareable),
        getChannelIconUseCase(channelData),
        getChannelValueStringUseCase.valueOrNull(it),
        getChannelIssuesForListUseCase(it.shareable)
      )
    }

  private fun toThermostatItem(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ): ListItem.HvacThermostatItem {
    val thermostatValue = channelData.channelValueEntity.asThermostatValue()
    val children = childrenMap[channelData.remoteId]?.filterNotNull()
    val temperatureControlType = (channelData.configEntity?.toSuplaConfig(gson) as? SuplaChannelHvacConfig)?.temperatureControlType
    val thermometerChild = children?.firstOrNull { temperatureControlType.filterRelationType(it.relationType) }
    val indicatorIcon = thermostatValue.getIndicatorIcon() mergeWith children?.indicatorIcon
    val onlineState = channelData.channelValueEntity.status.onlineState mergeWith children?.onlineState

    return ListItem.HvacThermostatItem(
      channelData,
      channelData.locationEntity.caption,
      onlineState,
      getCaptionUseCase(channelData.shareable),
      getChannelIconUseCase(channelData),
      thermometerChild?.let { getChannelValueStringUseCase(it.withChildren) } ?: NO_VALUE_TEXT,
      getChannelIssuesForListUseCase(channelWithChildren(channelData, childrenMap).shareable),
      channelData.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue?.countdownEndsAt,
      thermostatValue.getSetpointText(thermometerValueFormatter),
      indicatorIcon.resource,
    )
  }

  private fun toHeatpolThermostatItem(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ): ListItem.HeatpolThermostatItem {
    val value = channelData.channelValueEntity.asHeatpolThermostatValue()

    return channelWithChildren(channelData, childrenMap).let { channelWithChildren ->
      ListItem.HeatpolThermostatItem(
        channelData,
        channelData.locationEntity.caption,
        channelData.channelValueEntity.status.onlineState,
        getCaptionUseCase(channelData.shareable),
        getChannelIconUseCase(channelData),
        getChannelValueStringUseCase(channelWithChildren),
        getChannelIssuesForListUseCase(channelWithChildren(channelData, childrenMap).shareable),
        thermometerValueFormatter.format(value.presetTemperature, ValueFormat.WithUnit),
      )
    }
  }

  private fun toIconWithButtonsItem(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ): ListItem.IconWithButtonsItem {
    return channelWithChildren(channelData, childrenMap).let {
      ListItem.IconWithButtonsItem(
        channelData,
        channelData.locationEntity.caption,
        it.onlineState,
        getCaptionUseCase(channelData.shareable),
        getChannelIconUseCase(channelData),
        value = getChannelValueStringUseCase.valueOrNull(it),
        channelData.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue?.countdownEndsAt,
        getChannelIssuesForListUseCase(it.shareable)
      )
    }
  }

  private fun toIconWithRightButtonItem(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ): ListItem.IconWithRightButtonItem {
    return channelWithChildren(channelData, childrenMap).let {
      ListItem.IconWithRightButtonItem(
        channelData,
        channelData.locationEntity.caption,
        it.onlineState,
        getCaptionUseCase(channelData.shareable),
        getChannelIconUseCase(channelData),
        value = getChannelValueStringUseCase.valueOrNull(it),
        channelData.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue?.countdownEndsAt,
        getChannelIssuesForListUseCase(it.shareable)
      )
    }
  }

  private fun toDoubleValueItem(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ): ListItem.DoubleValueItem =
    channelWithChildren(channelData, childrenMap).let {
      ListItem.DoubleValueItem(
        channelData,
        channelData.locationEntity.caption,
        it.onlineState,
        getCaptionUseCase(channelData.shareable),
        getChannelIconUseCase(channelData),
        value = getChannelValueStringUseCase.valueOrNull(it),
        getChannelIssuesForListUseCase(it.shareable),
        secondIcon = getChannelIconUseCase(channelData, IconType.SECOND),
        secondValue = getChannelValueStringUseCase.valueOrNull(it, ValueType.SECOND, withUnit = false)
      )
    }

  private fun channelWithChildren(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ): ChannelWithChildren {
    val children = mutableListOf<ChannelChildEntity>().apply {
      childrenMap[channelData.remoteId]?.filterIsInstance<ChannelChildEntity>()?.let { addAll(it) }
    }
    return ChannelWithChildren(channelData, children)
  }
}
