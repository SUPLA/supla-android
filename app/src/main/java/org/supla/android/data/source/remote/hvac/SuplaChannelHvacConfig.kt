package org.supla.android.data.source.remote.hvac
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

import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.android.data.source.remote.hvac.SuplaTemperatureControlType.AUX_HEATER_COOLER_TEMPERATURE
import org.supla.android.tools.UsedFromNativeCode
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.extensions.fromSuplaTemperature

@UsedFromNativeCode
data class SuplaHvacTemperatures(
  val freezeProtection: Short?,
  val eco: Short?,
  val comfort: Short?,
  val boost: Short?,
  val heatProtection: Short?,
  val histeresis: Short?,
  val belowAlarm: Short?,
  val aboveAlarm: Short?,
  val auxMinSetpoint: Short?,
  val auxMaxSetpoint: Short?,
  val roomMin: Short?,
  val roomMax: Short?,
  val auxMin: Short?,
  val auxMax: Short?,
  val histeresisMin: Short?,
  val histeresisMax: Short?,
  val heatCoolOffsetMin: Short?,
  val heatCoolOffsetMax: Short?
)

@UsedFromNativeCode
enum class SuplaHvacThermometerType(value: Int) {
  NOT_SET(0),
  DISABLED(1),
  FLOOR(2),
  WATER(3),
  GENERIC_HEATER(4),
  GENERIC_COOLER(5)
}

@UsedFromNativeCode
enum class SuplaHvacAlgorithm(value: Int) {
  NOT_SET(0),
  ON_OFF_SETPOINT_MIDDLE(1),
  ON_OFF_SETPOINT_AT_MOST(2)
}

@UsedFromNativeCode
enum class SuplaTemperatureControlType(value: Int) {
  NOT_SUPPORTED(0),
  ROOM_TEMPERATURE(1),
  AUX_HEATER_COOLER_TEMPERATURE(2)
}

fun SuplaTemperatureControlType?.filterRelationType(relationType: ChannelRelationType): Boolean =
  when (relationType) {
    ChannelRelationType.AUX_THERMOMETER_FLOOR,
    ChannelRelationType.AUX_THERMOMETER_WATER,
    ChannelRelationType.AUX_THERMOMETER_GENERIC_COOLER,
    ChannelRelationType.AUX_THERMOMETER_GENERIC_HEATER -> this == AUX_HEATER_COOLER_TEMPERATURE

    ChannelRelationType.MAIN_THERMOMETER -> this != AUX_HEATER_COOLER_TEMPERATURE
    else -> false
  }

@UsedFromNativeCode
data class SuplaChannelHvacConfig(
  override val remoteId: Int,
  override val func: Int?,
  override val crc32: Long,
  val mainThermometerRemoteId: Int,
  val auxThermometerRemoteId: Int,
  val auxThermometerType: SuplaHvacThermometerType,
  val antiFreezeAndOverheatProtectionEnabled: Boolean,
  val availableAlgorithms: List<SuplaHvacAlgorithm>,
  val usedAlgorithm: SuplaHvacAlgorithm,
  val minOnTimeSec: Int,
  val minOffTimeSec: Int,
  val outputValueOnError: Int,
  val subfunction: ThermostatSubfunction,
  val temperatureSetpointChangeSwitchesToManualMode: Boolean,
  val temperatureControlType: SuplaTemperatureControlType,
  val temperatures: SuplaHvacTemperatures
) : SuplaChannelConfig(remoteId, func, crc32) {
  val minTemperature: Float?
    get() =
      when (temperatureControlType) {
        AUX_HEATER_COOLER_TEMPERATURE -> temperatures.auxMinSetpoint ?: temperatures.auxMin
        else -> temperatures.roomMin
      }?.fromSuplaTemperature()

  val maxTemperature: Float?
    get() =
      when (temperatureControlType) {
        AUX_HEATER_COOLER_TEMPERATURE -> temperatures.auxMaxSetpoint ?: temperatures.auxMax
        else -> temperatures.roomMax
      }?.fromSuplaTemperature()
}
