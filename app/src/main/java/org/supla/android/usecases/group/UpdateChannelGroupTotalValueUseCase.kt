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

import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.ChannelGroupRelationRepository
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.usecases.group.totalvalue.DimmerAndRgbGroupValue
import org.supla.android.usecases.group.totalvalue.DimmerGroupValue
import org.supla.android.usecases.group.totalvalue.GroupTotalValue
import org.supla.android.usecases.group.totalvalue.GroupValue
import org.supla.android.usecases.group.totalvalue.HeatpolThermostatGroupValue
import org.supla.android.usecases.group.totalvalue.OpenedClosedGroupValue
import org.supla.android.usecases.group.totalvalue.ProjectorScreenGroupValue
import org.supla.android.usecases.group.totalvalue.RgbGroupValue
import org.supla.android.usecases.group.totalvalue.ShadingSystemGroupValue
import org.supla.android.usecases.group.totalvalue.ShadowingBlindGroupValue
import org.supla.core.shared.data.SuplaChannelFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChannelGroupTotalValueUseCase @Inject constructor(
  private val channelGroupRelationRepository: ChannelGroupRelationRepository,
  private val channelGroupRepository: ChannelGroupRepository
) {

  operator fun invoke(): Single<List<Int>> =
    channelGroupRelationRepository.findAllVisibleRelations()
      .map {
        val groups = mutableListOf<ChannelGroupEntity>()

        var group: ChannelGroupEntity? = null
        val groupTotalValue = GroupTotalValue()

        for (relation in it) {
          if (group == null) {
            group = relation.channelGroupEntity
          } else if (group.remoteId != relation.channelGroupEntity.remoteId) {
            group.updateBy(groupTotalValue) { updatedGroup -> groups.add(updatedGroup) }
            group = relation.channelGroupEntity
            groupTotalValue.clear()
          }

          group.getGroupValue(relation.channelValueEntity)?.let { value ->
            groupTotalValue.add(value, relation.channelValueEntity.online)
          }
        }

        group?.updateBy(groupTotalValue) { updatedGroup -> groups.add(updatedGroup) }

        return@map groups
      }
      .flatMap { groups ->
        channelGroupRepository.update(groups)
          .andThen(Single.just(groups.map { it.remoteId }))
      }
}

// Private extensions - ChannelGroupEntity

private fun ChannelGroupEntity.updateBy(totalValue: GroupTotalValue, onChangedCallback: (ChannelGroupEntity) -> Unit) {
  val totalOnline = totalValue.online
  val totalValueString = totalValue.asString()

  if (this.online != totalOnline || this.totalValue != totalValueString) {
    onChangedCallback(copy(online = totalOnline, totalValue = totalValueString))
  }
}

private fun ChannelGroupEntity.getGroupValue(value: ChannelValueEntity): GroupValue? {
  return when (function) {
    SuplaChannelFunction.CONTROLLING_THE_DOOR_LOCK,
    SuplaChannelFunction.CONTROLLING_THE_GATEWAY_LOCK,
    SuplaChannelFunction.CONTROLLING_THE_GATE,
    SuplaChannelFunction.CONTROLLING_THE_GARAGE_DOOR ->
      OpenedClosedGroupValue(value.getSensorHighValue())

    SuplaChannelFunction.POWER_SWITCH,
    SuplaChannelFunction.LIGHTSWITCH,
    SuplaChannelFunction.STAIRCASE_TIMER,
    SuplaChannelFunction.VALVE_OPEN_CLOSE ->
      OpenedClosedGroupValue(value.getValueHi())

    SuplaChannelFunction.CONTROLLING_THE_ROLLER_SHUTTER,
    SuplaChannelFunction.CONTROLLING_THE_ROOF_WINDOW,
    SuplaChannelFunction.TERRACE_AWNING,
    SuplaChannelFunction.CURTAIN,
    SuplaChannelFunction.VERTICAL_BLIND,
    SuplaChannelFunction.ROLLER_GARAGE_DOOR ->
      ShadingSystemGroupValue(value.asRollerShutterValue().alwaysValidPosition, (value.getSubValueHi() and 0x1) == 0x1)

    SuplaChannelFunction.CONTROLLING_THE_FACADE_BLIND ->
      value.asFacadeBlindValue().let { ShadowingBlindGroupValue(it.alwaysValidPosition, it.alwaysValidTilt) }

    SuplaChannelFunction.PROJECTOR_SCREEN ->
      ProjectorScreenGroupValue(value.asRollerShutterValue().alwaysValidPosition)

    SuplaChannelFunction.DIMMER ->
      DimmerGroupValue(value.asBrightness())

    SuplaChannelFunction.RGB_LIGHTING ->
      RgbGroupValue(value.asColor(), value.asBrightnessColor())

    SuplaChannelFunction.DIMMER_AND_RGB_LIGHTING ->
      DimmerAndRgbGroupValue(value.asColor(), value.asBrightnessColor(), value.asBrightness())

    SuplaChannelFunction.THERMOSTAT_HEATPOL_HOMEPLUS ->
      value.asHeatpolThermostatValue().let {
        HeatpolThermostatGroupValue(value.getValueHi(), it.measuredTemperature, it.presetTemperature)
      }

    SuplaChannelFunction.UNKNOWN,
    SuplaChannelFunction.NONE,
    SuplaChannelFunction.THERMOMETER,
    SuplaChannelFunction.HUMIDITY,
    SuplaChannelFunction.HUMIDITY_AND_TEMPERATURE,
    SuplaChannelFunction.OPEN_SENSOR_GATEWAY,
    SuplaChannelFunction.OPEN_SENSOR_GATE,
    SuplaChannelFunction.OPEN_SENSOR_GARAGE_DOOR,
    SuplaChannelFunction.NO_LIQUID_SENSOR,
    SuplaChannelFunction.OPEN_SENSOR_DOOR,
    SuplaChannelFunction.OPEN_SENSOR_ROLLER_SHUTTER,
    SuplaChannelFunction.OPEN_SENSOR_ROOF_WINDOW,
    SuplaChannelFunction.RING,
    SuplaChannelFunction.ALARM,
    SuplaChannelFunction.NOTIFICATION,
    SuplaChannelFunction.DEPTH_SENSOR,
    SuplaChannelFunction.DISTANCE_SENSOR,
    SuplaChannelFunction.OPENING_SENSOR_WINDOW,
    SuplaChannelFunction.HOTEL_CARD_SENSOR,
    SuplaChannelFunction.ALARM_ARMAMENT_SENSOR,
    SuplaChannelFunction.MAIL_SENSOR,
    SuplaChannelFunction.WIND_SENSOR,
    SuplaChannelFunction.PRESSURE_SENSOR,
    SuplaChannelFunction.RAIN_SENSOR,
    SuplaChannelFunction.WEIGHT_SENSOR,
    SuplaChannelFunction.WEATHER_STATION,
    SuplaChannelFunction.ELECTRICITY_METER,
    SuplaChannelFunction.IC_ELECTRICITY_METER,
    SuplaChannelFunction.IC_GAS_METER,
    SuplaChannelFunction.IC_WATER_METER,
    SuplaChannelFunction.IC_HEAT_METER,
    SuplaChannelFunction.HVAC_THERMOSTAT,
    SuplaChannelFunction.HVAC_THERMOSTAT_HEAT_COOL,
    SuplaChannelFunction.HVAC_DOMESTIC_HOT_WATER,
    SuplaChannelFunction.VALVE_PERCENTAGE,
    SuplaChannelFunction.GENERAL_PURPOSE_MEASUREMENT,
    SuplaChannelFunction.GENERAL_PURPOSE_METER,
    SuplaChannelFunction.DIGIGLASS_HORIZONTAL,
    SuplaChannelFunction.DIGIGLASS_VERTICAL,
    SuplaChannelFunction.PUMP_SWITCH,
    SuplaChannelFunction.HEAT_OR_COLD_SOURCE_SWITCH -> null
  }
}

// Private extensions - ChannelValueEntity

private fun ChannelValueEntity.getSensorHighValue() = getSubValueHi() and 0x1 == 0x1
