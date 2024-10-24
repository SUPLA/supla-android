package org.supla.android.data.model.chart
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

import org.supla.android.core.ui.StringProvider
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.core.shared.data.SuplaChannelFunction

data class ChannelChartSets(
  val remoteId: Int,
  val function: SuplaChannelFunction,
  val name: StringProvider,
  val aggregation: ChartDataAggregation,
  val dataSets: List<HistoryDataSet>,
  val customData: Any? = null,
  val typeName: StringProvider? = null
) {
  val active: Boolean
    get() {
      dataSets.forEach {
        if (it.active) {
          return true
        }
      }

      return false
    }

  fun toggleActive(type: ChartEntryType): ChannelChartSets =
    copy(
      dataSets = dataSets.map {
        if (it.type == type) {
          it.copy(active = it.active.not())
        } else {
          it
        }
      }
    )

  fun deactivate(): ChannelChartSets = copy(dataSets = dataSets.map { it.copy(active = false) })

  fun setActive(types: List<ChartEntryType>?): ChannelChartSets =
    copy(dataSets = dataSets.map { it.copy(active = types?.contains(it.type) == true) })

  fun empty(): ChannelChartSets = copy(dataSets = dataSets.map { it.copy(entities = emptyList()) })

  companion object {
    operator fun invoke(
      channel: ChannelDataEntity,
      name: StringProvider,
      aggregation: ChartDataAggregation,
      dataSets: List<HistoryDataSet>,
      customData: Any? = null,
      typeNameRes: StringProvider? = null
    ) = ChannelChartSets(channel.remoteId, channel.function, name, aggregation, dataSets, customData, typeNameRes)
  }
}

fun SuplaChannelFunction.hasCustomFilters(): Boolean =
  when (this) {
    SuplaChannelFunction.UNKNOWN,
    SuplaChannelFunction.NONE,
    SuplaChannelFunction.CONTROLLING_THE_GATEWAY_LOCK,
    SuplaChannelFunction.CONTROLLING_THE_GATE,
    SuplaChannelFunction.CONTROLLING_THE_GARAGE_DOOR,
    SuplaChannelFunction.THERMOMETER,
    SuplaChannelFunction.HUMIDITY,
    SuplaChannelFunction.OPEN_SENSOR_GATEWAY,
    SuplaChannelFunction.OPEN_SENSOR_GATE,
    SuplaChannelFunction.OPEN_SENSOR_GARAGE_DOOR,
    SuplaChannelFunction.NO_LIQUID_SENSOR,
    SuplaChannelFunction.CONTROLLING_THE_DOOR_LOCK,
    SuplaChannelFunction.OPEN_SENSOR_DOOR,
    SuplaChannelFunction.CONTROLLING_THE_ROLLER_SHUTTER,
    SuplaChannelFunction.CONTROLLING_THE_ROOF_WINDOW,
    SuplaChannelFunction.OPEN_SENSOR_ROLLER_SHUTTER,
    SuplaChannelFunction.OPEN_SENSOR_ROOF_WINDOW,
    SuplaChannelFunction.RING,
    SuplaChannelFunction.ALARM,
    SuplaChannelFunction.NOTIFICATION,
    SuplaChannelFunction.DIMMER,
    SuplaChannelFunction.RGB_LIGHTING,
    SuplaChannelFunction.DIMMER_AND_RGB_LIGHTING,
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
    SuplaChannelFunction.IC_ELECTRICITY_METER,
    SuplaChannelFunction.IC_GAS_METER,
    SuplaChannelFunction.IC_WATER_METER,
    SuplaChannelFunction.IC_HEAT_METER,
    SuplaChannelFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
    SuplaChannelFunction.VALVE_OPEN_CLOSE,
    SuplaChannelFunction.VALVE_PERCENTAGE,
    SuplaChannelFunction.GENERAL_PURPOSE_MEASUREMENT,
    SuplaChannelFunction.GENERAL_PURPOSE_METER,
    SuplaChannelFunction.DIGIGLASS_HORIZONTAL,
    SuplaChannelFunction.DIGIGLASS_VERTICAL,
    SuplaChannelFunction.CONTROLLING_THE_FACADE_BLIND,
    SuplaChannelFunction.TERRACE_AWNING,
    SuplaChannelFunction.PROJECTOR_SCREEN,
    SuplaChannelFunction.CURTAIN,
    SuplaChannelFunction.VERTICAL_BLIND,
    SuplaChannelFunction.HVAC_THERMOSTAT,
    SuplaChannelFunction.HVAC_THERMOSTAT_HEAT_COOL,
    SuplaChannelFunction.HVAC_DOMESTIC_HOT_WATER,
    SuplaChannelFunction.ROLLER_GARAGE_DOOR,
    SuplaChannelFunction.HUMIDITY_AND_TEMPERATURE,
    SuplaChannelFunction.PUMP_SWITCH,
    SuplaChannelFunction.HEAT_OR_COLD_SOURCE_SWITCH -> false

    SuplaChannelFunction.ELECTRICITY_METER,
    SuplaChannelFunction.POWER_SWITCH,
    SuplaChannelFunction.LIGHTSWITCH,
    SuplaChannelFunction.STAIRCASE_TIMER -> true
  }
