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

enum class SuplaHvacThermometerType(value: Int) {
  NOT_SET(0),
  DISABLED(1),
  FLOOR(2),
  WATER(3),
  GENERIC_HEATER(4),
  GENERIC_COOLER(5)
}

enum class SuplaHvacAlgorithm(value: Int) {
  NOT_SET(0),
  ON_OFF_SETPOINT_MIDDLE(1),
  ON_OFF_SETPOINT_AT_MOST(2)
}

enum class ThermostatSubfunction(value: Int) {
  NOT_SET(0),
  HEAT(1),
  COOL(2)
}

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
  val temperatures: SuplaHvacTemperatures
) : SuplaChannelConfig(remoteId, func, crc32)
