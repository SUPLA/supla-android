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
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString

data class ChannelChartSets(
  val remoteId: Int,
  val function: SuplaFunction,
  val name: StringProvider,
  val aggregation: ChartDataAggregation,
  val dataSets: List<HistoryDataSet>,
  val customData: Any? = null,
  val typeName: LocalizedString? = null
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

  fun activate(): ChannelChartSets = copy(dataSets = dataSets.map { it.copy(active = true) })

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
      typeNameRes: LocalizedString? = null
    ) = ChannelChartSets(channel.remoteId, channel.function, name, aggregation, dataSets, customData, typeNameRes)
  }
}

fun SuplaFunction.hasCustomFilters(): Boolean =
  when (this) {
    SuplaFunction.UNKNOWN,
    SuplaFunction.NONE,
    SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
    SuplaFunction.CONTROLLING_THE_GATE,
    SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
    SuplaFunction.THERMOMETER,
    SuplaFunction.HUMIDITY,
    SuplaFunction.OPEN_SENSOR_GATEWAY,
    SuplaFunction.OPEN_SENSOR_GATE,
    SuplaFunction.OPEN_SENSOR_GARAGE_DOOR,
    SuplaFunction.NO_LIQUID_SENSOR,
    SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
    SuplaFunction.OPEN_SENSOR_DOOR,
    SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
    SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
    SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER,
    SuplaFunction.OPEN_SENSOR_ROOF_WINDOW,
    SuplaFunction.RING,
    SuplaFunction.ALARM,
    SuplaFunction.NOTIFICATION,
    SuplaFunction.DIMMER,
    SuplaFunction.RGB_LIGHTING,
    SuplaFunction.DIMMER_AND_RGB_LIGHTING,
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
    SuplaFunction.IC_ELECTRICITY_METER,
    SuplaFunction.IC_GAS_METER,
    SuplaFunction.IC_WATER_METER,
    SuplaFunction.IC_HEAT_METER,
    SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
    SuplaFunction.VALVE_OPEN_CLOSE,
    SuplaFunction.VALVE_PERCENTAGE,
    SuplaFunction.GENERAL_PURPOSE_MEASUREMENT,
    SuplaFunction.GENERAL_PURPOSE_METER,
    SuplaFunction.DIGIGLASS_HORIZONTAL,
    SuplaFunction.DIGIGLASS_VERTICAL,
    SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
    SuplaFunction.TERRACE_AWNING,
    SuplaFunction.PROJECTOR_SCREEN,
    SuplaFunction.CURTAIN,
    SuplaFunction.VERTICAL_BLIND,
    SuplaFunction.HVAC_THERMOSTAT,
    SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
    SuplaFunction.HVAC_DOMESTIC_HOT_WATER,
    SuplaFunction.ROLLER_GARAGE_DOOR,
    SuplaFunction.HUMIDITY_AND_TEMPERATURE,
    SuplaFunction.PUMP_SWITCH,
    SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH,
    SuplaFunction.CONTAINER,
    SuplaFunction.SEPTIC_TANK,
    SuplaFunction.WATER_TANK,
    SuplaFunction.CONTAINER_LEVEL_SENSOR,
    SuplaFunction.FLOOD_SENSOR,
    SuplaFunction.MOTION_SENSOR,
    SuplaFunction.BINARY_SENSOR -> false

    SuplaFunction.ELECTRICITY_METER,
    SuplaFunction.POWER_SWITCH,
    SuplaFunction.LIGHTSWITCH,
    SuplaFunction.STAIRCASE_TIMER -> true
  }
