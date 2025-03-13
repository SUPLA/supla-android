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
import org.supla.core.shared.data.model.general.SuplaFunction
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
            groupTotalValue.add(value, relation.channelValueEntity.status.online)
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
    SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
    SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
    SuplaFunction.CONTROLLING_THE_GATE,
    SuplaFunction.CONTROLLING_THE_GARAGE_DOOR ->
      OpenedClosedGroupValue(value.getSensorHighValue())

    SuplaFunction.POWER_SWITCH,
    SuplaFunction.LIGHTSWITCH,
    SuplaFunction.STAIRCASE_TIMER,
    SuplaFunction.VALVE_OPEN_CLOSE ->
      OpenedClosedGroupValue(value.getValueHi())

    SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
    SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
    SuplaFunction.TERRACE_AWNING,
    SuplaFunction.CURTAIN,
    SuplaFunction.VERTICAL_BLIND,
    SuplaFunction.ROLLER_GARAGE_DOOR ->
      ShadingSystemGroupValue(value.asRollerShutterValue().alwaysValidPosition, (value.getSubValueHi() and 0x1) == 0x1)

    SuplaFunction.CONTROLLING_THE_FACADE_BLIND ->
      value.asFacadeBlindValue().let { ShadowingBlindGroupValue(it.alwaysValidPosition, it.alwaysValidTilt) }

    SuplaFunction.PROJECTOR_SCREEN ->
      ProjectorScreenGroupValue(value.asRollerShutterValue().alwaysValidPosition)

    SuplaFunction.DIMMER ->
      DimmerGroupValue(value.asBrightness())

    SuplaFunction.RGB_LIGHTING ->
      RgbGroupValue(value.asColor(), value.asBrightnessColor())

    SuplaFunction.DIMMER_AND_RGB_LIGHTING ->
      DimmerAndRgbGroupValue(value.asColor(), value.asBrightnessColor(), value.asBrightness())

    SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS ->
      value.asHeatpolThermostatValue().let {
        HeatpolThermostatGroupValue(value.getValueHi(), it.measuredTemperature, it.presetTemperature)
      }

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
    SuplaFunction.HVAC_THERMOSTAT,
    SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
    SuplaFunction.HVAC_DOMESTIC_HOT_WATER,
    SuplaFunction.VALVE_PERCENTAGE,
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
    SuplaFunction.FLOOD_SENSOR -> null
  }
}

// Private extensions - ChannelValueEntity

private fun ChannelValueEntity.getSensorHighValue() = getSubValueHi() and 0x1 == 0x1
